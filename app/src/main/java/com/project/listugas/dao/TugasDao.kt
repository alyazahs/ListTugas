package com.project.listugas.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.project.listugas.entity.Tugas

@Dao
interface TugasDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(vararg tugas: Tugas)

    @Delete
    suspend fun delete(tugas: Tugas)

    @Update
    suspend fun update(tugas: Tugas)

    @Query("UPDATE tugas SET isCompleted = :isCompleted WHERE id = :tugasId")
    suspend fun updateStatus(tugasId: Int, isCompleted: Boolean)

    @Query("SELECT * FROM tugas")
    fun getAll(): Array<Tugas>
}