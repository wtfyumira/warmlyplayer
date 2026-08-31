package com.warmly.player.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorites")
data class FavoriteEntity(
    @PrimaryKey
    val mediaId: Long,
    val title: String,
    val artist: String?,
    val duration: Long,
    val uri: String,
    val mimeType: String,
    val dateAdded: Long = System.currentTimeMillis()
)
