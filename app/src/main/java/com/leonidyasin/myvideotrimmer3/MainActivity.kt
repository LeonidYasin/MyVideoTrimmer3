package com.leonidyasin.myvideotrimmer3

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.TimePicker
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.arthenica.ffmpegkit.FFmpegKit
import com.arthenica.ffmpegkit.ReturnCode
import java.io.File

class MainActivity : AppCompatActivity() {
    private lateinit var playerView: PlayerView
    private lateinit var timePickerStart: TimePicker
    private lateinit var timePickerEnd: TimePicker
    private lateinit var buttonTrimVideo: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var textStatus: TextView
    private var player: ExoPlayer? = null
    private var selectedVideoUri: Uri? = null
    private var selectedVideoPath: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initializeViews()
        setupListeners()
    }

    private fun initializeViews() {
        playerView = findViewById(R.id.playerView)
        timePickerStart = findViewById(R.id.timePickerStart)
        timePickerEnd = findViewById(R.id.timePickerEnd)
        buttonTrimVideo = findViewById(R.id.buttonTrimVideo)
        progressBar = findViewById(R.id.progressBar)
        textStatus = findViewById(R.id.textStatus)

        // Настройка TimePicker для 24-часового формата
        timePickerStart.setIs24HourView(true)
        timePickerEnd.setIs24HourView(true)

        // Инициализация ExoPlayer
        player = ExoPlayer.Builder(this).build()
        playerView.player = player
    }

    private fun setupListeners() {
        findViewById<Button>(R.id.buttonChooseVideo).setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                type = "video/*"
            }
            startForResult.launch(intent)
        }

        buttonTrimVideo.setOnClickListener {
            if (selectedVideoPath != null) {
                trimVideo()
            }
        }
    }

    private val startForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            result.data?.data?.let { uri ->
                selectedVideoUri = uri
                updateVideoPreview(uri)
                getVideoFilePath(uri)?.let { path ->
                    selectedVideoPath = path
                    buttonTrimVideo.isEnabled = true
                }
            }
        }
    }

    private fun updateVideoPreview(uri: Uri) {
        findViewById<TextView>(R.id.textVideoPath).text = "Выбранное видео: ${uri.lastPathSegment}"
        player?.let { exoPlayer ->
            val mediaItem = MediaItem.fromUri(uri)
            exoPlayer.setMediaItem(mediaItem)
            exoPlayer.prepare()
        }
    }

    private fun getVideoFilePath(uri: Uri): String? {
        val projection = arrayOf(MediaStore.Video.Media.DATA)
        val cursor = contentResolver.query(uri, projection, null, null, null)
        return cursor?.use {
            val columnIndex = it.getColumnIndexOrThrow(MediaStore.Video.Media.DATA)
            if (it.moveToFirst()) it.getString(columnIndex) else null
        }
    }

    private fun trimVideo() {
        val inputPath = selectedVideoPath ?: return
        val inputFile = File(inputPath)
        val outputPath = "${inputFile.parent}/${inputFile.nameWithoutExtension}_trim.${inputFile.extension}"

        // Получаем время начала и конца
        val startHour = timePickerStart.hour
        val startMinute = timePickerStart.minute
        val endHour = timePickerEnd.hour
        val endMinute = timePickerEnd.minute

        // Форматируем время для FFmpeg
        val startTime = String.format("%02d:%02d:00", startHour, startMinute)
        val endTime = String.format("%02d:%02d:00", endHour, endMinute)

        // Показываем прогресс
        progressBar.visibility = View.VISIBLE
        textStatus.visibility = View.VISIBLE
        textStatus.text = "Обработка видео..."
        buttonTrimVideo.isEnabled = false

        // Команда для FFmpeg
        val command = arrayOf(
            "-i", inputPath,
            "-ss", startTime,
            "-to", endTime,
            "-c", "copy",
            outputPath
        ).joinToString(" ")

        FFmpegKit.executeAsync(command) { session ->
            runOnUiThread {
                val returnCode = session.returnCode
                if (ReturnCode.isSuccess(returnCode)) {
                    textStatus.text = "Видео успешно обработано и сохранено:\n$outputPath"
                } else {
                    textStatus.text = "Ошибка при обработке видео"
                }
                progressBar.visibility = View.GONE
                buttonTrimVideo.isEnabled = true
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        player?.release()
        player = null
    }
}