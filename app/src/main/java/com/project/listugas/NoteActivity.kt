package com.project.listugas

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.project.listugas.adapter.NoteAdapter
import com.project.listugas.databinding.ActivityNoteBinding
import com.project.listugas.entity.Note
import com.project.listugas.viewmodel.NoteViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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

        // Inisialisasi adapter
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

        // Set adapter dan layout manager untuk RecyclerView
        binding.rvNote.layoutManager = LinearLayoutManager(this)
        binding.rvNote.adapter = adapter

        // Observasi LiveData untuk memperbarui UI secara otomatis
        noteViewModel.getNoteByMatkulId(matkulId).observe(this) { noteList ->
            noteList?.let {
                adapter.setNotes(it) // Memperbarui daftar catatan
            }
        }

        // Menampilkan popup untuk menambahkan catatan
        binding.btnNote.setOnClickListener {
            showNotePopup()
        }
    }

    private fun showNotePopup(note: Note? = null) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.add_note, null)

        // Inisialisasi input field
        val inputJudul = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.ed_nama)
        val inputDeskripsi = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.ed_desk)

        // Jika edit, isi dengan data yang ada
        note?.let {
            inputJudul.setText(it.judul)
            inputDeskripsi.setText(it.deskripsi)
        }

        // Buat AlertDialog dan atur view
        val dialog = androidx.appcompat.app.AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        // Tampilkan dialog
        dialog.show()

        // Atur ukuran dan posisi popup di tengah
        dialog.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.85).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        // Tombol submit di popup
        dialogView.findViewById<Button>(R.id.btn_submit).setOnClickListener {
            val judulNote = inputJudul.text.toString().trim()
            val deskripsiNote = inputDeskripsi.text.toString().trim()
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val currentDate: String = sdf.format(Date())

            if (judulNote.isNotEmpty() && deskripsiNote.isNotEmpty()) {
                val newNote = Note(
                    id = note?.id ?: 0,
                    judul = judulNote,
                    deskripsi = deskripsiNote,
                    matkulId = matkulId,
                    tanggal = currentDate
                )

                if (note == null) {
                    noteViewModel.insert(newNote)
                    Toast.makeText(this, "Catatan berhasil ditambahkan", Toast.LENGTH_SHORT).show()
                } else {
                    noteViewModel.update(newNote)
                    Toast.makeText(this, "Catatan berhasil diperbarui", Toast.LENGTH_SHORT).show()
                }
                dialog.dismiss() // Tutup popup
            } else {
                Toast.makeText(this, "Isi semua field", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
