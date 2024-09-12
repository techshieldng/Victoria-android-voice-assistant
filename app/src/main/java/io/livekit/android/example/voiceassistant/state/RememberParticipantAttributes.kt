package io.livekit.android.example.voiceassistant.state

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import io.livekit.android.compose.flow.rememberEventSelector
import io.livekit.android.events.ParticipantEvent
import io.livekit.android.room.participant.Participant
import io.livekit.android.util.LKLog

@Composable
fun rememberParticipantAttributes(participant: Participant?): Map<String, String> {
    if (participant == null) {
        return emptyMap()
    }

    val attributes = remember(participant) {
        val pairs = participant.attributes.entries
            .map { entry -> entry.key to entry.value }
            .toTypedArray()
        mutableStateMapOf(*pairs)
    }
    val events = rememberEventSelector<ParticipantEvent.AttributesChanged>(participant = participant)

    LaunchedEffect(key1 = participant) {
        events.collect { event ->
            for ((key, value) in event.changedAttributes) {
                LKLog.e { "$key changed to $value" }
                attributes[key] = value
            }
        }
    }

    return attributes
}