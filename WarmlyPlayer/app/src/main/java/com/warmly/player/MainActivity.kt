package com.warmly.player

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.media3.common.MediaItem as ExoMediaItem
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import androidx.core.content.ContextCompat.startForegroundService
import android.content.Intent
import com.google.common.util.concurrent.MoreExecutors
import com.warmly.player.data.model.FavoriteEntity
import com.warmly.player.data.model.MediaItem
import com.warmly.player.service.PlayerService
import com.warmly.player.ui.screens.MediaLibraryScreen
import com.warmly.player.ui.screens.PlayerScreen
import com.warmly.player.ui.theme.WarmlyPlayerTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

class MainActivity : ComponentActivity() {
    
    private var mediaController: MediaController? = null
    private val allMediaItems = mutableStateListOf<MediaItem>()
    private var currentMediaItem by mutableStateOf<MediaItem?>(null)
    private var isPlaying by mutableStateOf(false)
    private var repeatMode by mutableStateOf(Player.REPEAT_MODE_OFF)
    private var showFavoritesOnly by mutableStateOf(false)
    private var filterAudioOnly by mutableStateOf(false)
    private var filterVideoOnly by mutableStateOf(false)
    private var showPlayerScreen by mutableStateOf(false)
    
    private val requiredPermissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        arrayOf(
            Manifest.permission.READ_MEDIA_AUDIO,
            Manifest.permission.READ_MEDIA_VIDEO
        )
    } else {
        arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
    }
    
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.all { it.value }
        if (allGranted) {
            loadMediaFiles()
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Start the player service for background playback
        val serviceIntent = Intent(this, PlayerService::class.java)
        ContextCompat.startForegroundService(this, serviceIntent)
        
        checkPermissionsAndLoadMedia()
        
        setContent {
            WarmlyPlayerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.Transparent
                ) {
                    if (showPlayerScreen && currentMediaItem != null) {
                        PlayerScreen(
                            currentMediaItem = currentMediaItem,
                            player = mediaController,
                            isPlaying = isPlaying,
                            repeatMode = repeatMode,
                            onPlayPause = { togglePlayPause() },
                            onNext = { playNext() },
                            onPrevious = { playPrevious() },
                            onToggleRepeat = { toggleRepeatMode() },
                            onToggleFavorite = { currentMediaItem?.let { toggleFavorite(it) } },
                            onBackClick = { showPlayerScreen = false }
                        )
                    } else {
                        MainScreen(
                            mediaItems = allMediaItems,
                            showFavoritesOnly = showFavoritesOnly,
                            filterAudioOnly = filterAudioOnly,
                            filterVideoOnly = filterVideoOnly,
                            onFilterChange = { audio, video, favorites ->
                                filterAudioOnly = audio
                                filterVideoOnly = video
                                showFavoritesOnly = favorites
                            },
                            onMediaClick = { media ->
                                currentMediaItem = media
                                showPlayerScreen = true
                                playMedia(media)
                            },
                            onToggleFavorite = { toggleFavorite(it) }
                        )
                    }
                }
            }
        }
    }
    
    override fun onStart() {
        super.onStart()
        
        val sessionToken = SessionToken(this, android.content.ComponentName(this, PlayerService::class.java))
        val controllerFuture = MediaController.Builder(this, sessionToken).buildAsync()
        
        controllerFuture.addListener({
            mediaController = controllerFuture.get()
            
            // Observe player state
            mediaController?.addListener(object : Player.Listener {
                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    this@MainActivity.isPlaying = isPlaying
                }
                
                override fun onRepeatModeChanged(repeatMode: Int) {
                    this@MainActivity.repeatMode = repeatMode
                }
            })
            
        }, MoreExecutors.directExecutor())
    }
    
    override fun onStop() {
        super.onStop()
        mediaController?.release()
    }
    
    private fun checkPermissionsAndLoadMedia() {
        val missingPermissions = requiredPermissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }
        
        if (missingPermissions.isEmpty()) {
            loadMediaFiles()
        } else {
            permissionLauncher.launch(missingPermissions.toTypedArray())
        }
    }
    
    private fun loadMediaFiles() {
        val mediaItems = mutableListOf<MediaItem>()
        
        // Load audio files
        val audioUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val audioProjection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.MIME_TYPE
        )
        
        contentResolver.query(audioUri, audioProjection, null, null, null)?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            val mimeTypeColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.MIME_TYPE)
            
            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val title = cursor.getString(titleColumn)
                val artist = cursor.getString(artistColumn)
                val duration = cursor.getLong(durationColumn)
                val mimeType = cursor.getString(mimeTypeColumn)
                val uri = Uri.withAppendedPath(audioUri, id.toString()).toString()
                
                mediaItems.add(
                    MediaItem(
                        id = id,
                        title = title,
                        artist = artist,
                        duration = duration,
                        uri = uri,
                        mimeType = mimeType,
                        isFavorite = false
                    )
                )
            }
        }
        
        // Load video files
        val videoUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        val videoProjection = arrayOf(
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.TITLE,
            MediaStore.Video.Media.DURATION,
            MediaStore.Video.Media.MIME_TYPE
        )
        
        contentResolver.query(videoUri, videoProjection, null, null, null)?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
            val titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.TITLE)
            val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION)
            val mimeTypeColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.MIME_TYPE)
            
            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val title = cursor.getString(titleColumn)
                val duration = cursor.getLong(durationColumn)
                val mimeType = cursor.getString(mimeTypeColumn)
                val uri = Uri.withAppendedPath(videoUri, id.toString()).toString()
                
                mediaItems.add(
                    MediaItem(
                        id = id,
                        title = title,
                        artist = null,
                        duration = duration,
                        uri = uri,
                        mimeType = mimeType,
                        isFavorite = false
                    )
                )
            }
        }
        
        allMediaItems.clear()
        allMediaItems.addAll(mediaItems)
    }
    
    private fun playMedia(mediaItem: MediaItem) {
        mediaController?.run {
            setMediaItem(ExoMediaItem.fromUri(Uri.parse(mediaItem.uri)))
            prepare()
            play()
        }
    }
    
    private fun togglePlayPause() {
        mediaController?.run {
            if (isPlaying) pause() else play()
        }
    }
    
    private fun playNext() {
        mediaController?.seekToNext()
    }
    
    private fun playPrevious() {
        mediaController?.seekToPrevious()
    }
    
    private fun toggleRepeatMode() {
        mediaController?.run {
            repeatMode = when (repeatMode) {
                Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ALL
                Player.REPEAT_MODE_ALL -> Player.REPEAT_MODE_ONE
                Player.REPEAT_MODE_ONE -> Player.REPEAT_MODE_OFF
                else -> Player.REPEAT_MODE_OFF
            }
        }
    }
    
    private fun toggleFavorite(mediaItem: MediaItem) {
        val index = allMediaItems.indexOfFirst { it.id == mediaItem.id }
        if (index != -1) {
            val updatedItem = mediaItem.copy(isFavorite = !mediaItem.isFavorite)
            allMediaItems[index] = updatedItem
            currentMediaItem = if (currentMediaItem?.id == mediaItem.id) updatedItem else currentMediaItem
            
            // Save to database
            lifecycleScope.launch {
                if (updatedItem.isFavorite) {
                    (application as WarmlyPlayerApplication).favoriteRepository.addFavorite(updatedItem)
                } else {
                    (application as WarmlyPlayerApplication).favoriteRepository.removeFavorite(updatedItem.id)
                }
            }
        }
    }
}

