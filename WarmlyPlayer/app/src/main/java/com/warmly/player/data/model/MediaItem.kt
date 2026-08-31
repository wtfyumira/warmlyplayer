package com.warmly.player.data.model

data class MediaItem(
    val id: Long,
    val title: String,
    val artist: String?,
    val duration: Long,
    val uri: String,
    val mimeType: String,
    val isVideo: Boolean = mimeType.startsWith("video/"),
    var isFavorite: Boolean = false
) {
    fun getFormattedDuration(): String {
        val minutes = duration / 60000
        val seconds = (duration % 60000) / 1000
        return String.format("%02d:%02d", minutes, seconds)
    }
}
