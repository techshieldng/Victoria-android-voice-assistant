package io.livekit.android.example.voiceassistant

import android.widget.Toast
import androidx.activity.ComponentActivity
import com.google.gson.Gson
import java.io.IOException
import okhttp3.*

// TODO: Add your Sandbox ID here
const val sandboxID = ""

/**
 * Ensures a valid LiveKit token is generated.
 *
 * Currently configured to use LiveKit's Sandbox token server.
 * When building an app for production, you should use your own token server.
 */
fun ComponentActivity.requireToken(onTokenGenerated: (url: String, token: String) -> Unit) {
    val activity = this
    val tokenEndpoint = "https://cloud-api.livekit.io/api/sandbox/connection-details"
    val client = OkHttpClient()
    val request = Request.Builder()
        .url(tokenEndpoint)
        .header("X-Sandbox-ID", sandboxID)
        .build()

    client.newCall(request).enqueue(object : Callback {
        override fun onFailure(call: Call, e: IOException) {
            e.printStackTrace()
            runOnUiThread {
                Toast.makeText(
                    activity,
                    "Failed to fetch connection details",
                    Toast.LENGTH_SHORT
                )
                    .show()
            }
        }

        override fun onResponse(call: Call, response: Response) {
            response.body.let { responseBody ->
                if (response.isSuccessful) {
                    data class ConnectionDetails(
                        val serverUrl: String,
                        val participantToken: String,
                    )
                    val json = responseBody.string()
                    val cd = Gson().fromJson(json, ConnectionDetails::class.java)
                    runOnUiThread {
                        onTokenGenerated(cd.serverUrl, cd.participantToken)
                    }
                } else {
                    runOnUiThread {
                        Toast.makeText(
                            activity,
                            "Failed to parse connection details",
                            Toast.LENGTH_SHORT
                        )
                            .show()
                    }
                }
            }
        }
    })
}