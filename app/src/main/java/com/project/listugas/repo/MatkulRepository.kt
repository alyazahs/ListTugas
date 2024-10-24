package com.project.listugas.repo

import androidx.lifecycle.LiveData
import com.project.listugas.dao.MatkulDao
import com.project.listugas.entity.Matkul

class MatkulRepository(private val matkulDao: MatkulDao) {

    fun getAllMatkuls(): LiveData<List<Matkul>> {
        return matkulDao.getAllMatkuls()
    }

    fun getMatkulById(matkulId: Int): LiveData<Matkul> {
        return matkulDao.getMatkulById(matkulId)
    }

    suspend fun insert(matkul: Matkul) {
        matkulDao.insert(matkul)
    }

    suspend fun update(matkul: Matkul) {
        matkulDao.update(matkul)
    }

    suspend fun delete(matkul: Matkul) {
        matkulDao.delete(matkul)
    }
}