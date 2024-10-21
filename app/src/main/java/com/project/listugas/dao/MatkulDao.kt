package com.project.listugas.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
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

    @Query("SELECT * FROM matkul")
    fun getAll(): Array<Matkul>
}
