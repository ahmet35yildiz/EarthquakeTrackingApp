package com.ahmetyildiz.quakealert.features.emergency.data.source

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import com.ahmetyildiz.quakealert.features.emergency.domain.EmergencyConfig
import com.ahmetyildiz.quakealert.features.emergency.domain.WhistlePlayer
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class AudioTrackWhistlePlayer @Inject constructor(
    @ApplicationContext context: Context,
) : WhistlePlayer {

    private val audioManager: AudioManager = context.getSystemService(AudioManager::class.java)
    private val waveform: ShortArray by lazy { WhistleWaveform.create(EmergencyConfig.WHISTLE_PATTERN, SAMPLE_RATE_HZ) }
    private var audioTrack: AudioTrack? = null
    private var alarmVolumeBeforeStart: Int? = null

    override fun start(): Boolean {
        if (audioTrack != null) return true
        val track: AudioTrack = createLoopingTrack() ?: return false
        raiseAlarmVolume()
        track.play()
        audioTrack = track
        return true
    }

    override fun stop() {
        audioTrack?.let { track ->
            track.stop()
            track.release()
        }
        audioTrack = null
        restoreAlarmVolume()
    }

    private fun createLoopingTrack(): AudioTrack? {
        val track: AudioTrack = try {
            buildTrack()
        } catch (exception: UnsupportedOperationException) {
            return null
        }
        val isReady: Boolean = track.write(waveform, 0, waveform.size) == waveform.size &&
            track.state == AudioTrack.STATE_INITIALIZED &&
            track.setLoopPoints(0, waveform.size, LOOP_FOREVER) == AudioTrack.SUCCESS
        if (isReady) return track
        track.release()
        return null
    }

    private fun buildTrack(): AudioTrack =
        AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build(),
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(SAMPLE_RATE_HZ)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build(),
            )
            .setTransferMode(AudioTrack.MODE_STATIC)
            .setBufferSizeInBytes(waveform.size * Short.SIZE_BYTES)
            .build()

    private fun raiseAlarmVolume() {
        alarmVolumeBeforeStart = audioManager.getStreamVolume(AudioManager.STREAM_ALARM)
        setAlarmVolume(audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM))
    }

    private fun restoreAlarmVolume() {
        val volume: Int = alarmVolumeBeforeStart ?: return
        setAlarmVolume(volume)
        alarmVolumeBeforeStart = null
    }

    private fun setAlarmVolume(volume: Int) {
        try {
            audioManager.setStreamVolume(AudioManager.STREAM_ALARM, volume, 0)
        } catch (exception: SecurityException) {
            return
        }
    }

    private companion object {
        const val SAMPLE_RATE_HZ: Int = 44_100
        const val LOOP_FOREVER: Int = -1
    }
}
