package com.example.musicplayer
import android.annotation.SuppressLint
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.ProgressBar
import android.widget.SeekBar
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.registerForActivityResult
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import kotlinx.coroutines.Runnable


class MainActivity : AppCompatActivity() {

    lateinit var start: Button
    lateinit var stop: Button
    lateinit var volumeUpbtn: Button
    lateinit var volumeDownbtn: Button

    lateinit var dolby: Button

    lateinit var seekBar: SeekBar

    lateinit var pickSong: Button

    lateinit var Progress: SeekBar
    lateinit var music: MediaPlayer

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }



        start = findViewById(R.id.startBtn)
        stop = findViewById(R.id.stopBtn)

        volumeUpbtn = findViewById(R.id.volumeUp)
        volumeDownbtn = findViewById(R.id.volumeDown)
        seekBar = findViewById(R.id.volumeSeeker)
        dolby = findViewById(R.id.dolbyBtn)
        Progress = findViewById(R.id.progressbar)
        pickSong = findViewById(R.id.btnPickSong)



        start.setOnClickListener {
            if (::music.isInitialized) {
                if (music.isPlaying) {
                    music.pause()
                } else {
                    music.start()
                }
            }
        }

        stop.setOnClickListener {
            if (::music.isInitialized) {
                music.pause()
                music.seekTo(0)
            }
            Progress.progress=0
        }

        val audioManager = getSystemService(AUDIO_SERVICE) as AudioManager
        val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        seekBar.max = maxVolume
        val currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
        seekBar.progress = currentVolume

        volumeUpbtn.setOnClickListener {
//
//            if (currentVolume < maxVolume){
//                currentVolume+=18
//
//                audioManager.setStreamVolume(
//                    AudioManager.STREAM_MUSIC,
//                    currentVolume,
//                    AudioManager.FLAG_SHOW_UI
//                )
//            }
            music.setVolume(1.0f, 0.0f)
        }

        volumeDownbtn.setOnClickListener {

//            if (currentVolume > 0){
//                currentVolume-=18
//                audioManager.setStreamVolume(
//                    AudioManager.STREAM_MUSIC,
//                    currentVolume,
//                    AudioManager.FLAG_SHOW_UI
//                )
//            }
            music.setVolume(0.0f, 1.0f)
        }

        dolby.setOnClickListener {
            music.setVolume(1.0f, 1.0f)
        }



        val launcher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->

            if (uri != null) {

                if (::music.isInitialized) {
                    music.release()
                }

                music = MediaPlayer()

                music.setOnPreparedListener {
                    Progress.max = it.duration
                    it.isLooping = true
                    it.start()
                }

                music.setDataSource(this, uri)
                music.prepareAsync()
            }
        }
        pickSong.setOnClickListener {
            launcher.launch("audio/*")
        }



        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                    audioManager.setStreamVolume(
                        AudioManager.STREAM_MUSIC,
                        progress,
                        AudioManager.FLAG_SHOW_UI
                    )

                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {}

                override fun onStopTrackingTouch(seekBar: SeekBar?) {}
            })

            Progress.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                    if (fromUser) {
                        music.seekTo(progress)
                    }
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {

                }

                override fun onStopTrackingTouch(seekBar: SeekBar?) {

                }
            })

            var handler = Handler(Looper.getMainLooper())

            var runnable = object : Runnable {
                override fun run() {
                    val currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
                    seekBar.progress = currentVolume
                    handler.postDelayed(this, 200)
                }
            }
            handler.post(runnable)

            val handlerPos = Handler(Looper.getMainLooper())
            val runnable2 = object : Runnable {
                override fun run() {
                    if (::music.isInitialized && music.isPlaying) {
                        val currentPosition = music.currentPosition
                        Progress.progress = currentPosition
                    }
                    handlerPos.postDelayed(this, 200)
                }
            }
            handlerPos.post(runnable2)

        }


//    fun playMusic(){
//        music.start()
//    }
    }



