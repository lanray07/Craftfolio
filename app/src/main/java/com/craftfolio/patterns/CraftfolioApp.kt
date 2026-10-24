package com.craftfolio.patterns

import android.app.Application
import androidx.room.Room

class CraftfolioApp : Application() {
    val database by lazy { Room.databaseBuilder(this, CraftDatabase::class.java, "craftfolio.db")
        .addMigrations(MIGRATION_1_2)
        .addCallback(object : androidx.room.RoomDatabase.Callback() {
            override fun onOpen(db: androidx.sqlite.db.SupportSQLiteDatabase) { db.execSQL("PRAGMA synchronous=FULL") }
        }).build() }
    val repository by lazy { Repository(this, database) }
    val billing by lazy { AmazonBilling(this) }
}
