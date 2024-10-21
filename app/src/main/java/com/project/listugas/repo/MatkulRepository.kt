package com.project.listugas.repo

import androidx.lifecycle.LiveData
import com.project.listugas.dao.MatkulDao
import com.project.listugas.entity.Matkul

class MatkulRepository(private val matkulDao: MatkulDao) {

    // Mengambil semua data mata kuliah
    fun getAllMatkuls(): LiveData<List<Matkul>> {
        return matkulDao.getAllMatkuls()
    }

    // Mengambil data mata kuliah berdasarkan ID
    fun getMatkulById(matkulId: Int): LiveData<Matkul> {
        return matkulDao.getMatkulById(matkulId)
    }

    // Menyisipkan data mata kuliah baru
    suspend fun insert(matkul: Matkul) {
        matkulDao.insert(matkul)
    }

    // Memperbarui data mata kuliah yang ada
    suspend fun update(matkul: Matkul) {
        matkulDao.update(matkul)
    }

    // Menghapus data mata kuliah
    suspend fun delete(matkul: Matkul) {
        matkulDao.delete(matkul)
    }
}