@Composable
fun MainScreen(
    mediaItems: List<MediaItem>,
    showFavoritesOnly: Boolean,
    filterAudioOnly: Boolean,
    filterVideoOnly: Boolean,
    onFilterChange: (Boolean, Boolean, Boolean) -> Unit,
    onMediaClick: (MediaItem) -> Unit,
    onToggleFavorite: (MediaItem) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
    ) {
        // Header
        Text(
            text = "Warmly Player",
            color = Color(0xFFF1F5F9),
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(16.dp)
        )
        
        // Filter tabs
        ScrollableTabRow(
            selectedTabIndex = if (showFavoritesOnly) 3 else if (filterVideoOnly) 2 else if (filterAudioOnly) 1 else 0,
            containerColor = Color.Transparent,
            contentColor = Color(0xFFF1F5F9),
            modifier = Modifier.padding(horizontal = 8.dp)
        ) {
            Tab(
                selected = !showFavoritesOnly && !filterAudioOnly && !filterVideoOnly,
                onClick = { onFilterChange(false, false, false) }
            ) {
                Text("Все", modifier = Modifier.padding(16.dp))
            }
            Tab(
                selected = filterAudioOnly,
                onClick = { onFilterChange(true, false, false) }
            ) {
                Text("Аудио", modifier = Modifier.padding(16.dp))
            }
            Tab(
                selected = filterVideoOnly,
                onClick = { onFilterChange(false, true, false) }
            ) {
                Text("Видео", modifier = Modifier.padding(16.dp))
            }
            Tab(
                selected = showFavoritesOnly,
                onClick = { onFilterChange(false, false, true) }
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Favorite,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = if (showFavoritesOnly) Color(0xFFEC4899) else Color(0xFFF1F5F9)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Избранное", modifier = Modifier.padding(16.dp))
                }
            }
        }
        
        // Media list
        MediaLibraryScreen(
            mediaItems = mediaItems,
            onMediaClick = onMediaClick,
            onToggleFavorite = onToggleFavorite,
            showFavoritesOnly = showFavoritesOnly,
            filterAudioOnly = filterAudioOnly,
            filterVideoOnly = filterVideoOnly
        )
    }
}
