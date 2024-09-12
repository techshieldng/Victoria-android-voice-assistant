package io.livekit.android.example.voiceassistant.state

import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import io.livekit.android.room.participant.Participant

@Composable
fun rememberAssistantState(participant: Participant?): AssistantState {

    val attributes = rememberParticipantAttributes(participant = participant)
    return remember(attributes) {
        derivedStateOf {
            when (attributes["voice_assistant.state"]) {
                "listening" -> AssistantState.LISTENING
                "thinking" -> AssistantState.THINKING
                "speaking" -> AssistantState.SPEAKING
                else -> AssistantState.UNKNOWN
            }
        }
    }.value
}

enum class AssistantState {
    LISTENING,
    THINKING,
    SPEAKING,
    UNKNOWN,
}