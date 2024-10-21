package com.project.listugas.repo

import android.util.Log
import androidx.lifecycle.LiveData
import com.project.listugas.dao.NoteDao
import com.project.listugas.entity.Note

class NoteRepository(private val noteDao: NoteDao) {

    fun getAllNotes(): LiveData<List<Note>> {
        return noteDao.getAllNotes()
    }

    fun getNoteByMatkulId(matkulId: Int): LiveData<List<Note>> {
        return noteDao.getNoteBymatkulId(matkulId)
    }

    suspend fun insert(note: Note) {
        noteDao.insert(note)
        Log.d("NoteRepository", "Catatan ditambahkan: $note")
    }

    suspend fun update(note: Note) {
        noteDao.update(note)
        Log.d("NoteRepository", "Catatan diperbarui: $note")
    }

    suspend fun delete(note: Note) {
        noteDao.delete(note)
        Log.d("NoteRepository", "Catatan dihapus: $note")
    }
}