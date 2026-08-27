package com.huanchengfly.tieba.post.utils

import android.os.Environment
import android.util.Log
import com.huanchengfly.tieba.post.App
import java.io.File
import java.io.FileOutputStream
import java.nio.charset.StandardCharsets

object AgreeDebugUtil {
    var lastAgreeJson: String = ""

    fun recordAgreeJson(json: String) {
        lastAgreeJson = json
        Log.d("TIEBA_AGREE_DUMP", json)
        try {
            // 1. Documents / TiebaLite
            val docsDir = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),
                "TiebaLite"
            )
            if (!docsDir.exists()) docsDir.mkdirs()
            val file1 = File(docsDir, "agree_dump.json")
            FileOutputStream(file1).use {
                it.write(json.toByteArray(StandardCharsets.UTF_8))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        try {
            // 2. App External Files
            val extDir = App.INSTANCE.getExternalFilesDir("debug")
            if (extDir != null) {
                if (!extDir.exists()) extDir.mkdirs()
                val file2 = File(extDir, "agree_dump.json")
                FileOutputStream(file2).use {
                    it.write(json.toByteArray(StandardCharsets.UTF_8))
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        try {
            // 3. App internal filesDir
            val file3 = File(App.INSTANCE.filesDir, "agree_dump.json")
            FileOutputStream(file3).use {
                it.write(json.toByteArray(StandardCharsets.UTF_8))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
