package com.example.musicplayer

import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.Binder
import android.os.IBinder
import android.util.Log


    class MusicService : Service() {

        private var mediaPlayer: MediaPlayer? = null
        private val songList = ArrayList<Uri>()
        private var currentIndex = 0

        inner class MyBinder : Binder() {
            fun getService(): MusicService = this@MusicService
        }

        private val binder = MyBinder()

        override fun onBind(intent: Intent?): IBinder {
            return binder
        }

        fun setPlaylist(list: ArrayList<Uri>, startIndex: Int = 0) {
            songList.clear()
            songList.addAll(list)
            currentIndex = startIndex
        }

        fun playSong(index: Int) {
            if (songList.isEmpty()) return

            mediaPlayer?.reset()
            mediaPlayer?.release()
            mediaPlayer=null

            currentIndex = index

            Log.d("DEBUG", "Playing index: $index")
            Log.d("DEBUG", "URI: ${songList[index]}")

            mediaPlayer = MediaPlayer.create(this, songList[index])
            mediaPlayer?.start()

            mediaPlayer?.setOnCompletionListener {
                playNext()
            }
        }

        fun playNext() {
            if (songList.isEmpty()) return

            currentIndex =
                if (currentIndex < songList.size - 1) currentIndex + 1 else 0

            playSong(currentIndex)
        }

        fun playPrevious() {
            if (songList.isEmpty()) return

            currentIndex =
                if (currentIndex > 0) currentIndex - 1 else songList.size - 1

            playSong(currentIndex)
        }

        fun playPause() {
            mediaPlayer?.let {
                if (it.isPlaying) it.pause() else it.start()
            }
        }

        fun seekTo(position: Int) {
            mediaPlayer?.seekTo(position)
        }

        fun getCurrentPosition(): Int {
            return mediaPlayer?.currentPosition ?: 0
        }

        fun getDuration(): Int {
            return mediaPlayer?.duration ?: 0
        }

        fun isPlaying(): Boolean {
            return mediaPlayer?.isPlaying ?: false
        }

    fun dolby(){
        mediaPlayer?.setVolume(1.0f,1.0f)
    }
    fun left(){
        mediaPlayer?.setVolume(1.0f,0.0f)
    }

    fun right(){
        mediaPlayer?.setVolume(0.0f,1.0f)
    }

        override fun onDestroy() {
            super.onDestroy()
            mediaPlayer?.release()
        }

    }