package com.project.listugas.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.project.listugas.database.ListDatabase
import com.project.listugas.entity.Note
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NoteViewModel(application: Application) : AndroidViewModel(application) {
    private val noteDao = ListDatabase.getDatabase(application).noteDao()

    fun getNoteByMatkulName(matkulName: String): LiveData<List<Note>> {
        return noteDao.getNoteByMatkulName(matkulName)
    }

    fun getNoteByMatkulId(matkulId: Int): LiveData<List<Note>> {
        return noteDao.getNoteByMatkulId(matkulId)
    }

    fun insert(note: Note) = viewModelScope.launch {
        noteDao.insert(note)
    }

    fun update(note: Note) {
        viewModelScope.launch(Dispatchers.IO) {
            noteDao.update(note)
            Log.d("NoteViewModel", "Note updated in database: $note")
        }
    }

    fun delete(note: Note) = viewModelScope.launch {
        noteDao.delete(note)
    }
}
