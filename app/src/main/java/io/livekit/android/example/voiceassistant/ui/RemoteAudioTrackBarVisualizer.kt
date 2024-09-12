package io.livekit.android.example.voiceassistant.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.dp
import io.livekit.android.compose.types.TrackReference
import io.livekit.android.room.track.RemoteAudioTrack
import io.livekit.android.util.LKLog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import livekit.org.webrtc.AudioTrackSink
import java.nio.ByteBuffer
import kotlin.math.round
import kotlin.math.sqrt

// Odd for single bar in the middle.
private const val NUM_BARS = 15

private const val MIN_AMPLITUDE = 0.1f
private const val MAX_VOLUME = 25000.0
private const val MIN_VOLUME = MAX_VOLUME * MIN_AMPLITUDE


@Composable
fun RemoteAudioTrackBarVisualizer(audioTrackRef: TrackReference?, modifier: Modifier = Modifier) {
    val amplitudes = remember {
        val emptyInts = Array(NUM_BARS) { MIN_AMPLITUDE }
        mutableStateListOf(*emptyInts)
    }
    val audioSink = remember { AudioTrackSinkFlow() }
    DisposableEffect(key1 = audioTrackRef) {
        val track = audioTrackRef?.publication?.track as? RemoteAudioTrack
        track?.addSink(audioSink)

        onDispose {
            track?.removeSink(audioSink)
        }
    }

    LaunchedEffect(key1 = audioTrackRef) {
        audioSink.audioFormat.collectLatest {
            LKLog.e { "$it" }
        }
    }

    LaunchedEffect(key1 = audioTrackRef) {
        launch(Dispatchers.IO) {
            audioSink.audioFlow.collect { (buffer, _) ->
                val volume = (calculateVolume(buffer).coerceIn(MIN_VOLUME, MAX_VOLUME) / MAX_VOLUME).toFloat()

                val middle = NUM_BARS / 2
                amplitudes[middle] = volume

                var curVolume = volume
                for (i in 1..(NUM_BARS / 2)) {
                    curVolume = (curVolume * 0.8f).coerceAtLeast(MIN_AMPLITUDE)
                    amplitudes[middle - i] = curVolume
                    amplitudes[middle + i] = curVolume
                }
            }
        }
    }


    val colorBrush = SolidColor(Color(0xFF65f0f5))

    BarVisualizer(
        brush = colorBrush,
        innerPadding = 10.dp,
        radius = 2.dp,
        amplitudes = amplitudes,
        modifier = modifier,
    )
}

class AudioTrackSinkFlow : AudioTrackSink {
    val audioFormat = MutableStateFlow(AudioFormat(16, 48000, 1))
    val audioFlow = MutableSharedFlow<Pair<ByteBuffer, Int>>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    override fun onData(
        audioData: ByteBuffer,
        bitsPerSample: Int,
        sampleRate: Int,
        numberOfChannels: Int,
        numberOfFrames: Int,
        absoluteCaptureTimestampMs: Long
    ) {
        val curAudioFormat = audioFormat.value
        if (curAudioFormat.bitsPerSample != bitsPerSample ||
            curAudioFormat.sampleRate != sampleRate ||
            curAudioFormat.numberOfChannels != numberOfChannels
        ) {
            audioFormat.tryEmit(AudioFormat(bitsPerSample, sampleRate, numberOfChannels))
        }
        audioFlow.tryEmit(audioData to numberOfFrames)
    }
}

data class AudioFormat(val bitsPerSample: Int, val sampleRate: Int, val numberOfChannels: Int)

/**
 * Determines volume of input using the RMS of the amplitude.
 */
fun calculateVolume(input: ByteBuffer): Double {
    var average = 0L

    try {
        input.mark()

        val bytesPerSample = 2
        val size = input.remaining() / bytesPerSample
        while (input.remaining() >= bytesPerSample) {
            val value = input.getShort()
            average += value * value
        }

        average /= size

        return round(sqrt(average.toDouble()))
    } finally {
        input.reset()
    }
}

