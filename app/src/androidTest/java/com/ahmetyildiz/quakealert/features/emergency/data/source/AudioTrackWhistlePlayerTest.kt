package com.ahmetyildiz.quakealert.features.emergency.data.source

import android.content.Context
import android.media.AudioManager
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AudioTrackWhistlePlayerTest {

    private val context: Context = InstrumentationRegistry.getInstrumentation().targetContext
    private val audioManager: AudioManager = context.getSystemService(AudioManager::class.java)
    private val player = AudioTrackWhistlePlayer(context)
    private var originalVolume: Int = 0

    @Before
    fun setUp() {
        originalVolume = audioManager.getStreamVolume(AudioManager.STREAM_ALARM)
        audioManager.setStreamVolume(AudioManager.STREAM_ALARM, audioManager.getStreamMinVolume(AudioManager.STREAM_ALARM), 0)
    }

    @After
    fun tearDown() {
        player.stop()
        audioManager.setStreamVolume(AudioManager.STREAM_ALARM, originalVolume, 0)
    }

    @Test
    fun whistlePlaysAtFullAlarmVolumeAndStopRestoresTheVolume() {
        val volumeBefore: Int = audioManager.getStreamVolume(AudioManager.STREAM_ALARM)
        assertTrue(player.start())
        assertEquals(audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM), audioManager.getStreamVolume(AudioManager.STREAM_ALARM))
        player.stop()
        assertEquals(volumeBefore, audioManager.getStreamVolume(AudioManager.STREAM_ALARM))
    }

    @Test
    fun startingTwiceKeepsOneWhistle() {
        assertTrue(player.start())
        assertTrue(player.start())
        player.stop()
        assertTrue(player.start())
    }
}
