package com.project.listugas.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.project.listugas.dao.MatkulDao
import com.project.listugas.dao.NoteDao
import com.project.listugas.dao.TugasDao
import com.project.listugas.entity.Matkul
import com.project.listugas.entity.Note
import com.project.listugas.entity.Tugas

@Database(entities = [Matkul::class, Tugas::class, Note::class], version = 2, exportSchema = false)
abstract class ListDatabase : RoomDatabase() {
    abstract fun matkulDao(): MatkulDao
    abstract fun tugasDao(): TugasDao
    abstract fun noteDao(): NoteDao

    companion object {
        @Volatile
        private var INSTANCE: ListDatabase? = null

        fun getDatabase(context: Context): ListDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ListDatabase::class.java,
                    "todo_database"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}