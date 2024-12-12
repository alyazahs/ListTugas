package com.project.listugas.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "note")
data class Note(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val judul: String = "",
    val deskripsi: String = "",
    val matkulName: String = "",
    val matkulId: Int = 0,
    val tanggal: String = "",
    val category: String = ""
)
