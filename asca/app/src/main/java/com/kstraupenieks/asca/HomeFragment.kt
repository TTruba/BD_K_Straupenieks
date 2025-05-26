package com.kstraupenieks.asca

import android.Manifest
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.activity.result.contract.ActivityResultContracts
import kotlinx.coroutines.*
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.pm.PackageManager
import android.os.Build
import android.media.*
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import java.io.*

class HomeFragment : Fragment() {

    private var selectedFileUri: Uri? = null
    private lateinit var tvSelectedFile: TextView
    private lateinit var tvTranscriptionResult: TextView
    private lateinit var btnTranscribe: Button

    private var recording = false
    private lateinit var btnStartRealtime: Button
    private lateinit var btnStopRealtime: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        val sharedPreferences = requireActivity().getSharedPreferences("UserSession", Context.MODE_PRIVATE)
        val username = sharedPreferences.getString("username", "User")
        val tvWelcome = view.findViewById<TextView>(R.id.tvWelcome)
        tvWelcome.text = "Welcome, $username!"

        val btnSelectFile = view.findViewById<Button>(R.id.btnSelectFile)
        btnTranscribe = view.findViewById(R.id.btnTranscribe)
        tvSelectedFile = view.findViewById(R.id.tvSelectedFile)
        tvTranscriptionResult = view.findViewById(R.id.tvTranscriptionResult)
        btnStartRealtime = view.findViewById(R.id.btnStartRealtime)
        btnStopRealtime = view.findViewById(R.id.btnStopRealtime)

        btnTranscribe.isEnabled = false
        btnStopRealtime.isEnabled = false

