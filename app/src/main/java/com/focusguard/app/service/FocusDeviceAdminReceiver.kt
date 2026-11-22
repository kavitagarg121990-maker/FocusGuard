package com.focusguard.app.service

import android.app.admin.DeviceAdminReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast

/**
 * FocusDeviceAdminReceiver
 * Prevents app uninstallation and administrative bypass while Strict Mode is engaged.
 */
class FocusDeviceAdminReceiver : DeviceAdminReceiver() {

    override fun onEnabled(context: Context, intent: Intent) {
        super.onEnabled(context, intent)
        Toast.makeText(context, "FocusGuard Strict Mode Protection Enabled", Toast.LENGTH_SHORT).show()
    }

    override fun onDisableRequested(context: Context, intent: Intent): CharSequence {
        return "FocusGuard Strict Mode is active! Disabling protection will break your focus commitment."
    }

    override fun onDisabled(context: Context, intent: Intent) {
        super.onDisabled(context, intent)
        Toast.makeText(context, "FocusGuard Strict Mode Disabled", Toast.LENGTH_SHORT).show()
    }
}
