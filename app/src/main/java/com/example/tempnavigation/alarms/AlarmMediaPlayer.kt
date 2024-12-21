package com.example.tempnavigation.alarms

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.os.Looper
import android.util.Log

class AlarmMediaPlayer(private val context:Context) {
    private val mediaPlayer = MediaPlayer()
    //private val handler = android.os.Handler(Looper.getMainLooper())

    private enum class PlayerState { IDLE, PREPARED, PLAYING, STOPPED, RELEASED }
    private var playerState = PlayerState.IDLE

    fun prepareMediaPlayer(rawResourceId:Int,packageName:String){
        mediaPlayer.apply {
            reset()
            setDataSource(context, getSoundUri(rawResourceId, packageName))
            setOnPreparedListener{mp ->
                playerState = PlayerState.PREPARED
                mp.start()
                playerState = PlayerState.PLAYING
            }
            setOnErrorListener{mp,what,extra ->
                Log.e("AlarmMediaPlayer", "MediaPlayer Error: what=$what extra=$extra")
                releaseMediaPlayer()
                true
            }
            setAudioAttributes(createAudioAttributes())
            prepareAsync()
            playerState = PlayerState.PREPARED
        }
    }
    private fun getSoundUri(rawResourceId:Int,packageName:String): Uri {
        return  Uri.parse("android.resource://${packageName}/raw/${rawResourceId}")
    }
    private fun createAudioAttributes(): AudioAttributes {
        return AudioAttributes.Builder()
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .setUsage(AudioAttributes.USAGE_ALARM)
            .build()
    }
    fun startMediaPlayer() {
        if(playerState == PlayerState.PREPARED || playerState ==  PlayerState.STOPPED) {
            mediaPlayer.start()
            playerState = PlayerState.PLAYING
        }
    }

    fun stopMediaPlayer() {
        if (playerState == PlayerState.PLAYING) {
            mediaPlayer.stop()
            playerState = PlayerState.STOPPED
        }
    }

    fun releaseMediaPlayer() {
        mediaPlayer.release()
        playerState = PlayerState.RELEASED
    }

    fun setOnCompletionListener(listener: MediaPlayer.OnCompletionListener) {
        mediaPlayer.setOnCompletionListener{mp->
            listener.onCompletion(mp)
            releaseMediaPlayer()
        }
    }

    fun setLooping(isLooping: Boolean) {
        mediaPlayer.isLooping = isLooping
    }

    fun setVolume(leftVolume: Float, rightVolume: Float) {
        mediaPlayer.setVolume(leftVolume, rightVolume)
    }

    fun isPlaying(): Boolean {
        return mediaPlayer.isPlaying
    }

    fun getCurrentPosition(): Int {
        return mediaPlayer.currentPosition
    }

    fun seekTo(position: Int) {
        mediaPlayer.seekTo(position)
    }

    fun setWakeMode(mode: Int) {
        mediaPlayer.setWakeMode(context, mode)
    }

    fun setAudioAttributes(attributes: AudioAttributes) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            mediaPlayer.setAudioAttributes(attributes)
        }
    }

    fun setOnErrorListener(listener: MediaPlayer.OnErrorListener) {
        mediaPlayer.setOnErrorListener(listener)
    }

    /*fun postDelayed(delayMillis: Long, action: () -> Unit) {
        handler.postDelayed(action, delayMillis)
    }*/
}