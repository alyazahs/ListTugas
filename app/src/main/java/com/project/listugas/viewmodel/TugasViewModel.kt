package com.project.listugas.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.project.listugas.database.ListDatabase
import com.project.listugas.entity.Tugas
import kotlinx.coroutines.launch

class TugasViewModel(application: Application) : AndroidViewModel(application) {
    private val tugasDao = ListDatabase.getDatabase(application).tugasDao()

    fun getTugasByMatkulId(matkulId: Int): LiveData<List<Tugas>> {
        return tugasDao.getTugasByMatkulId(matkulId)
    }

    fun insert(tugas: Tugas) = viewModelScope.launch {
        tugasDao.insert(tugas)
    }

    fun delete(tugas: Tugas) = viewModelScope.launch {
        tugasDao.delete(tugas)
    }

    fun update(tugas: Tugas) = viewModelScope.launch {
        tugasDao.update(tugas)
    }

    fun updateStatus(tugasId: Int, isCompleted: Boolean) = viewModelScope.launch {
        tugasDao.updateStatus(tugasId, isCompleted)
    }
}
