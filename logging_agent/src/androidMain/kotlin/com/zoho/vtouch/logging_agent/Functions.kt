package com.zoho.vtouch.logging_agent

import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.Drawable
import com.soywiz.klock.TimeSpan
import com.soywiz.korio.async.delay
import com.zoho.vtouch.logging_agent.LoggingAgent.AVAILABLE_MEMORY
import com.zoho.vtouch.logging_agent.LoggingAgent.USED_MEMORY
import com.zoho.vtouch.logging_agent.model.GraphData
import com.zoho.vtouch.logging_agent.model.JsonData.Companion.TABLE_DATA
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.io.InputStream


actual typealias JSONObject = org.json.JSONObject
actual typealias PlatformContext = Activity

actual fun loadContent(fileName: String?, context: PlatformContext): ByteArray? {
    var input: InputStream? = null
    return try {
        val output = ByteArrayOutputStream()
        input = context.assets.open(fileName!!)
        val buffer = ByteArray(1024)
        var size: Int
        while (-1 != input.read(buffer).also { size = it }) {
            output.write(buffer, 0, size)
        }
        output.flush()
        output.toByteArray()
    } catch (e: Exception) {
        null
    } finally {
        try {
            input?.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

actual fun createJSON(json: JSONObject, id: String): String {
    val obj = JSONObject()
    obj.put("type", TABLE_DATA)
    obj.put("json", json)
    obj.put("id", id)
    return obj.toString()
}

actual fun createJSON(json: String, id: String): String = createJSON(JSONObject(json), id)

actual fun getApplicationIcon(context: PlatformContext): ByteArray? {
    val d = context.packageManager.getApplicationIcon(context.applicationInfo)
    val stream = ByteArrayOutputStream()
    val bitMap = getBitmapFromDrawable(d)
    bitMap?.compress(Bitmap.CompressFormat.JPEG, 100, stream)
    return stream.toByteArray()
}

private fun getBitmapFromDrawable(drawable: Drawable): Bitmap? {
    val bmp = Bitmap.createBitmap(
        drawable.intrinsicWidth,
        drawable.intrinsicHeight,
        Bitmap.Config.ARGB_8888
    )
    val canvas = Canvas(bmp)
    val paint = Paint()
    paint.color = Color.WHITE
    canvas.drawRect(0F, 0F, canvas.width.toFloat(), canvas.height.toFloat(), paint)
    drawable.setBounds(0, 0, canvas.width, canvas.height)
    drawable.draw(canvas)
    return bmp
}

actual fun sendMemoryStats(webSocket: WebSocketServer?, context: PlatformContext) {
    val runtime: Runtime = Runtime.getRuntime()
    val memoryInfo = ActivityManager.MemoryInfo()
    (context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager).getMemoryInfo(
        memoryInfo
    )

    CoroutineScope(Dispatchers.Default).launch {
        var usedMemInMB: Long
        var maxHeapSizeInMB: Long
        var availHeapSizeInMB: Long
        while (true) {
            if (webSocket?.isRunning == false)
                return@launch
            if (webSocket?.isClientConnected == true) {
                delay(TimeSpan(2000.0))
                usedMemInMB = (runtime.totalMemory() - runtime.freeMemory()) / 1048576L
                maxHeapSizeInMB = runtime.maxMemory() / 1048576L
                availHeapSizeInMB = maxHeapSizeInMB - usedMemInMB
                webSocket.sendGraphData(
                    listOf(
                        GraphData(
                            USED_MEMORY,
                            usedMemInMB.toFloat(),
                            System.currentTimeMillis()
                        ),
                        GraphData(
                            AVAILABLE_MEMORY,
                            availHeapSizeInMB.toFloat(),
                            System.currentTimeMillis()
                        )
                    )
                )
            }
        }
    }
}