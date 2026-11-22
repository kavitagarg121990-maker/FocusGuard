package com.focusguard.app.service

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.focusguard.app.FocusGuardApp
import com.focusguard.app.data.local.entity.ShortContentRuleEntity
import com.focusguard.app.ui.overlay.BlockOverlayActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean

/**
 * FocusAccessibilityService
 *
 * Core accessibility service for FocusGuard responsible for:
 * 1. Short-Form Content Detection (YouTube Shorts, Instagram Reels, Snapchat Spotlight, etc.)
 *    without locking the user out of legitimate app features (e.g., messaging or long-form videos).
 * 2. In-browser website blocking via URL bar node inspection.
 * 3. Strict Mode enforcement (blocking attempts to disable accessibility or force-stop FocusGuard).
 *
 * PRIVACY GUARANTEE:
 * All inspection is strictly real-time and volatile in memory. No keystrokes, personal messages,
 * credentials, or screen pixels are saved, logged, or transmitted anywhere. FocusGuard does not
 * request the INTERNET permission.
 *
 * NOTE ON APP UPDATES:
 * Major third-party apps frequently revise internal view IDs. The detection rules are loaded from
 * the local Room [ShortContentRuleEntity] table, allowing dynamic updates and custom resource IDs
 * without requiring service recompilation.
 */
class FocusAccessibilityService : AccessibilityService() {

    private val serviceScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var activeRules: List<ShortContentRuleEntity> = emptyList()
    private var blockedDomains: List<String> = emptyList()
    private var isStrictModeActive = false

    private var lastActionTime = 0L
    private val isHandlingBlock = AtomicBoolean(false)

    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.i(TAG, "FocusAccessibilityService connected.")

        val app = application as? FocusGuardApp ?: return
        val shortContentDao = app.database.shortContentRuleDao()
        val prefs = app.preferencesRepository

        // Observe active detection rules in real-time
        serviceScope.launch {
            shortContentDao.getActiveRules().collect { rules ->
                activeRules = rules
                Log.d(TAG, "Updated active short content rules: ${rules.map { it.id }}")
            }
        }

        // Observe blocked domains
        serviceScope.launch {
            prefs.blockedDomains.collect { domains ->
                blockedDomains = domains
            }
        }

        // Observe strict mode state
        serviceScope.launch {
            prefs.isStrictModeEnabled.collect { strict ->
                isStrictModeActive = strict
            }
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return

        val packageName = event.packageName?.toString() ?: return
        val eventType = event.eventType

        // Filter relevant event types to avoid unnecessary node traversal
        if (eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED &&
            eventType != AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED
        ) {
            return
        }

        // 1. Strict Mode Protection: Intercept attempts to tamper with FocusGuard
        if (isStrictModeActive && isTamperAttempt(packageName, event)) {
            performGlobalAction(GLOBAL_ACTION_BACK)
            showBlockOverlay(
                packageName = packageName,
                appName = "System Settings",
                reason = "Strict Mode Active: FocusGuard cannot be disabled until your focus timer ends."
            )
            return
        }

        // Throttle checks to prevent multiple actions per trigger
        val now = System.currentTimeMillis()
        if (now - lastActionTime < 1200L || isHandlingBlock.get()) {
            return
        }

        // 2. Short-Form Video Interception
        val matchingRules = activeRules.filter { it.packageName == packageName }
        if (matchingRules.isNotEmpty()) {
            val rootNode = rootInActiveWindow ?: return
            for (rule in matchingRules) {
                if (matchesShortContentTarget(rootNode, rule)) {
                    triggerShortsBlock(rule)
                    break
                }
            }
        }

        // 3. Browser Website Blocker (checks URL bar)
        if (blockedDomains.isNotEmpty() && isBrowserPackage(packageName)) {
            val rootNode = rootInActiveWindow ?: return
            checkBrowserUrlBar(rootNode, packageName)
        }
    }

    /**
     * Traverses the AccessibilityNodeInfo tree searching for matching resource ID fragments
     * or content descriptions specific to the target short-form feed.
     */
    private fun matchesShortContentTarget(
        root: AccessibilityNodeInfo,
        rule: ShortContentRuleEntity
    ): Boolean {
        val resourceIds = rule.targetResourceIdsCsv.split(",").map { it.trim().lowercase() }.filter { it.isNotEmpty() }
        val contentDescs = rule.targetContentDescriptionsCsv.split(",").map { it.trim().lowercase() }.filter { it.isNotEmpty() }

        return inspectNodeRecursive(root, resourceIds, contentDescs, depth = 0, maxDepth = 25)
    }

