package com.warmly.player

import android.app.Application
import androidx.room.Room
import com.warmly.player.data.repository.FavoriteRepository

class WarmlyPlayerApplication : Application() {
    
    lateinit var database: MediaDatabase
        private set
    
    lateinit var favoriteRepository: FavoriteRepository
        private set
    
    override fun onCreate() {
        super.onCreate()
        
        database = Room.databaseBuilder(
            applicationContext,
            MediaDatabase::class.java,
            "warmly_player_database"
        ).build()
        
        favoriteRepository = FavoriteRepository(database.favoriteDao())
    }
}
