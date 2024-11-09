package com.project.listugas

import android.content.Intent
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.project.listugas.adapter.NoteAdapter
import com.project.listugas.databinding.ActivityNoteBinding
import com.project.listugas.databinding.AddNoteBinding
import com.project.listugas.date.DateUtils
import com.project.listugas.entity.Note
import com.project.listugas.viewmodel.NoteViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class NoteActivity() : AppCompatActivity(), Parcelable {
    private lateinit var binding: ActivityNoteBinding
    private val noteViewModel: NoteViewModel by viewModels()
    private lateinit var adapter: NoteAdapter
    private var matkulId: Int = -1

    constructor(parcel: Parcel) : this() {
        matkulId = parcel.readInt()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNoteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        matkulId = intent.getIntExtra("MATKUL_ID", -1)

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
            noteList?.let {
                adapter.submitList(it.sortedBy { note ->
                    SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).parse(note.tanggal)
                })
            }
        }

        binding.btnNote.setOnClickListener {
            showNotePopup()
        }
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(matkulId)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<NoteActivity> {
        override fun createFromParcel(parcel: Parcel): NoteActivity {
            return NoteActivity(parcel)
        }

        override fun newArray(size: Int): Array<NoteActivity?> {
            return arrayOfNulls(size)
        }
    }

    private fun showNotePopup(note: Note? = null) {
        val dialogBinding = AddNoteBinding.inflate(LayoutInflater.from(this))
        val dialog = androidx.appcompat.app.AlertDialog.Builder(this)
            .setView(dialogBinding.root)
            .create()

        dialog.show()

        dialog.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.85).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        note?.let {
            dialogBinding.edNama.setText(it.judul)
            dialogBinding.edDesk.setText(it.deskripsi)
        }

        dialogBinding.btnSubmit.setOnClickListener {
            val judulNote = dialogBinding.edNama.text.toString().trim()
            val deskripsiNote = dialogBinding.edDesk.text.toString().trim()
            val currentDate = DateUtils.getCurrentDate()

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
                dialog.dismiss()
            } else {
                Toast.makeText(this, "Isi semua field", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
