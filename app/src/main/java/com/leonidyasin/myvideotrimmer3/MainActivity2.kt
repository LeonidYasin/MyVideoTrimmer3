package com.leonidyasin.myvideotrimmer3


import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.leonidyasin.myvideotrimmer3.R.id
import com.leonidyasin.myvideotrimmer3.R.layout
//import com.arthenica.ffmpegkit.FFmpegKit.*
//import com.arthenica.ffmpegkit.FFmpegKitConfig.*
//import com.arthenica.mobileffmpeg.FFmpeg


import android.widget.TimePicker
import com.arthenica.ffmpegkit.FFmpegKit
import com.arthenica.ffmpegkit.FFmpegKitConfig
import com.arthenica.ffmpegkit.ReturnCode

class MainActivity2 : AppCompatActivity() {

    private lateinit var playerView: PlayerView

    private lateinit var timePickerStart: TimePicker
    private lateinit var timePickerEnd: TimePicker

    //private lateinit var binding: ActivityMainBinding
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(layout.activity_main2)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val buttonChooseFile: Button = findViewById(id.button1)
        val textViewPath: TextView = findViewById(id.textView)

        buttonChooseFile.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "*/*" // Allow selecting any file type
            startForResult.launch(intent) // Use startForResult to launch the activity

        }

        //playerView = findViewById(R.id.player_view)
        initializePlayer()


        //timePickerStart = findViewById(R.id.timePickerStart)
        //timePickerEnd = findViewById(R.id.timePickerEnd)

        // Инициализация ExoPlayer и установка источника видео

        // Обработчики событий для TimePicker, чтобы получить выбранное время
        //timePickerStart.setOnTimeChangedListener { _, hour, minute ->
        // Обработка выбранного времени начала
        //}
        //timePickerEnd.setOnTimeChangedListener { _, hour, minute ->
        // Обработка выбранного времени конца
        //}
    }

    // Create an instance of ActivityResultLauncher using registerForActivityResult
    private val startForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            result.data?.data?.let { uri ->
                // Handle the selected file URI
                val textViewPath: TextView = findViewById(id.editTextText)
                textViewPath.text = "Selected file: $uri"

                playerView = findViewById(R.id.playerView)
                // Instantiate the player.
                val player = ExoPlayer.Builder(this).build()
// Attach player to the view.
                playerView.player = player
// Set the media item to be played.
                val mediaItem = MediaItem.fromUri(uri)
                player.setMediaItem(mediaItem)
// Prepare the player.
                player.prepare()

                /*  // Пример использования:
                  val inputVideo = "/path/to/input.mp4" // Путь к входному видео
                  val outputVideo = "/path/to/output.mp4" // Путь для сохранения обрезанного видео

                  trimVideo(inputVideo, outputVideo)*/
            }
        }
    }

    private fun initializePlayer() {
        /*  val player = SimpleExoPlayer.Builder(this).build()
          playerView.player = player

          val mediaItem = MediaItem.fromUri("https://your_video_url")
          player.setMediaItem(mediaItem)
          player.prepare()
          player.play()*/
    }





// Assuming LogHelper is defined elsewhere. If not, replace with appropriate logging.
// Example: import android.util.Log
// Or if you're using Timber: import timber.log.Timber

    object FFmpeg { // Consider placing this in an object for better organization
        const val TAG = "FFmpeg"
    }

    fun trimVideo(inputPath: String, outputPath: String) {
        val startTime = "00:01:00"
        val endTime = "00:02:00"

        // More robust command construction to handle potential issues with paths containing spaces.
        val command = arrayOf(
            "-i", inputPath,
            "-ss", startTime,
            "-to", endTime,
            "-c", "copy",
            outputPath
        )

        FFmpegKit.executeAsync(command.joinToString(" ")) { session -> // Use session instead of individual parameters
            val returnCode = session.returnCode
            if (ReturnCode.isSuccess(returnCode)) { // Use isSuccess for clarity
                Log.i(FFmpeg.TAG, "Command execution completed successfully.")
            } else {
                Log.e(FFmpeg.TAG, "Command execution failed with rc=${returnCode?.value}.") // Handle potential null return code
                val output = session.output
                if (output != null) {
                    Log.e(FFmpeg.TAG, "FFmpeg output:\n$output")
                }
                val logs = session.logs
                if (logs != null) {
                    logs.forEach { log ->
                        Log.e(FFmpeg.TAG, "FFmpeg log: ${log.message}")
                    }
                }
            }
        }
    }
}