package com.project.listugas.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.project.listugas.entity.Matkul

@Dao
interface MatkulDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(vararg matkul: Matkul)

    @Update
    suspend fun update(matkul: Matkul)

    @Delete
    suspend fun delete(matkul: Matkul)

    @Query("SELECT * FROM matkul ORDER BY Id")
    fun getAllMatkuls(): LiveData<List<Matkul>>

    @Query("SELECT * FROM matkul WHERE id = :matkulId LIMIT 1")
    fun getMatkulById(matkulId: Int): LiveData<Matkul>
}
