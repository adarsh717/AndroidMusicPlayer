package com.example.musicplayer
import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import android.widget.Button
import android.widget.SeekBar
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.Runnable
class MainActivity : AppCompatActivity() {
    private var musicService: MusicService? = null
    private var isBound = false
    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as MusicService.MyBinder
            musicService = binder.getService()
            isBound = true
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            isBound = false
        }
    }

    lateinit var previousBtn: Button
    lateinit var playPauseBtn: Button
    lateinit var nextBtn: Button
    lateinit var progress: SeekBar
    lateinit var pickSong: Button

    lateinit var dlbbtn: Button
    lateinit var leftspk : Button
    lateinit var rigthspk: Button

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bindService(
            Intent(this, MusicService::class.java),
            connection,
            BIND_AUTO_CREATE
        )

        previousBtn = findViewById(R.id.pre)
        playPauseBtn = findViewById(R.id.play_pause)
        nextBtn = findViewById(R.id.nxt)
        progress = findViewById(R.id.progressbar)
        pickSong = findViewById(R.id.btnPickSong)
        dlbbtn=findViewById(R.id.dolbyBtn)
        leftspk=findViewById(R.id.volumeUp)
        rigthspk=findViewById(R.id.volumeDown)


        dlbbtn.setOnClickListener {
            musicService?.dolby()
        }
        leftspk.setOnClickListener {
            musicService?.left()
        }
        rigthspk.setOnClickListener {
            musicService?.right()
        }

        previousBtn.setOnClickListener {
            musicService?.playPrevious()
        }

        playPauseBtn.setOnClickListener {
            musicService?.playPause()
        }

        nextBtn.setOnClickListener {

            musicService?.playNext()
        }


        pickSong.setOnClickListener {
            launcher.launch(arrayOf("audio/*"))
        }


        progress.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(sb: SeekBar?, value: Int, fromUser: Boolean) {
                if (fromUser) {
                    musicService?.seekTo(value)
                }
            }

            override fun onStartTrackingTouch(sb: SeekBar?) {}
            override fun onStopTrackingTouch(sb: SeekBar?) {}
        })


        val handler = Handler(Looper.getMainLooper())
        val runnable = object : Runnable {
            override fun run() {
                if (musicService?.isPlaying() == true) {
                    progress.max = musicService?.getDuration() ?: 0
                    progress.progress = musicService?.getCurrentPosition() ?: 0
                }
                handler.postDelayed(this, 500)
            }
        }
        handler.post(runnable)
    }

    private val launcher =
        registerForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { uris ->

            if (uris.isNotEmpty()) {
                val list = ArrayList(uris)
                musicService?.setPlaylist(list, 0)
                musicService?.playSong(0)
            }
        }

    override fun onDestroy() {
        super.onDestroy()
        if (isBound) {
            unbindService(connection)
            isBound = false
        }
    }
}