package com.project.listugas.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity( "note")
data class Note(
    @PrimaryKey(true) val id: Int = 0,
    val matkulId: Int = 0,
    val judul: String = "",
    val deskripsi: String = "",
    val tanggal: String = "",
    val category: String= ""
)