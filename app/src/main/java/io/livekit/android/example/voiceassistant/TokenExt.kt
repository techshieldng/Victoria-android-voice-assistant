package io.livekit.android.example.voiceassistant

import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import com.google.gson.Gson
import okhttp3.*
import java.io.IOException

// TODO: Add your Sandbox ID here
// When building an app for production, you should use your own token server.
const val sandboxID = ""

data class ConnectionDetails(
    val serverUrl: String,
    val participantToken: String,
)

/**
 * Ensures a valid LiveKit token is generated
 */
fun ComponentActivity.requireToken(onTokenGenerated: (cd: ConnectionDetails) -> Unit) {
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
                    val json = responseBody.string()
                    val cd = Gson().fromJson(json, ConnectionDetails::class.java)
                    runOnUiThread {
                        onTokenGenerated(cd)
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