        val filePicker = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            if (uri != null) {
                selectedFileUri = uri
                val fileName = getFileName(requireContext(), uri)
                tvSelectedFile.text = "Selected: $fileName"
                btnTranscribe.isEnabled = true
            }
        }

        btnSelectFile.setOnClickListener {
            filePicker.launch("audio/mpeg")
        }

        btnTranscribe.setOnClickListener {
            selectedFileUri?.let { uri ->
                CoroutineScope(Dispatchers.IO).launch {
                    val transcription = transcribeWithOpenAI(requireContext(), uri)
                    val classification = classifyWithOpenAI(transcription)

                    withContext(Dispatchers.Main) {
                        val textOnly = JSONObject(transcription).getString("text")
                        tvTranscriptionResult.text = """
                        Transcription:
                        $textOnly

                        Possible scammer? → $classification
                        """.trimIndent()

                        if (classification.equals("yes", ignoreCase = true)) {
                            showScamAlertNotification()
                        }
                    }
                }
            }
        }

        btnStartRealtime.setOnClickListener {
            when {
                ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED -> {
                    recording = true
                    btnStartRealtime.isEnabled = false
                    btnStopRealtime.isEnabled = true
                    startRecordingLoop()
                }
                shouldShowRequestPermissionRationale(android.Manifest.permission.RECORD_AUDIO) -> {
                    Toast.makeText(requireContext(), "Microphone permission is needed to record audio", Toast.LENGTH_SHORT).show()
                }
                else -> {
                    requestPermissionLauncher.launch(android.Manifest.permission.RECORD_AUDIO)
                }
            }
        }

        btnStopRealtime.setOnClickListener {
            recording = false
            btnStartRealtime.isEnabled = true
            btnStopRealtime.isEnabled = false
        }

        return view
    }
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            startRecordingLoop()
        } else {
            Toast.makeText(requireContext(), "Microphone permission denied", Toast.LENGTH_SHORT).show()
        }
    }
    private fun startRecordingLoop() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Toast.makeText(requireContext(), "Microphone permission not granted", Toast.LENGTH_SHORT).show()
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            while (recording) {
                try {
                    val pcmFile = File(requireContext().cacheDir, "chunk_${System.currentTimeMillis()}.pcm")
                    record10SecondsToFile(pcmFile)
                    val wavFile = convertPcmToWav(pcmFile)
                    val transcription = transcribeWithOpenAI(requireContext(), Uri.fromFile(wavFile))
                    val classification = classifyWithOpenAI(transcription)

                    withContext(Dispatchers.Main) {
                        val textOnly = JSONObject(transcription).optString("text", "no text")
                        tvTranscriptionResult.text = """
                        Transcription:
                        $textOnly

                        Possible scammer? → $classification
                    """.trimIndent()

                        if (classification.equals("yes", ignoreCase = true)) {
                            showScamAlertNotification()
                        }
                    }
                } catch (e: SecurityException) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(requireContext(), "Microphone access error: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                    break
                }
            }
        }
    }


    @RequiresPermission(Manifest.permission.RECORD_AUDIO)
    private fun record10SecondsToFile(outputFile: File) {
        val sampleRate = 16000
        val bufferSize = AudioRecord.getMinBufferSize(sampleRate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT)
        val audioRecord = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            sampleRate,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
            bufferSize
        )

        val outputStream = FileOutputStream(outputFile)
        val buffer = ByteArray(bufferSize)
        val endTime = System.currentTimeMillis() + 10_000

        audioRecord.startRecording()
        while (System.currentTimeMillis() < endTime) {
            val read = audioRecord.read(buffer, 0, buffer.size)
            if (read > 0) outputStream.write(buffer, 0, read)
        }
        audioRecord.stop()
        audioRecord.release()
        outputStream.close()
    }

    private fun convertPcmToWav(pcmFile: File): File {
        val wavFile = File(pcmFile.parent, pcmFile.name.replace(".pcm", ".wav"))
        val pcmData = pcmFile.readBytes()
        val wavHeader = createWavHeader(pcmData.size)
        wavFile.writeBytes(wavHeader + pcmData)
        return wavFile
    }

    private fun createWavHeader(pcmLength: Int): ByteArray {
        val sampleRate = 16000
        val channels = 1
        val byteRate = 16 * sampleRate * channels / 8

        val header = ByteArray(44)
        header[0] = 'R'.toByte()
        header[1] = 'I'.toByte()
        header[2] = 'F'.toByte()
        header[3] = 'F'.toByte()
        val totalDataLen = pcmLength + 36
        header[4] = (totalDataLen and 0xff).toByte()
        header[5] = ((totalDataLen shr 8) and 0xff).toByte()
        header[6] = ((totalDataLen shr 16) and 0xff).toByte()
        header[7] = ((totalDataLen shr 24) and 0xff).toByte()
        header[8] = 'W'.toByte()
        header[9] = 'A'.toByte()
        header[10] = 'V'.toByte()
        header[11] = 'E'.toByte()
        header[12] = 'f'.toByte()
        header[13] = 'm'.toByte()
        header[14] = 't'.toByte()
        header[15] = ' '.toByte()
        header[16] = 16
        header[17] = 0
        header[18] = 0
        header[19] = 0
        header[20] = 1
        header[21] = 0
        header[22] = channels.toByte()
        header[23] = 0
        header[24] = (sampleRate and 0xff).toByte()
        header[25] = ((sampleRate shr 8) and 0xff).toByte()
        header[26] = ((sampleRate shr 16) and 0xff).toByte()
        header[27] = ((sampleRate shr 24) and 0xff).toByte()
        header[28] = (byteRate and 0xff).toByte()
        header[29] = ((byteRate shr 8) and 0xff).toByte()
        header[30] = ((byteRate shr 16) and 0xff).toByte()
        header[31] = ((byteRate shr 24) and 0xff).toByte()
        header[32] = (2).toByte()
        header[33] = 0
        header[34] = 16
        header[35] = 0
        header[36] = 'd'.toByte()
        header[37] = 'a'.toByte()
        header[38] = 't'.toByte()
        header[39] = 'a'.toByte()
        header[40] = (pcmLength and 0xff).toByte()
        header[41] = ((pcmLength shr 8) and 0xff).toByte()
        header[42] = ((pcmLength shr 16) and 0xff).toByte()
        header[43] = ((pcmLength shr 24) and 0xff).toByte()
        return header
    }

    private fun getFileName(context: Context, uri: Uri): String {
        var result: String? = null
        if (uri.scheme == "content") {
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (nameIndex != -1 && it.moveToFirst()) {
                    result = it.getString(nameIndex)
                }
            }
        }
        return result ?: uri.lastPathSegment ?: "unknown_file"
    }

    private fun transcribeWithOpenAI(context: Context, audioUri: Uri): String {
        val apiKey = Constants.API_KEY
        val contentResolver = context.contentResolver
        val inputStream = contentResolver.openInputStream(audioUri) ?: return "File not found"
        val fileBytes = inputStream.readBytes()

        val client = OkHttpClient()
        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart(
                "file", "audio.wav",
                fileBytes.toRequestBody("audio/wav".toMediaTypeOrNull())
            )
            .addFormDataPart("model", "whisper-1")
            .build()

        val request = Request.Builder()
            .url("https://api.openai.com/v1/audio/transcriptions")
            .addHeader("Authorization", "Bearer $apiKey")
            .post(requestBody)
            .build()

        val response = client.newCall(request).execute()
        return response.body?.string() ?: "No response from OpenAI"
    }

    private fun classifyWithOpenAI(transcription: String): String {
        val apiKey = Constants.API_KEY
        val client = OkHttpClient()
        val prompt = "Scam call? Reply Yes or No.\n\n$transcription"

        val jsonBody = JSONObject()
        jsonBody.put("model", "gpt-3.5-turbo")
        val messages = org.json.JSONArray()
        messages.put(JSONObject().put("role", "system").put("content", "Reply only Yes or No if it's a scam."))
        messages.put(JSONObject().put("role", "user").put("content", prompt))
        jsonBody.put("messages", messages)
        jsonBody.put("max_tokens", 5)
        jsonBody.put("temperature", 0.2)

        val requestBody = jsonBody.toString().toRequestBody("application/json".toMediaTypeOrNull())

        val request = Request.Builder()
            .url("https://api.openai.com/v1/chat/completions")
            .addHeader("Authorization", "Bearer $apiKey")
            .addHeader("Content-Type", "application/json")
            .post(requestBody)
            .build()

        val response = client.newCall(request).execute()
        val rawResponse = response.body?.string() ?: return "No response"
        val json = JSONObject(rawResponse)
        return json.getJSONArray("choices")
            .getJSONObject(0)
            .getJSONObject("message")
            .getString("content")
            .trim()
    }

    private fun showScamAlertNotification() {
        val channelId = "scam_alert_channel"
        val notificationId = 1

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                return
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Scam Alert"
            val descriptionText = "Alerts for possible scam calls"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(channelId, name, importance).apply {
                description = descriptionText
            }

            val notificationManager: NotificationManager =
                requireContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        val builder = NotificationCompat.Builder(requireContext(), channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle("Scam Call Detected")
            .setContentText("Open the app to review the call.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        with(NotificationManagerCompat.from(requireContext())) {
            notify(notificationId, builder.build())
        }
    }
}
