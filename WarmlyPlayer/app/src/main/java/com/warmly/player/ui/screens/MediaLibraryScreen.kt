package com.warmly.player.ui.screens

import android.Manifest
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.warmly.player.data.model.MediaItem
import com.warmly.player.ui.theme.*

@Composable
fun MediaLibraryScreen(
    mediaItems: List<MediaItem>,
    onMediaClick: (MediaItem) -> Unit,
    onToggleFavorite: (MediaItem) -> Unit,
    showFavoritesOnly: Boolean = false,
    filterAudioOnly: Boolean = false,
    filterVideoOnly: Boolean = false,
    modifier: Modifier = Modifier
) {
    val filteredItems = remember(mediaItems, showFavoritesOnly, filterAudioOnly, filterVideoOnly) {
        mediaItems.filter { item ->
            if (showFavoritesOnly && !item.isFavorite) return@filter false
            if (filterAudioOnly && item.isVideo) return@filter false
            if (filterVideoOnly && !item.isVideo) return@filter false
            true
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Background)
            .padding(16.dp)
    ) {
        if (filteredItems.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Медиа файлы не найдены",
                    color = OnSurface.copy(alpha = 0.6f),
                    fontSize = 18.sp
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredItems, key = { it.id }) { mediaItem ->
                    MediaItemRow(
                        mediaItem = mediaItem,
                        onClick = { onMediaClick(mediaItem) },
                        onToggleFavorite = { onToggleFavorite(mediaItem) }
                    )
                }
            }
        }
    }
}

@Composable
fun MediaItemRow(
    mediaItem: MediaItem,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = CardBackground)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Thumbnail or icon
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Surface),
                contentAlignment = Alignment.Center
            ) {
                if (mediaItem.isVideo) {
                    Icon(
                        imageVector = Icons.Default.Movie,
                        contentDescription = null,
                        tint = Primary,
                        modifier = Modifier.size(32.dp)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = null,
                        tint = Secondary,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Media info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = mediaItem.title,
                    color = OnSurface,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1
                )
                
                mediaItem.artist?.let { artist ->
                    Text(
                        text = artist,
                        color = OnSurface.copy(alpha = 0.7f),
                        fontSize = 14.sp,
                        maxLines = 1
                    )
                } ?: run {
                    Text(
                        text = mediaItem.getFormattedDuration(),
                        color = OnSurface.copy(alpha = 0.7f),
                        fontSize = 14.sp
                    )
                }
            }
            
            // Favorite button
            IconButton(
                onClick = onToggleFavorite
            ) {
                Icon(
                    imageVector = if (mediaItem.isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                    contentDescription = if (mediaItem.isFavorite) "Remove from favorites" else "Add to favorites",
                    tint = if (mediaItem.isFavorite) Secondary else OnSurface.copy(alpha = 0.5f),
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}
