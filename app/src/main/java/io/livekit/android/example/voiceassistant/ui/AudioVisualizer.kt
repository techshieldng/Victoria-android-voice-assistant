package io.livekit.android.example.voiceassistant.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import io.livekit.android.compose.types.TrackReference
import io.livekit.android.room.track.RemoteAudioTrack
import io.livekit.android.util.LKLog
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import livekit.org.webrtc.AudioTrackSink
import java.nio.ByteBuffer

@Composable
fun RemoteAudioTrackBarVisualizer(audioTrackRef: TrackReference) {
    val audioSink = remember { AudioTrackSinkFlow() }
    DisposableEffect(key1 = audioTrackRef) {
        val track = audioTrackRef.publication?.track as? RemoteAudioTrack
        track?.addSink(audioSink)

        onDispose {
            track?.removeSink(audioSink)
        }
    }

    LaunchedEffect(key1 = audioTrackRef) {
        audioSink.audioFormat.collect {
            LKLog.e { "audio format: $it" }
        }
    }
    LaunchedEffect(key1 = audioTrackRef) {
        var count = 0
        audioSink.audioFlow.collect { (buffer, numFrames) ->
            count++
            count %= 100
            if (count == 0) {
                LKLog.e { "frames: ${numFrames}, hasArray: ${buffer.hasArray()}" }
            }
        }
    }
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