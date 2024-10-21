package com.project.listugas

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.project.ListTugas.databinding.ActivityNoteBinding
import com.project.listugas.adapter.NoteAdapter
import com.project.listugas.viewmodel.NoteViewModel

class NoteActivity : AppCompatActivity() {
    private lateinit var binding: ActivityNoteBinding
    private val noteViewModel: NoteViewModel by viewModels()
    private lateinit var adapter: NoteAdapter
    private var matkulId: Int = -1 // Menyimpan ID Mata Kuliah

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNoteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Mendapatkan matkulId dari intent
        matkulId = intent.getIntExtra("MATKUL_ID", -1)
        Log.d("NoteActivity", "Received matkulId: $matkulId") // Tambahkan logging

        adapter = NoteAdapter(
            onDeleteClick = { note ->
                noteViewModel.delete(note)
                Toast.makeText(this, "Catatan dihapus: ${note.judul}", Toast.LENGTH_SHORT).show()
            },
            onNoteClick = { note ->
                val intent = Intent(this, NoteDetailActivity::class.java)
                intent.putExtra("NOTE_TITLE", note.judul)
                intent.putExtra("NOTE_CONTENT", note.deskripsi)
                intent.putExtra("NOTE_ID", note.id)
                intent.putExtra("MATKUL_ID", matkulId)
                startActivity(intent)
            }
        )

        binding.rvNote.layoutManager = LinearLayoutManager(this)
        binding.rvNote.adapter = adapter

        noteViewModel.getNoteByMatkulId(matkulId).observe(this) { noteList ->
            Log.d("NoteActivity", "Notes loaded for matkulId: $matkulId -> $noteList") // Tambahkan logging
            noteList?.let {
                adapter.setNotes(it) // Memperbarui daftar catatan
            }
        }

        binding.btnNote.setOnClickListener {
            val intent = Intent(this, AddNoteActivity::class.java)
            intent.putExtra("MATKUL_ID", matkulId) // Pastikan mengirimkan matkulId ke AddNoteActivity
            startActivity(intent)
        }
    }
}