package com.example.musicplayer
import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.database.ContentObserver
import android.media.AudioManager
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.widget.Button
import android.widget.SeekBar
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
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
    lateinit var leftspk: Button
    lateinit var rigthspk: Button
    lateinit var audioManager: AudioManager
    lateinit var seekbar: SeekBar
    lateinit var observer: ContentObserver
    lateinit var currentTime: TextView
    lateinit var maxTime: TextView


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
        dlbbtn = findViewById(R.id.dolbyBtn)
        leftspk = findViewById(R.id.volumeUp)
        rigthspk = findViewById(R.id.volumeDown)
        seekbar = findViewById(R.id.volumeSeeker)
        currentTime=findViewById(R.id.currenttime)
        maxTime=findViewById(R.id.maxtime)



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

        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager

        observer = object : ContentObserver(Handler(Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean) {
                super.onChange(selfChange)
                val currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
                seekbar.progress = currentVolume
            }
        }





        val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        val currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)

        seekbar.max=maxVolume
        seekbar.progress=currentVolume

        seekbar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(
                seekBar: SeekBar?,
                progress: Int,
                fromUser: Boolean
            ) {
                if(fromUser){
                    audioManager.setStreamVolume(
                        AudioManager.STREAM_MUSIC,
                        progress,
                        AudioManager.FLAG_SHOW_UI
                    )
                }
            }


            override fun onStartTrackingTouch(seekBar: SeekBar?) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {

            }

        })

        val handler = Handler(Looper.getMainLooper())
        val runnable = object : Runnable {
            override fun run() {


                val current=musicService?.getCurrentPosition()
                if (current != null) {

                    progress.progress=current
                }

                current?.let {
                    currentTime.text=timeformat(it)
                }
                maxTime.text=timeformat(progress.max)
                if (musicService?.isPlaying() == true) {
                    progress.max = musicService?.getDuration() ?: 0
                    progress.progress = musicService?.getCurrentPosition() ?: 0
                }
                handler.postDelayed(this, 10)
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
    override fun onStart() {
        super.onStart()
        contentResolver.registerContentObserver(
            android.provider.Settings.System.CONTENT_URI,
            true,
            observer
        )
    }

    override fun onStop() {
        super.onStop()
        contentResolver.unregisterContentObserver(observer)
    }

    @SuppressLint("DefaultLocale")
    fun timeformat(ms: Int): String{
        val minute = ms/1000/60
        val second = (ms/(1000)%60)
        return String.format("%02d:%02d",minute,second)
    }
}
