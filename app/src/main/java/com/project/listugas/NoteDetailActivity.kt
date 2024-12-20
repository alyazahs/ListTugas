package com.project.listugas

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.project.listugas.databinding.NoteBinding
import com.project.listugas.date.DateUtils
import com.project.listugas.entity.Note
import com.project.listugas.viewmodel.NoteViewModel

class NoteDetailActivity : AppCompatActivity() {


    //persiapan variable yang akan digunakan
    private lateinit var binding: NoteBinding
    private val database: DatabaseReference by lazy { FirebaseDatabase.getInstance().getReference("notes") }
    private val noteViewModel: NoteViewModel by viewModels()
    private var matkulId: Int = -1
    private var noteId: Int = -1
    private val categories = mutableListOf<String>()
    private val sharedPreferences by lazy {
        getSharedPreferences("NotePreferences", MODE_PRIVATE)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //mengatur layout yang ditampilkan
        binding = NoteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Ambil data dari Intent
        noteId = intent.getIntExtra("NOTE_ID", -1)
        matkulId = intent.getIntExtra("MATKUL_ID", -1)
        val noteTitle = intent.getStringExtra("NOTE_TITLE") ?: ""
        val noteContent = intent.getStringExtra("NOTE_CONTENT") ?: ""

        // Atur UI awal
        binding.noteTitle.setText(noteTitle)
        binding.noteContent.setText(noteContent)

        loadCategories() //memuat kategori

        // Setup spinner untuk kategori
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerCategory.adapter = adapter

        // Tombol Simpan ketika di klik
        binding.saveButton.setOnClickListener {

            //ambil data inputan
            val updatedTitle = binding.noteTitle.text.toString().trim()
            val updatedContent = binding.noteContent.text.toString().trim()
            val selectedCategory = binding.spinnerCategory.selectedItem?.toString() ?: "Umum"

            //pengecekan apakah ada inputan yang kosong
            if (updatedTitle.isNotEmpty() && updatedContent.isNotEmpty()) {
                val currentDate = DateUtils.getCurrentDate()

                //membuat wadah untuk emnyimpan semua data
                val updatedNote = Note(
                    id = noteId,
                    matkulId = matkulId,
                    judul = updatedTitle,
                    deskripsi = updatedContent,
                    tanggal = currentDate,
                    category = selectedCategory
                )
                updateNoteInFirebase(updatedNote) //menjalankan fungsi update data firebase, dengan mengiirmkan semua data updatedNote
            } else {
                Toast.makeText(this, "Isi semua field", Toast.LENGTH_SHORT).show()
            }
        }
    }

    //memuat kategori
    private fun loadCategories() {
        val storedCategories = sharedPreferences.getStringSet("categories", setOf("Umum"))
        categories.clear()
        categories.addAll(storedCategories ?: setOf("Umum"))
    }

    private fun updateNoteInFirebase(note: Note) {
        database.child(note.id.toString()).setValue(note) //mengupdate data di firebase berdasarkan note.id
            .addOnSuccessListener {
                //ketika sukses akan muncul notifikasi
                Toast.makeText(this, "Catatan berhasil diperbarui", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e -> //ketika gagal akan muncul notifikasi
                Toast.makeText(this, "Gagal memperbarui catatan: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}