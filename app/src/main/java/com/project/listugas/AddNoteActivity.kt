package com.project.listugas

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.project.ListTugas.databinding.AddNoteBinding
import com.project.listugas.entity.Note
import com.project.listugas.viewmodel.NoteViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AddNoteActivity : AppCompatActivity() {
    private lateinit var binding: AddNoteBinding
    private val noteViewModel: NoteViewModel by viewModels() // Inisialisasi ViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = AddNoteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Mendapatkan matkulId dari intent
        val matkulId = intent.getIntExtra("MATKUL_ID", -1)

        binding.btnSubmit.setOnClickListener {
            val judul = binding.edNama.text.toString().trim()
            val deskripsi = binding.edDesk.text.toString().trim()

            if (judul.isNotEmpty() && deskripsi.isNotEmpty()) {
                val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                val currentDateAndTime: String = sdf.format(Date())

                val note = Note(
                    matkulId = matkulId,
                    judul = judul,
                    deskripsi = deskripsi,
                    tanggal = currentDateAndTime
                )
                noteViewModel.insert(note)
                Log.d("AddNoteActivity", "Catatan ditambahkan: $note") // Tambahkan logging
                Toast.makeText(this, "Catatan berhasil ditambahkan", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "Isi semua field", Toast.LENGTH_SHORT).show()
            }
        }
    }
}