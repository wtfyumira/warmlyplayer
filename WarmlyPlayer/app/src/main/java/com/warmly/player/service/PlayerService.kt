package com.warmly.player.service

import android.content.Intent
import androidx.media3.common.C
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel

class PlayerService : MediaSessionService() {
    
    private var mediaSession: MediaSession? = null
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    
    override fun onCreate() {
        super.onCreate()
        
        val player = ExoPlayer.Builder(this)
            .setHandleAudioBecomingNoisy(true)
            .setWakeMode(C.WAKE_MODE_LOCAL)
            .build()
        
        player.repeatMode = Player.REPEAT_MODE_OFF
        
        mediaSession = MediaSession.Builder(this, player).build()
    }
    
    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession
    }
    
    override fun onDestroy() {
        mediaSession?.run {
            player.release()
            release()
            mediaSession = null
        }
        serviceScope.cancel()
        super.onDestroy()
    }
    
    companion object {
        const val ACTION_PLAY_PAUSE = "com.warmly.player.ACTION_PLAY_PAUSE"
        const val ACTION_NEXT = "com.warmly.player.ACTION_NEXT"
        const val ACTION_PREVIOUS = "com.warmly.player.ACTION_PREVIOUS"
        const val ACTION_REPEAT = "com.warmly.player.ACTION_REPEAT"
    }
}
