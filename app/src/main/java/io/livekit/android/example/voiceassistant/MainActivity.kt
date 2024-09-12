@file:OptIn(Beta::class)

package io.livekit.android.example.voiceassistant

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import io.livekit.android.LiveKit
import io.livekit.android.annotations.Beta
import io.livekit.android.compose.local.RoomScope
import io.livekit.android.compose.state.rememberTracks
import io.livekit.android.compose.state.transcriptions.rememberParticipantTranscriptions
import io.livekit.android.compose.state.transcriptions.rememberTranscriptions
import io.livekit.android.example.voiceassistant.ui.RemoteAudioTrackBarVisualizer
import io.livekit.android.example.voiceassistant.ui.theme.LiveKitVoiceAssistantExampleTheme
import io.livekit.android.room.track.Track
import io.livekit.android.room.types.TranscriptionSegment
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
                    VoiceAssistant(modifier = Modifier.fillMaxSize())
                }
            }
        }
    }
}

@Composable
fun VoiceAssistant(modifier: Modifier = Modifier) {

    ConstraintLayout(modifier = modifier) {
        RoomScope(
            url = wsURL,
            token = token,
            audio = true,
            connect = true,
        ) { room ->
            val (audioVisualizer, chatLog) = createRefs()
            val trackRefs = rememberTracks(sources = listOf(Track.Source.MICROPHONE))
            val filtered = trackRefs.filter { it.participant != room.localParticipant }

            val segments = rememberTranscriptions()
            val localSegments = rememberParticipantTranscriptions(room.localParticipant)
            val lazyListState = rememberLazyListState()

            RemoteAudioTrackBarVisualizer(
                audioTrackRef = filtered.firstOrNull(),
                modifier = Modifier
                    .padding(8.dp)
                    .fillMaxWidth()
                    .constrainAs(audioVisualizer) {
                        height = Dimension.percent(0.1f)
                        width = Dimension.fillToConstraints

                        top.linkTo(parent.top)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                    }
            )

            // Scroll to bottom as new transcriptions come in.
            LaunchedEffect(segments) {
                lazyListState.scrollToItem((segments.size - 1).coerceAtLeast(0))
            }
            LazyColumn(
                userScrollEnabled = true,
                state = lazyListState,
                modifier = Modifier
                    .constrainAs(chatLog) {
                        bottom.linkTo(parent.bottom)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                        height = Dimension.percent(0.9f)
                        width = Dimension.fillToConstraints
                    }
            ) {
                items(
                    items = segments,
                    key = { segment -> segment.id },
                ) { segment ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                    ) {
                        if (localSegments.contains(segment)) {
                            UserTranscriptionBox(segment = segment, modifier = Modifier.align(Alignment.CenterEnd))
                        } else {
                            Text(text = segment.text, modifier = Modifier.align(Alignment.CenterStart))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun UserTranscriptionBox(
    segment: TranscriptionSegment,
    modifier: Modifier = Modifier
) {
    val state = remember {
        MutableTransitionState(false).apply {
            // Start the animation immediately.
            targetState = true
        }
    }
    AnimatedVisibility(
        visibleState = state,
        enter = fadeIn(),
        modifier = modifier
    ) {
        Box(
            modifier = modifier
                .clip(RoundedCornerShape(8.dp, 2.dp, 8.dp, 8.dp))
                .background(Color.LightGray)
        ) {
            Text(
                text = segment.text,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(8.dp)
            )
        }
    }
}