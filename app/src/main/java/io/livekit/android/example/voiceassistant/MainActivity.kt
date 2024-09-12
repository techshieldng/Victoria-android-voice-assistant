@file:OptIn(Beta::class)

package io.livekit.android.example.voiceassistant

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import io.livekit.android.LiveKit
import io.livekit.android.annotations.Beta
import io.livekit.android.compose.local.RoomScope
import io.livekit.android.compose.state.rememberTracks
import io.livekit.android.compose.state.transcriptions.rememberParticipantTranscriptions
import io.livekit.android.compose.state.transcriptions.rememberTranscriptions
import io.livekit.android.example.voiceassistant.ui.RemoteAudioTrackBarVisualizer
import io.livekit.android.example.voiceassistant.ui.theme.LiveKitVoiceAssistantExampleTheme
import io.livekit.android.room.track.Track
import io.livekit.android.util.LoggingLevel

// Replace these values with your url and generated token.
const val wsURL = ""
const val token =
    ""

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LiveKit.loggingLevel = LoggingLevel.DEBUG
        requireNeededPermissions {
            setContent {
                LiveKitVoiceAssistantExampleTheme {
                    RoomScope(
                        url = wsURL,
                        token = token,
                        audio = true,
                        connect = true,
                    ) { room ->
                        val trackRefs = rememberTracks(sources = listOf(Track.Source.MICROPHONE))
                        val filtered = trackRefs.filter { it.participant != room.localParticipant }

                        if (filtered.isNotEmpty()) {
                            RemoteAudioTrackBarVisualizer(audioTrackRef = filtered.first())
                        }

                        val segments = rememberTranscriptions()
                        val localSegments = rememberParticipantTranscriptions(room.localParticipant)
                        LazyColumn(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(
                                items = segments,
                                key = { segment -> segment.id },
                            ) { segment ->
                                Box(modifier = Modifier.fillMaxWidth()) {
                                    val alignment = if (localSegments.contains(segment)) {
                                        Alignment.CenterEnd
                                    } else {
                                        Alignment.CenterStart
                                    }

                                    Text(text = segment.text, modifier = Modifier.align(alignment))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}


