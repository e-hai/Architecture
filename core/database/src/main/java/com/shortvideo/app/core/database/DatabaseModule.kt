package com.shortvideo.app.core.database

import androidx.room.Room
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val coreDatabaseModule =
    module {
        single {
            Room
                .databaseBuilder(
                    androidContext(),
                    AppDatabase::class.java,
                    "app_database",
                ).build()
        }

        single {
            get<AppDatabase>().videoDao()
        }
    }
