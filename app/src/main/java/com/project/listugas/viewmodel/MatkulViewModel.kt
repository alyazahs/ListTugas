package com.project.listugas.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.project.listugas.database.ListDatabase
import com.project.listugas.entity.Matkul
import com.project.listugas.repo.MatkulRepository
import kotlinx.coroutines.launch

class MatkulViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: MatkulRepository
    val allMatkuls: LiveData<List<Matkul>>

    init {
        val matkulDao = ListDatabase.getDatabase(application).matkulDao()
        repository = MatkulRepository(matkulDao)
        allMatkuls = repository.getAllMatkuls() // Mendapatkan semua matkul
    }

    // Mendapatkan mata kuliah berdasarkan ID
    fun getMatkulById(matkulId: Int): LiveData<Matkul> {
        return repository.getMatkulById(matkulId)
    }

    // Menambah mata kuliah baru
    fun insert(matkul: Matkul) = viewModelScope.launch {
        repository.insert(matkul)
    }

    // Memperbarui mata kuliah yang ada
    fun update(matkul: Matkul) = viewModelScope.launch {
        repository.update(matkul)
    }

    // Menghapus mata kuliah
    fun delete(matkul: Matkul) = viewModelScope.launch {
        repository.delete(matkul)
    }
}