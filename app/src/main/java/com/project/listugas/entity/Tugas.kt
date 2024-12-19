package com.project.listugas.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tugas")
data class Tugas(
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,
    val matkulName: String = "",
    val matkulId: Int = 0,
    val namaTugas: String = "",
    var isCompleted: Boolean = false
)
