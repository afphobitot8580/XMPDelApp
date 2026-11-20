package com.example.xmpdel

import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import java.io.File
import java.io.FileOutputStream

class MainActivity : AppCompatActivity() {

    private lateinit var statusText: TextView
    private var cleanedBytes: ByteArray? = null
    private var originalName: String = "fotka"

    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri == null) return@registerForActivityResult
        try {
            val input = contentResolver.openInputStream(uri) ?: return@registerForActivityResult
            val bytes = input.readBytes()
            input.close()
            val (cleaned, removed) = stripXmp(bytes)
            cleanedBytes = cleaned
            originalName = uri.lastPathSegment?.substringAfterLast("/") ?: "fotka"
            statusText.text = if (removed) {
                "Hotovo: XMP smazáno. Původní: ${bytes.size} B, nové: ${cleaned.size} B. Teď dej Uložit."
            } else {
                "XMP nenalezeno, fotka je už čistá. Můžeš ji i tak uložit."
            }
        } catch (e: Exception) {
            statusText.text = "Chyba: ${e.message}"
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        statusText = findViewById(R.id.statusText)
        findViewById<Button>(R.id.pickButton).setOnClickListener { pickImage.launch("image/jpeg") }
        findViewById<Button>(R.id.saveButton).setOnClickListener { saveCleaned() }
    }

    private fun saveCleaned() {
        val data = cleanedBytes
        if (data == null) {
            Toast.makeText(this, "Nejdřív vyber fotku", Toast.LENGTH_SHORT).show()
            return
        }
        try {
            val dir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "XMPDelApp")
            if (!dir.exists()) dir.mkdirs()
            val cleanName = originalName.replace(".jpg", "").replace(".jpeg", "") + "_bezXMP.jpg"
            val outFile = File(dir, cleanName)
            FileOutputStream(outFile).use { it.write(data) }
            statusText.text = "Uloženo do: Stažené / XMPDelApp / $cleanName"
            Toast.makeText(this, "Uloženo", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            statusText.text = "Chyba při ukládání: ${e.message}"
        }
    }

    // Odstraní z JPEG všechny APP1 segmenty s XMP: http://ns.adobe.com/xap/1.0/
    private fun stripXmp(bytes: ByteArray): Pair<ByteArray, Boolean> {
        if (bytes.size < 4 || bytes[0] != 0xFF.toByte() || bytes[1] != 0xD8.toByte()) {
            return Pair(bytes, false)
        }
        val out = mutableListOf<Byte>()
        out.add(bytes[0]); out.add(bytes[1])
        var i = 2
        var removed = false
        val xmpHeader = "http://ns.adobe.com/xap/1.0/".toByteArray(Charsets.US_ASCII)
        while (i + 3 < bytes.size) {
            if (bytes[i] != 0xFF.toByte()) break
            val marker = bytes[i + 1]
            if (marker == 0xDA.toByte()) {
                // SOS - zbytek je obrazová data, kopíruj beze změny
                while (i < bytes.size) { out.add(bytes[i]); i++ }
                break
            }
            if (marker == 0xD9.toByte()) { out.add(bytes[i]); out.add(bytes[i+1]); break }
            if (i + 4 > bytes.size) break
            val len = ((bytes[i+2].toInt() and 0xFF) shl 8) or (bytes[i+3].toInt() and 0xFF)
            if (len < 2) break
            val segmentEnd = i + 2 + len
            if (segmentEnd > bytes.size) break
            val isXmp = marker == 0xE1.toByte() && segmentHasPrefix(bytes, i+4, xmpHeader)
            if (isXmp) {
                removed = true
                // přeskočit - nemazat, jen nezkopírovat
            } else {
                for (j in i until segmentEnd) out.add(bytes[j])
            }
            i = segmentEnd
        }
        return Pair(out.toByteArray(), removed)
    }

    private fun segmentHasPrefix(bytes: ByteArray, start: Int, prefix: ByteArray): Boolean {
        if (start + prefix.size > bytes.size) return false
        for (k in prefix.indices) if (bytes[start+k] != prefix[k]) return false
        return true
    }
}
