package com.warmly.player.data.repository

import androidx.room.Database
import androidx.room.RoomDatabase
import com.warmly.player.data.model.FavoriteEntity

@Database(entities = [FavoriteEntity::class], version = 1, exportSchema = false)
abstract class MediaDatabase : RoomDatabase() {
    abstract fun favoriteDao(): FavoriteDao
}
