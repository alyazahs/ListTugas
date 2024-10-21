package com.project.listugas.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class TugasViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: TugasRepository

    init {
        val tugasDao = TodoDatabase.getDatabase(application).tugasDao()
        repository = TugasRepository(tugasDao)
    }
    fun getTugasByMatkulId(matkulId: Int): LiveData<List<Tugas>> {
        return repository.getTugasByMatkulId(matkulId)
    }

    fun insert(tugas: Tugas) = viewModelScope.launch {
        repository.insert(tugas)
    }
    fun delete(tugas: Tugas) = viewModelScope.launch {
        repository.delete(tugas)
    }
    fun updateStatus(tugasId: Int, isCompleted: Boolean) = viewModelScope.launch {
        repository.updateStatus(tugasId, isCompleted)
    }
}