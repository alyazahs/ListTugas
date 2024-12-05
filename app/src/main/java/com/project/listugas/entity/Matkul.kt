package com.project.listugas.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "matkul")
data class Matkul(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val namaMatkul: String="",
    val deskripsi: String="",
    val category: String=""
){
    constructor(): this(0, "", "", "")
}