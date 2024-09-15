package io.livekit.android.example.voiceassistant.audio

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import livekit.org.webrtc.audio.JavaAudioDeviceModule
import livekit.org.webrtc.audio.JavaAudioDeviceModule.AudioSamples
import livekit.org.webrtc.audio.JavaAudioDeviceModule.SamplesReadyCallback

class LocalAudioTrackFlow: SamplesReadyCallback {
    val flow = MutableSharedFlow<AudioSamples>(extraBufferCapacity = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)

    override fun onWebRtcAudioRecordSamplesReady(samples: JavaAudioDeviceModule.AudioSamples) {
        flow.tryEmit(samples)
    }
}
