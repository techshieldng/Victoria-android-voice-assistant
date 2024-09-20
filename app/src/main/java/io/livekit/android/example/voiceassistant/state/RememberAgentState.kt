package io.livekit.android.example.voiceassistant.state

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import io.livekit.android.room.participant.Participant
import io.livekit.android.util.flow
import kotlinx.coroutines.flow.collectLatest

private const val AGENT_STATE_KEY = "lk.agent.state"

/**
 * Keeps track of the agent state for a participant.
 */
@Composable
fun rememberAgentState(participant: Participant?): AgentState {
    var state by remember(participant) {
        val initialState = AgentState.fromAttribute(
            participant?.attributes?.get(AGENT_STATE_KEY)
        )
        mutableStateOf(initialState)
    }

    LaunchedEffect(key1 = participant) {
        if (participant == null) {
            return@LaunchedEffect
        }

        participant::attributes.flow.collectLatest { attributes ->
            state = AgentState.fromAttribute(attributes[AGENT_STATE_KEY])
        }
    }

    return state
}

enum class AgentState {
    LISTENING,
    THINKING,
    SPEAKING,
    UNKNOWN;

    companion object {
        fun fromAttribute(attribute: String?): AgentState {
            return when (attribute) {
                "listening" -> LISTENING
                "thinking" -> THINKING
                "speaking" -> SPEAKING
                else -> UNKNOWN
            }
        }
    }
}