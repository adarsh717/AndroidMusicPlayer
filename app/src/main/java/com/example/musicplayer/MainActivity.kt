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
        music = MediaPlayer.create(this, R.raw.music2)


        start = findViewById(R.id.startBtn)
        stop = findViewById(R.id.stopBtn)

        volumeUpbtn = findViewById(R.id.volumeUp)
        volumeDownbtn = findViewById(R.id.volumeDown)
        seekBar = findViewById(R.id.volumeSeeker)
        dolby = findViewById(R.id.dolbyBtn)
        Progress = findViewById(R.id.progressbar)

        Progress.max = music.duration

        music.isLooping = true

        start.setOnClickListener {
            if (music.isPlaying) {
                music.pause()
            } else {
                music.start()
            }

        }

        stop.setOnClickListener {
            if (::music.isInitialized) {
                music.pause()
                music.seekTo(0)
            }
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

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                audioManager.setStreamVolume(
                    AudioManager.STREAM_MUSIC,
                    progress,
                    AudioManager.FLAG_SHOW_UI
                )
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        var handler = Handler(Looper.getMainLooper())

        var runnable = object : Runnable {
            override fun run() {
                val currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
                seekBar.progress = currentVolume
                handler.postDelayed(this, 10)
            }
        }
        handler.post(runnable)

        val handlerPos = Handler(Looper.getMainLooper())
        val runnable2 = object : Runnable {
            override fun run() {
                val currentPosition = music.currentPosition
                Progress.progress = currentPosition
                handlerPos.postDelayed(this, 200)
            }
        }
        handlerPos.post(runnable2)

    }


//    fun playMusic(){
//        music.start()
//    }
    }



