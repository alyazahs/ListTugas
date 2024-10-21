package com.project.listugas

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.project.listugas.dao.MatkulDao
import com.project.listugas.dao.NoteDao
import com.project.listugas.database.ListDatabase
import com.project.listugas.entity.Matkul
import com.project.listugas.entity.Note
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class DatabaseTest {

    private lateinit var matkulDao: MatkulDao
    private lateinit var noteDao: NoteDao
    private lateinit var db: ListDatabase

    private val mobile = Matkul(1, "Mobile", "Jumat")
    private val jarkom = Matkul(2, "Jaringan Komputer", "Kamis")

    private val uts = Note(1, 1,"uts","demo project", "20 oktober")

    @Before
    fun createDb() {
        val context: Context = ApplicationProvider.getApplicationContext()
        db = Room.inMemoryDatabaseBuilder(context, ListDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        matkulDao = db.matkulDao()
        noteDao = db.noteDao()
    }
    @After
    @Throws(IOException::class)
    fun closeDb() = db.close()

    @Test
    @Throws(Exception::class)
    fun insertAndRetrieveMatkul() {
        matkulDao.insert(mobile, jarkom)
        val result = matkulDao.getAll()
        assert(result.size == 2)
    }

    @Test
    @Throws(Exception::class)
    fun insertAndRetrieveNote() {
        noteDao.insert(uts)
        val result2 = noteDao.getAll()
        assert(result2.size == 1)
    }
}
