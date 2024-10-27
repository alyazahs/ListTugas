package com.project.listugas

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.project.listugas.dao.MatkulDao
import com.project.listugas.dao.NoteDao
import com.project.listugas.dao.TugasDao
import com.project.listugas.database.ListDatabase
import com.project.listugas.entity.Matkul
import com.project.listugas.entity.Note
import com.project.listugas.entity.Tugas
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException


@RunWith(AndroidJUnit4::class)
class DatabaseTest {

    private lateinit var matkulDao: MatkulDao
    private lateinit var noteDao: NoteDao
    private lateinit var tugasDao: TugasDao
    private lateinit var db: ListDatabase

    private val mobile = Matkul(1, "Mobile", "Jumat")
    private val jarkom = Matkul(2, "Jaringan Komputer", "Kamis")

    private val UTS = Note(1,1, "UTS", "Membuat Aplikasi", "1-1-24")

    private val tugas = Tugas(1,1, "Membuat Aplikasi", true)

    @Before
    fun createDb() {
        val context: Context = ApplicationProvider.getApplicationContext()
        db = Room.inMemoryDatabaseBuilder(context, ListDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        matkulDao = db.matkulDao()
        noteDao = db.noteDao()
        tugasDao = db.tugasDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() = db.close()

    @Test
    @Throws(Exception::class)
    fun insertMatkul() {
        matkulDao.insert(mobile, jarkom)
        val result = matkulDao.getAll()
        assert(result.size == 2)
    }

    @Test
    @Throws(Exception::class)
    fun insertNote() {
        noteDao.insert(UTS)
        val result = noteDao.getAll()
        assert(result.size == 1)
    }

    @Test
    @Throws(Exception::class)
    fun insertTugas() {
        tugasDao.insert(tugas)
        val result = tugasDao.getAll()
        assert(result.size == 1)
    }
}
