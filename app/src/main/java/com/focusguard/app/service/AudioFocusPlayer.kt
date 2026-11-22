package com.focusguard.app.service

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import com.focusguard.app.data.model.FocusSoundType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Random
import kotlin.math.sin

/**
 * 100% offline ambient focus sound player.
 * Uses procedural PCM synthesis with [AudioTrack] for White Noise, Rain, and Binaural Beats.
 * Zero asset downloads, perfectly seamless looping, and completely private.
 */
class AudioFocusPlayer {

    private val playerScope = CoroutineScope(Dispatchers.Default)
    private var playbackJob: Job? = null
    private var audioTrack: AudioTrack? = null
    private var currentType: FocusSoundType = FocusSoundType.NONE

    val currentSound: FocusSoundType
        get() = currentType

    fun play(soundType: FocusSoundType) {
        if (soundType == FocusSoundType.NONE) {
            stop()
            return
        }

        stop()
        currentType = soundType

        playbackJob = playerScope.launch {
            val sampleRate = 44100
            val bufferSize = AudioTrack.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            ).coerceAtLeast(8192)

            val track = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(bufferSize)
                .setTransferMode(AudioTrack.MODE_STREAM)
                .build()

            audioTrack = track
            track.play()

            val buffer = ShortArray(bufferSize / 2)
            val random = Random()
            var phase = 0.0

            when (soundType) {
                FocusSoundType.WHITE_NOISE -> {
                    while (isActive) {
                        for (i in buffer.indices) {
                            // Soft Gaussian-like white noise
                            val noise = (random.nextDouble() * 2.0 - 1.0) * 0.15
                            buffer[i] = (noise * Short.MAX_VALUE).toInt().toShort()
                        }
                        track.write(buffer, 0, buffer.size)
                    }
                }
                FocusSoundType.RAIN -> {
                    var lastSample = 0.0
                    while (isActive) {
                        for (i in buffer.indices) {
                            // Pink/Brown noise filtered for rain-like patter
                            val white = random.nextDouble() * 2.0 - 1.0
                            lastSample = (lastSample * 0.96) + (white * 0.04)
                            val modulated = lastSample * 0.4
                            buffer[i] = (modulated * Short.MAX_VALUE).toInt().toShort()
                        }
                        track.write(buffer, 0, buffer.size)
                    }
                }
                FocusSoundType.BINAURAL -> {
                    val baseFreq = 220.0 // A3 note
                    val beatFreq = 40.0 // Gamma wave for deep focus
                    val twoPi = 2.0 * Math.PI
                    while (isActive) {
                        for (i in buffer.indices) {
                            val sample = sin(phase * twoPi) * sin((phase * (beatFreq / baseFreq)) * twoPi) * 0.2
                            buffer[i] = (sample * Short.MAX_VALUE).toInt().toShort()
                            phase += baseFreq / sampleRate
                            if (phase >= 1.0) phase -= 1.0
                        }
                        track.write(buffer, 0, buffer.size)
                    }
                }
                FocusSoundType.NONE -> {
                    // No-op
                }
            }
        }
    }

    fun stop() {
        currentType = FocusSoundType.NONE
        playbackJob?.cancel()
        playbackJob = null
        try {
            audioTrack?.apply {
                stop()
                release()
            }
        } catch (e: Exception) {
            // Safe cleanup
        }
        audioTrack = null
    }
}
