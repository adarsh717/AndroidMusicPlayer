package com.example.musicplayer

import android.annotation.SuppressLint
import android.content.Context
import android.database.ContentObserver
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.widget.Button
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity


class MainActivity : AppCompatActivity() {

    // -----------------------------
    // Views
    // -----------------------------

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

    lateinit var songTitle: TextView
    lateinit var artistName: TextView
    lateinit var albumImage: ImageView
    lateinit var volumeText: TextView


    // -----------------------------
    // MediaPlayer
    // -----------------------------

    private var mediaPlayer: MediaPlayer? = null

    private val songList = ArrayList<Uri>()

    private var currentSongIndex = 0

    private var isUserChangingProgress = false


    // -----------------------------
    // Handler
    // -----------------------------

    private val handler = Handler(Looper.getMainLooper())


    // -----------------------------
    // Activity Result
    // -----------------------------

    private val launcher =
        registerForActivityResult(
            ActivityResultContracts.OpenMultipleDocuments()
        ) { uris ->

            if (uris.isNotEmpty()) {

                songList.clear()
                songList.addAll(uris)

                currentSongIndex = 0

                playSong(currentSongIndex)
            }
        }


    // =====================================================
    // ON CREATE
    // =====================================================

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)


        // -----------------------------
        // Find Views
        // -----------------------------

        previousBtn = findViewById(R.id.pre)
        playPauseBtn = findViewById(R.id.play_pause)
        nextBtn = findViewById(R.id.nxt)

        progress = findViewById(R.id.progressbar)
        pickSong = findViewById(R.id.btnPickSong)

        dlbbtn = findViewById(R.id.dolbyBtn)

        leftspk = findViewById(R.id.volumeUp)
        rigthspk = findViewById(R.id.volumeDown)

        seekbar = findViewById(R.id.volumeSeeker)

        currentTime = findViewById(R.id.currenttime)
        maxTime = findViewById(R.id.maxtime)

        songTitle = findViewById(R.id.songTitle)
        artistName = findViewById(R.id.artistName)

        albumImage = findViewById(R.id.albumImage)

        volumeText = findViewById(R.id.volumeText)


        // =====================================================
        // PICK SONG
        // =====================================================

        pickSong.setOnClickListener {

            launcher.launch(arrayOf("audio/*"))

        }


        // =====================================================
        // PLAY / PAUSE
        // =====================================================

        playPauseBtn.setOnClickListener {

            if (mediaPlayer == null) {

                // Agar koi song load nahi hai
                if (songList.isNotEmpty()) {

                    playSong(currentSongIndex)

                }

            } else {

                if (mediaPlayer!!.isPlaying) {

                    mediaPlayer!!.pause()

                    playPauseBtn.text = "▶"

                } else {

                    mediaPlayer!!.start()

                    playPauseBtn.text = "Ⅱ"

                }

            }

        }


        // =====================================================
        // PREVIOUS
        // =====================================================

        previousBtn.setOnClickListener {

            if (songList.isEmpty()) {
                return@setOnClickListener
            }


            if (currentSongIndex > 0) {

                currentSongIndex--

            } else {

                // Last song par chale jao

                currentSongIndex = songList.size - 1

            }


            playSong(currentSongIndex)

        }


        // =====================================================
        // NEXT
        // =====================================================

        nextBtn.setOnClickListener {

            if (songList.isEmpty()) {
                return@setOnClickListener
            }


            if (currentSongIndex < songList.size - 1) {

                currentSongIndex++

            } else {

                // Last ke baad first song

                currentSongIndex = 0

            }


            playSong(currentSongIndex)

        }


        // =====================================================
        // MUSIC SEEK BAR
        // =====================================================

        progress.setOnSeekBarChangeListener(

            object : SeekBar.OnSeekBarChangeListener {

                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progressValue: Int,
                    fromUser: Boolean
                ) {

                    if (fromUser) {

                        mediaPlayer?.seekTo(progressValue)

                        currentTime.text =
                            timeFormat(progressValue)

                    }

                }


                override fun onStartTrackingTouch(
                    seekBar: SeekBar?
                ) {

                    isUserChangingProgress = true

                }


                override fun onStopTrackingTouch(
                    seekBar: SeekBar?
                ) {

                    isUserChangingProgress = false

                }

            }
        )


        // =====================================================
        // AUDIO MANAGER
        // =====================================================

        audioManager =
            getSystemService(Context.AUDIO_SERVICE) as AudioManager


        // Maximum volume

        val maxVolume =
            audioManager.getStreamMaxVolume(
                AudioManager.STREAM_MUSIC
            )


        // Current volume

        val currentVolume =
            audioManager.getStreamVolume(
                AudioManager.STREAM_MUSIC
            )


        seekbar.max = maxVolume

        seekbar.progress = currentVolume


        // =====================================================
        // VOLUME SEEK BAR
        // =====================================================

        seekbar.setOnSeekBarChangeListener(

            object : SeekBar.OnSeekBarChangeListener {

                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progressValue: Int,
                    fromUser: Boolean
                ) {

                    if (fromUser) {

                        audioManager.setStreamVolume(

                            AudioManager.STREAM_MUSIC,

                            progressValue,

                            AudioManager.FLAG_SHOW_UI

                        )

                    }

                }


                override fun onStartTrackingTouch(
                    seekBar: SeekBar?
                ) {

                }


                override fun onStopTrackingTouch(
                    seekBar: SeekBar?
                ) {

                }

            }
        )


        // =====================================================
        // VOLUME UP
        // =====================================================

        leftspk.setOnClickListener {

            audioManager.adjustVolume(

                AudioManager.ADJUST_RAISE,

                AudioManager.FLAG_SHOW_UI

            )

            updateVolumeSeekBar()

        }


        // =====================================================
        // VOLUME DOWN
        // =====================================================

        rigthspk.setOnClickListener {

            audioManager.adjustVolume(

                AudioManager.ADJUST_LOWER,

                AudioManager.FLAG_SHOW_UI

            )

            updateVolumeSeekBar()

        }


        // =====================================================
        // DOLBY
        // =====================================================

        var dolbyOn = false


        dlbbtn.setOnClickListener {

            dolbyOn = !dolbyOn


            if (dolbyOn) {

                dlbbtn.text = "Dolby ON"

            } else {

                dlbbtn.text = "Dolby"

            }

        }


        // =====================================================
        // VOLUME OBSERVER
        // =====================================================

        observer =
            object : ContentObserver(
                Handler(Looper.getMainLooper())
            ) {

                override fun onChange(
                    selfChange: Boolean
                ) {

                    super.onChange(selfChange)

                    updateVolumeSeekBar()

                }

            }


        // Start updating UI

        handler.post(updateProgressRunnable)

    }


    // =====================================================
    // PLAY SONG
    // =====================================================

    private fun playSong(index: Int) {

        if (songList.isEmpty()) {
            return
        }


        if (index < 0 || index >= songList.size) {
            return
        }


        currentSongIndex = index


        // Old MediaPlayer release

        mediaPlayer?.release()

        mediaPlayer = null


        try {

            mediaPlayer = MediaPlayer()


            mediaPlayer?.setDataSource(
                this,
                songList[index]
            )


            mediaPlayer?.setOnPreparedListener {

                // Duration

                progress.max = it.duration


                maxTime.text =
                    timeFormat(it.duration)


                currentTime.text =
                    timeFormat(0)


                // Play

                it.start()


                playPauseBtn.text = "Ⅱ"


                // Song information

                updateSongInformation(index)

            }


            // When song finishes

            mediaPlayer?.setOnCompletionListener {

                playNextSong()

            }


            mediaPlayer?.setOnErrorListener { _, _, _ ->

                playPauseBtn.text = "▶"

                true

            }


            mediaPlayer?.prepareAsync()

        } catch (e: Exception) {

            e.printStackTrace()

        }

    }


    // =====================================================
    // NEXT SONG
    // =====================================================

    private fun playNextSong() {

        if (songList.isEmpty()) {
            return
        }


        if (currentSongIndex < songList.size - 1) {

            currentSongIndex++

        } else {

            currentSongIndex = 0

        }


        playSong(currentSongIndex)

    }


    // =====================================================
    // SONG INFORMATION
    // =====================================================

    @SuppressLint("Range")
    private fun updateSongInformation(index: Int) {

        val uri = songList[index]


        // Simple name

        val cursor = contentResolver.query(

            uri,

            arrayOf(
                android.provider.MediaStore.Audio.Media.DISPLAY_NAME
            ),

            null,
            null,
            null

        )


        if (cursor != null) {

            if (cursor.moveToFirst()) {

                val name = cursor.getString(

                    cursor.getColumnIndex(
                        android.provider.MediaStore.Audio.Media.DISPLAY_NAME
                    )

                )


                songTitle.text =
                    name.removeSuffix(".mp3")
                        .removeSuffix(".m4a")
                        .removeSuffix(".wav")
                        .removeSuffix(".flac")

            }


            cursor.close()

        } else {

            songTitle.text = "Unknown Song"

        }


        artistName.text = "Local Music"


        // Default album image


    }


    // =====================================================
    // PROGRESS UPDATE
    // =====================================================

    private val updateProgressRunnable =
        object : Runnable {

            override fun run() {

                mediaPlayer?.let {

                    if (!isUserChangingProgress) {

                        if (it.isPlaying) {

                            progress.progress =
                                it.currentPosition


                            currentTime.text =
                                timeFormat(
                                    it.currentPosition
                                )


                            maxTime.text =
                                timeFormat(
                                    it.duration
                                )

                        }

                    }

                }


                handler.postDelayed(
                    this,
                    500
                )

            }

        }


    // =====================================================
    // UPDATE VOLUME
    // =====================================================

    private fun updateVolumeSeekBar() {

        val currentVolume =
            audioManager.getStreamVolume(
                AudioManager.STREAM_MUSIC
            )


        seekbar.progress =
            currentVolume

    }


    // =====================================================
    // TIME FORMAT
    // =====================================================

    @SuppressLint("DefaultLocale")
    private fun timeFormat(ms: Int): String {

        val minutes =
            ms / 1000 / 60


        val seconds =
            (ms / 1000) % 60


        return String.format(
            "%02d:%02d",
            minutes,
            seconds
        )

    }


    // =====================================================
    // OBSERVER
    // =====================================================

    override fun onStart() {

        super.onStart()


        contentResolver.registerContentObserver(

            Settings.System.CONTENT_URI,

            true,

            observer

        )

    }


    override fun onStop() {

        super.onStop()


        contentResolver.unregisterContentObserver(
            observer
        )

    }


    // =====================================================
    // DESTROY
    // =====================================================

    override fun onDestroy() {

        super.onDestroy()


        handler.removeCallbacks(
            updateProgressRunnable
        )


        mediaPlayer?.release()

        mediaPlayer = null

    }

}