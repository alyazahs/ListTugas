package com.project.listugas.repo

import androidx.lifecycle.LiveData

class TugasRepository(private val tugasDao: TugasDao) {

    fun getTugasByMatkulId(matkulId: Int): LiveData<List<Tugas>> {
        return tugasDao.getTugasByMatkulId(matkulId)
    }

    suspend fun insert(tugas: Tugas) {
        tugasDao.insert(tugas)
    }

    suspend fun delete(tugas: Tugas) {
        tugasDao.delete(tugas)
    }

    suspend fun updateStatus(tugasId: Int, isCompleted: Boolean) {
        tugasDao.updateStatus(tugasId, isCompleted)
    }
}