package com.warmly.player.ui.screens

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.MediaItem as ExoMediaItem
import androidx.media3.common.Player
import com.warmly.player.data.model.MediaItem
import com.warmly.player.ui.theme.*

@Composable
fun PlayerScreen(
    currentMediaItem: MediaItem?,
    player: Player?,
    isPlaying: Boolean,
    repeatMode: Int,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onToggleRepeat: () -> Unit,
    onToggleFavorite: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Back button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "Back",
                        tint = OnSurface,
                        modifier = Modifier.size(32.dp)
                    )
                }
                
                Spacer(modifier = Modifier.weight(1f))
                
                Text(
                    text = "Сейчас играет",
                    color = OnSurface.copy(alpha = 0.7f),
                    fontSize = 14.sp
                )
                
                Spacer(modifier = Modifier.weight(1f))
            }
            
            // Album art / Video thumbnail placeholder
            Box(
                modifier = Modifier
                    .size(300.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Surface),
                contentAlignment = Alignment.Center
            ) {
                if (currentMediaItem != null) {
                    Icon(
                        imageVector = if (currentMediaItem.isVideo) Icons.Default.Movie else Icons.Default.MusicNote,
                        contentDescription = null,
                        tint = if (currentMediaItem.isVideo) Primary else Secondary,
                        modifier = Modifier.size(120.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Title and artist
            currentMediaItem?.let { media ->
                Text(
                    text = media.title,
                    color = OnSurface,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
                
                media.artist?.let { artist ->
                    Text(
                        text = artist,
                        color = OnSurface.copy(alpha = 0.7f),
                        fontSize = 16.sp,
                        maxLines = 1
                    )
                }
            } ?: run {
                Text(
                    text = "Выберите трек",
                    color = OnSurface.copy(alpha = 0.5f),
                    fontSize = 20.sp
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Progress bar (placeholder - would need actual progress from player)
            Slider(
                value = 0f,
                onValueChange = { },
                modifier = Modifier.fillMaxWidth(),
                colors = SliderDefaults.colors(
                    thumbColor = Primary,
                    activeTrackColor = Primary,
                    inactiveTrackColor = Surface
                )
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "0:00",
                    color = OnSurface.copy(alpha = 0.7f),
                    fontSize = 12.sp
                )
                
                currentMediaItem?.let { media ->
                    Text(
                        text = media.getFormattedDuration(),
                        color = OnSurface.copy(alpha = 0.7f),
                        fontSize = 12.sp
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Playback controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Repeat button
                IconButton(onClick = onToggleRepeat) {
                    Icon(
                        imageVector = when (repeatMode) {
                            Player.REPEAT_MODE_ONE -> Icons.Default.RepeatOne
                            Player.REPEAT_MODE_ALL -> Icons.Default.Repeat
                            else -> Icons.Default.Repeat
                        },
                        contentDescription = "Repeat mode",
                        tint = if (repeatMode != Player.REPEAT_MODE_OFF) Secondary else OnSurface.copy(alpha = 0.5f),
                        modifier = Modifier.size(28.dp)
                    )
                }
                
                // Previous button
                IconButton(onClick = onPrevious) {
                    Icon(
                        imageVector = Icons.Default.SkipPrevious,
                        contentDescription = "Previous",
                        tint = OnSurface,
                        modifier = Modifier.size(48.dp)
                    )
                }
                
                // Play/Pause button
                FloatingActionButton(
                    onClick = onPlayPause,
                    containerColor = Primary,
                    modifier = Modifier.size(72.dp)
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) "Pause" else "Play",
                        tint = OnPrimary,
                        modifier = Modifier.size(40.dp)
                    )
                }
                
                // Next button
                IconButton(onClick = onNext) {
                    Icon(
                        imageVector = Icons.Default.SkipNext,
                        contentDescription = "Next",
                        tint = OnSurface,
                        modifier = Modifier.size(48.dp)
                    )
                }
                
                // Favorite button
                IconButton(onClick = onToggleFavorite) {
                    Icon(
                        imageVector = if (currentMediaItem?.isFavorite == true) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                        contentDescription = "Toggle favorite",
                        tint = if (currentMediaItem?.isFavorite == true) Secondary else OnSurface.copy(alpha = 0.5f),
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
    }
}
