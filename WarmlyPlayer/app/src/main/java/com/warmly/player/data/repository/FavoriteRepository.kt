package com.warmly.player.data.repository

import com.warmly.player.data.model.FavoriteEntity
import com.warmly.player.data.model.MediaItem

class FavoriteRepository(private val favoriteDao: FavoriteDao) {
    
    fun getAllFavoritesFlow() = favoriteDao.getAllFavorites()
    
    suspend fun getFavorite(mediaId: Long): FavoriteEntity? {
        return favoriteDao.getFavorite(mediaId)
    }
    
    suspend fun isFavorite(mediaId: Long): Boolean {
        return favoriteDao.isFavorite(mediaId)
    }
    
    suspend fun toggleFavorite(mediaItem: MediaItem) {
        val isFav = favoriteDao.isFavorite(mediaItem.id)
        if (isFav) {
            favoriteDao.deleteFavoriteById(mediaItem.id)
        } else {
            val favorite = FavoriteEntity(
                mediaId = mediaItem.id,
                title = mediaItem.title,
                artist = mediaItem.artist,
                duration = mediaItem.duration,
                uri = mediaItem.uri,
                mimeType = mediaItem.mimeType
            )
            favoriteDao.insertFavorite(favorite)
        }
    }
    
    suspend fun addFavorite(mediaItem: MediaItem) {
        val favorite = FavoriteEntity(
            mediaId = mediaItem.id,
            title = mediaItem.title,
            artist = mediaItem.artist,
            duration = mediaItem.duration,
            uri = mediaItem.uri,
            mimeType = mediaItem.mimeType
        )
        favoriteDao.insertFavorite(favorite)
    }
    
    suspend fun removeFavorite(mediaId: Long) {
        favoriteDao.deleteFavoriteById(mediaId)
    }
}