    private fun inspectNodeRecursive(
        node: AccessibilityNodeInfo?,
        targetIds: List<String>,
        targetDescriptions: List<String>,
        depth: Int,
        maxDepth: Int
    ): Boolean {
        if (node == null || depth > maxDepth) return false

        // Check View Resource ID
        val viewId = node.viewIdResourceName?.lowercase()
        if (viewId != null) {
            for (id in targetIds) {
                if (viewId.contains(id)) {
                    Log.d(TAG, "Short-form target detected by ViewId: $viewId matching $id")
                    return true
                }
            }
        }

        // Check Content Description
        val contentDesc = node.contentDescription?.toString()?.lowercase()
        if (contentDesc != null) {
            for (desc in targetDescriptions) {
                if (contentDesc == desc || contentDesc.contains(desc)) {
                    Log.d(TAG, "Short-form target detected by ContentDescription: $contentDesc matching $desc")
                    return true
                }
            }
        }

        // Recursively inspect children
        val childCount = node.childCount
        for (i in 0 until childCount) {
            val child = node.getChild(i) ?: continue
            val found = inspectNodeRecursive(child, targetIds, targetDescriptions, depth + 1, maxDepth)
            child.recycle()
            if (found) return true
        }

        return false
    }

    private fun triggerShortsBlock(rule: ShortContentRuleEntity) {
        lastActionTime = System.currentTimeMillis()
        if (isHandlingBlock.compareAndSet(false, true)) {
            // Step A: Immediately kick the user out of the addictive short feed
            performGlobalAction(GLOBAL_ACTION_BACK)

            // Step B: Record block event for stats
            val app = application as? FocusGuardApp
            app?.let {
                serviceScope.launch {
                    it.usageStatsRepository.recordBlockEvent(rule.packageName)
                }
            }

            // Step C: Show the friction overlay with 3-second delay
            showBlockOverlay(
                packageName = rule.packageName,
                appName = rule.displayName,
                reason = "${rule.displayName} blocked — stay focused on what truly matters."
            )

            serviceScope.launch {
                kotlinx.coroutines.delay(1500)
                isHandlingBlock.set(false)
            }
        }
    }

    private fun checkBrowserUrlBar(root: AccessibilityNodeInfo, browserPackage: String) {
        val urlNode = findUrlBarNode(root) ?: return
        val urlText = urlNode.text?.toString()?.lowercase() ?: return

        for (domain in blockedDomains) {
            if (urlText.contains(domain.lowercase())) {
                lastActionTime = System.currentTimeMillis()
                performGlobalAction(GLOBAL_ACTION_BACK)
                showBlockOverlay(
                    packageName = browserPackage,
                    appName = "Web Browser",
                    reason = "Access to $domain is blocked by your FocusGuard rules."
                )
                break
            }
        }
    }

    private fun findUrlBarNode(node: AccessibilityNodeInfo?): AccessibilityNodeInfo? {
        if (node == null) return null
        val id = node.viewIdResourceName?.lowercase() ?: ""
        if (id.contains("url_bar") || id.contains("search_box") || id.contains("location_bar")) {
            return node
        }
        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            val found = findUrlBarNode(child)
            if (found != null) return found
            child.recycle()
        }
        return null
    }

    private fun isTamperAttempt(packageName: String, event: AccessibilityEvent): Boolean {
        if (packageName == "com.android.settings") {
            val className = event.className?.toString() ?: ""
            val text = event.text.joinToString(" ").lowercase()
            return text.contains("focusguard") || className.contains("accessibility") || className.contains("deviceadmin")
        }
        return false
    }

    private fun isBrowserPackage(pkg: String): Boolean {
        return pkg.contains("chrome") || pkg.contains("firefox") || pkg.contains("browser") || pkg.contains("opera")
    }

    private fun showBlockOverlay(packageName: String, appName: String, reason: String) {
        val intent = Intent(this, BlockOverlayActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS
            putExtra(BlockOverlayActivity.EXTRA_PACKAGE_NAME, packageName)
            putExtra(BlockOverlayActivity.EXTRA_APP_NAME, appName)
            putExtra(BlockOverlayActivity.EXTRA_BLOCK_REASON, reason)
        }
        startActivity(intent)
    }

    override fun onInterrupt() {
        Log.w(TAG, "FocusAccessibilityService interrupted.")
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }

    companion object {
        private const val TAG = "FocusAccessibility"
    }
}
