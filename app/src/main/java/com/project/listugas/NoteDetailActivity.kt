package com.project.listugas

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.project.listugas.databinding.NoteBinding
import com.project.listugas.entity.Note
import com.project.listugas.viewmodel.NoteViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class NoteDetailActivity : AppCompatActivity() {
    private lateinit var binding: NoteBinding
    private val noteViewModel: NoteViewModel by viewModels()
    private var matkulId: Int = -1
    private var noteId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = NoteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val noteTitle = intent.getStringExtra("NOTE_TITLE")
        val noteContent = intent.getStringExtra("NOTE_CONTENT")
        noteId = intent.getIntExtra("NOTE_ID", -1)
        matkulId = intent.getIntExtra("MATKUL_ID", -1)

        Log.d("NoteDetailActivity", "Received noteId: $noteId, matkulId: $matkulId")

        binding.noteTitle.setText(noteTitle)
        binding.noteContent.setText(noteContent)

        binding.saveButton.setOnClickListener {
            val updatedTitle = binding.noteTitle.text.toString().trim()
            val updatedContent = binding.noteContent.text.toString().trim()

            if (updatedTitle.isNotEmpty() && updatedContent.isNotEmpty()) {
                val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                val currentDateAndTime: String = sdf.format(Date())

                val updatedNote = Note(
                    id = noteId,
                    matkulId = matkulId,
                    judul = updatedTitle,
                    deskripsi = updatedContent,
                    tanggal = currentDateAndTime
                )
                noteViewModel.update(updatedNote)
                Log.d("NoteDetailActivity", "Catatan diperbarui: $updatedNote")
                Toast.makeText(this, "Catatan berhasil diperbarui", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "Isi semua field", Toast.LENGTH_SHORT).show()
            }
        }
    }
}