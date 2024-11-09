package com.project.listugas

import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.project.listugas.databinding.NoteBinding
import com.project.listugas.date.DateUtils
import com.project.listugas.entity.Note
import com.project.listugas.viewmodel.NoteViewModel
import java.util.Date

class NoteDetailActivity : AppCompatActivity() {
    private lateinit var binding: NoteBinding
    private val noteViewModel: NoteViewModel by viewModels()
    private var matkulId: Int = -1
    private var noteId: Int = -1

    private val categories = mutableListOf<String>()
    private val sharedPreferences by lazy {
        getSharedPreferences("NotePreferences", MODE_PRIVATE)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = NoteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        noteId = intent.getIntExtra("NOTE_ID", -1)
        matkulId = intent.getIntExtra("MATKUL_ID", -1)
        val noteTitle = intent.getStringExtra("NOTE_TITLE")
        val noteContent = intent.getStringExtra("NOTE_CONTENT")

        Log.d("NoteDetailActivity", "Received noteId: $noteId, matkulId: $matkulId")

        binding.noteTitle.setText(noteTitle)
        binding.noteContent.setText(noteContent)

        loadCategories()

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerCategory.adapter = adapter

        binding.saveButton.setOnClickListener {
            val updatedTitle = binding.noteTitle.text.toString().trim()
            val updatedContent = binding.noteContent.text.toString().trim()
            val selectedCategory = binding.spinnerCategory.selectedItem.toString()

            if (updatedTitle.isNotEmpty() && updatedContent.isNotEmpty()) {
                val currentDate = DateUtils.getCurrentDate()

                val updatedNote = Note(
                    id = noteId,
                    matkulId = matkulId,
                    judul = updatedTitle,
                    deskripsi = updatedContent,
                    tanggal = currentDate,
                    category = selectedCategory
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

    private fun loadCategories() {
        val storedCategories = sharedPreferences.getStringSet("categories", setOf())
        categories.clear()
        categories.addAll(storedCategories ?: setOf("Umum"))
    }
}
