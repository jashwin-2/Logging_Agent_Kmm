package com.zoho.vtouch.logging_agent.android


import android.content.res.AssetManager
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.zoho.vtouch.logging_agent.LoggingAgent
import com.zoho.vtouch.logging_agent.WebSocketCallback
import com.zoho.vtouch.logging_agent.WebSocketServer
import com.zoho.vtouch.logging_agent.model.GraphData
import com.zoho.vtouch.logging_agent.model.LogMessage
import com.zoho.vtouch.logging_agent.model.SessionDetails
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.IOException
import java.io.InputStream
import java.util.*

class MainActivity : AppCompatActivity() {
    var count = 1
    var webSocket: WebSocketServer? = null

    companion object {
        const val AUDIO_STATS = "Audio Stats"
        const val VIDEO_STATS = "Video Stats"
        const val STATS_4 = "Stats 4"
        const val STATS_5 = "Stats 5"
        const val LOGS = "Logs"
        const val LOGS_1 = "Logs 1"
        const val GRAPH_1 = "Graph 1"
        const val GRAPH_2 = "Graph2"
        const val GRAPH_3 = "Graph3"
        const val GRAPH_4 = "Graph4"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        val sessionDetails = SessionDetails(
            listOf(
                AUDIO_STATS,
                VIDEO_STATS, STATS_4, STATS_5
            ),
            listOf(LOGS, LOGS_1),
            listOf(GRAPH_1, GRAPH_2, GRAPH_3, GRAPH_4)
        )
        webSocket = LoggingAgent.initialize(
            serverPort = 8000,
            sessionDetails,
            object : WebSocketCallback {
                override fun onError(ex: Exception) {
                    ex.localizedMessage?.let { Log.d("Error", it) }
                }

                override fun onMessageReceived(message: String?) {
                    Log.d("Message Received", "Client : $message")
                }

            }, this.assets
        )

        et_address.text = LoggingAgent.getAddress()




        GlobalScope.launch {
            sendGraphs()
            while (true) {
                delay(1000)
                if (webSocket?.isClientConnected == true) {
                    sendStats()
                    sendLog()
                }

            }
        }

    }

    override fun onDestroy() {
        LoggingAgent.shutDown()
        super.onDestroy()
    }

    fun sendStats() {
        val audioStats = Objects.requireNonNull(getJsonFromAssets(this.assets, "audio_stats.json"))
        val videoStats = Objects.requireNonNull(getJsonFromAssets(this.assets, "video_stats.json"))

        webSocket?.sendStatsToClient(
            JSONObject(audioStats),
            VIDEO_STATS
        )
        webSocket?.sendStatsToClient(
            JSONObject(videoStats),
            STATS_4
        )

        webSocket?.sendStatsToClient(
            videoStats!!,
            STATS_5
        )

        webSocket?.sendStatsToClient(
            audioStats!!,
            AUDIO_STATS
        )

    }

    fun sendLog() {
        val id = if (count % 2 == 0) LOGS else LOGS_1
        LogMessage(
            LogMessage.ERROR,
            "Error Log",
            System.currentTimeMillis()
        ).also {
            webSocket?.sendLogMessage(it, id)
        }
        LogMessage(
            LogMessage.INFO,
            "Info log ",
            System.currentTimeMillis()
        ).also {
            webSocket?.sendLogMessage(it, id)
        }

        count++

    }

    fun getJsonFromAssets(manager: AssetManager, fileName: String?): String? {
        return try {
            val inputStream: InputStream = manager.open(fileName!!)
            val size = inputStream.available()
            val buffer = ByteArray(size)
            inputStream.read(buffer)
            inputStream.close()
            String(buffer, charset("UTF-8"))
        } catch (e: IOException) {
            e.printStackTrace()
            return null
        }
    }

    fun sendGraphs() {
        GlobalScope.launch {
            while (true) {
                delay(2000)
                if (webSocket?.isClientConnected == true) {
                    val list = mutableListOf<GraphData>()
                    list.addAll(
                        listOf(
                            GraphData(
                                GRAPH_1,
                                (0..100).random(),
                                System.currentTimeMillis()
                            ),
                            GraphData(
                                GRAPH_2,
                                (0..100).random(),
                                System.currentTimeMillis()
                            ),
                            GraphData(
                                GRAPH_3,
                                (0..100).random(),
                                System.currentTimeMillis()
                            ),
                            GraphData(
                                GRAPH_4,
                                (0..100).random(),
                                System.currentTimeMillis()
                            ),
                        )
                    )
                    webSocket?.sendGraphData(list)

                }
            }
        }
    }


}

