package com.example.tempnavigation.alarms

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.os.Looper

class AlarmMediaPlayer(private val context:Context) {
    private val mediaPlayer = MediaPlayer()
    private val handler = android.os.Handler(Looper.getMainLooper())

    fun prepareMediaPlayer(rawResourceId:Int,packageName:String){
        mediaPlayer.apply {
            reset()
            setDataSource(context, getSoundUri(rawResourceId, packageName))
            setOnPreparedListener{mp ->
                mp.start()
            }
            setOnErrorListener{mp,what,extra ->
                false
            }
            setAudioAttributes(createAudioAttributes())
            prepareAsync()
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
        mediaPlayer.start()
    }

    fun stopMediaPlayer() {
        mediaPlayer.stop()
    }

    fun releaseMediaPlayer() {
        mediaPlayer.release()
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

    fun postDelayed(delayMillis: Long, action: () -> Unit) {
        handler.postDelayed(action, delayMillis)
    }

    fun removeCallbacks(action: () -> Unit) {
        handler.removeCallbacksAndMessages(null)
    }
}