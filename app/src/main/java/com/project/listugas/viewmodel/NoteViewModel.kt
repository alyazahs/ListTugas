package com.project.listugas.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.project.listugas.database.ListDatabase
import com.project.listugas.entity.Note
import com.project.listugas.repo.NoteRepository
import kotlinx.coroutines.launch

class NoteViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: NoteRepository
    val allNotes: LiveData<List<Note>>

    init {
        val noteDao = ListDatabase.getDatabase(application).noteDao()
        repository = NoteRepository(noteDao)
        allNotes = repository.getAllNotes()
    }

    fun getNoteByMatkulId(matkulId: Int): LiveData<List<Note>> {
        return repository.getNoteByMatkulId(matkulId)
    }

    fun insert(note: Note) = viewModelScope.launch {
        repository.insert(note)
        Log.d("NoteViewModel", "Catatan ditambahkan: $note")
    }

    fun update(note: Note) = viewModelScope.launch {
        repository.update(note)
        Log.d("NoteViewModel", "Catatan diperbarui: $note")
    }

    fun delete(note: Note) = viewModelScope.launch {
        repository.delete(note)
        Log.d("NoteViewModel", "Catatan dihapus: $note")
    }
}