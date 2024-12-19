package com.project.listugas.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.project.listugas.database.ListDatabase
import com.project.listugas.entity.Matkul
import kotlinx.coroutines.launch

class MatkulViewModel(application: Application) : AndroidViewModel(application) {
    private val matkulDao = ListDatabase.getDatabase(application).matkulDao()

    val allMatkuls: LiveData<List<Matkul>> = matkulDao.getAllMatkuls()

    fun getMatkulById(matkulId: Int): LiveData<Matkul> {
        return matkulDao.getMatkulById(matkulId)
    }

    fun insert(matkul: Matkul) = viewModelScope.launch {
        matkulDao.insert(matkul)
    }

    fun update(matkul: Matkul) = viewModelScope.launch {
        matkulDao.update(matkul)
    }

    fun delete(matkul: Matkul) = viewModelScope.launch {
        matkulDao.delete(matkul)
    }
}
