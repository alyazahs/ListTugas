package com.project.listugas.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.project.listugas.database.ListDatabase
import com.project.listugas.entity.Note
import kotlinx.coroutines.launch

class NoteViewModel(application: Application) : AndroidViewModel(application) {
    private val noteDao = ListDatabase.getDatabase(application).noteDao()

    fun getNoteByMatkulId(matkulId: Int): LiveData<List<Note>> {
        return noteDao.getNoteBymatkulId(matkulId)
    }

    fun insert(note: Note) = viewModelScope.launch {
        noteDao.insert(note)
        Log.d("NoteViewModel", "Catatan ditambahkan: $note")
    }

    fun update(note: Note) = viewModelScope.launch {
        noteDao.update(note)
        Log.d("NoteViewModel", "Catatan diperbarui: $note")
    }

    fun delete(note: Note) = viewModelScope.launch {
        noteDao.delete(note)
        Log.d("NoteViewModel", "Catatan dihapus: $note")
    }
}
