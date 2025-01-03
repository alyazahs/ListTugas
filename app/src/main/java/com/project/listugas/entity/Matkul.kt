package com.project.listugas.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "matkul")
data class Matkul(
    @PrimaryKey
    var id: Int = 0,
    val namaMatkul: String = "",
    val deskripsi: String = "",
    val category: String = ""
)
