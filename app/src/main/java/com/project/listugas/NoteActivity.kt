package com.project.listugas

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.project.listugas.adapter.NoteAdapter
import com.project.listugas.databinding.ActivityNoteBinding
import com.project.listugas.databinding.AddNoteBinding
import com.project.listugas.date.DateUtils
import com.project.listugas.entity.Note
import com.project.listugas.viewmodel.MatkulViewModel
import com.project.listugas.viewmodel.NoteViewModel

class NoteActivity : AppCompatActivity() {
    private lateinit var binding: ActivityNoteBinding
    private val noteViewModel: NoteViewModel by viewModels()
    private val matkulViewModel: MatkulViewModel by viewModels()
    private lateinit var adapter: NoteAdapter
    private var matkulId: Int = -1

    private val categories = mutableListOf<String>()
    private val database: DatabaseReference = FirebaseDatabase.getInstance().getReference("notes")

    private val sharedPreferences by lazy {
        getSharedPreferences("NotePreferences", MODE_PRIVATE)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNoteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        matkulId = intent.getIntExtra("MATKUL_ID", -1)

        loadCategories()
        setupRecyclerView()
        observeViewModel()

        binding.btnNote.setOnClickListener {
            showNotePopup()
        }

        binding.iconNote.setOnClickListener {
            val intent = Intent(this, NoteActivity::class.java)
            intent.putExtra("MATKUL_ID", matkulId)
            startActivity(intent)
        }

        binding.iconTodo.setOnClickListener {
            val intent = Intent(this, TugasActivity::class.java)
            intent.putExtra("MATKUL_ID", matkulId)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        fetchNotesFromFirebase()
    }

    private fun loadCategories() {
        val storedCategories = sharedPreferences.getStringSet("categories", setOf())
        categories.clear()
        categories.addAll(storedCategories ?: setOf("Umum"))
    }

    private fun saveCategories() {
        sharedPreferences.edit().putStringSet("categories", categories.toSet()).apply()
        Log.d("NoteActivity", "Saved categories: $categories")
    }

    private fun setupRecyclerView() {
        adapter = NoteAdapter(
            onDeleteClick = { note ->
                deleteNoteFromFirebase(note)
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

        val customLayoutManager = GridLayoutManager(this, 2)
        customLayoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return when (adapter.getItemViewType(position)) {
                    NoteAdapter.ViewType.HEADER.ordinal -> 2
                    NoteAdapter.ViewType.ITEM.ordinal -> 1
                    else -> 1
                }
            }
        }

        binding.rvNote.layoutManager = customLayoutManager
        binding.rvNote.adapter = adapter
    }

    private fun observeViewModel() {
        matkulViewModel.getMatkulById(matkulId).observe(this) { matkul ->
            matkul?.let {
                binding.tvname.text = it.namaMatkul
            }
        }
    }

    private fun createCategorizedList(notes: List<Note>): List<Any> {
        val categorizedList = mutableListOf<Any>()
        val groupedNotes = notes.groupBy { it.category }

        for ((category, items) in groupedNotes) {
            categorizedList.add(NoteAdapter.CategoryItem(category)) // Header
            categorizedList.addAll(items) // Notes
        }

        return categorizedList
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

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        dialogBinding.spinnerCategory.adapter = adapter

        note?.let {
            dialogBinding.edNama.setText(it.judul)
            dialogBinding.edDesk.setText(it.deskripsi)
            val categoryIndex = categories.indexOf(it.category)
            dialogBinding.spinnerCategory.setSelection(categoryIndex)
        }

        dialogBinding.btnSubmit.setOnClickListener {
            val judulNote = dialogBinding.edNama.text.toString().trim()
            val deskripsiNote = dialogBinding.edDesk.text.toString().trim()
            val selectedCategory = dialogBinding.spinnerCategory.selectedItem?.toString()
            val currentDate = DateUtils.getCurrentDate()

            if (judulNote.isNotEmpty() && deskripsiNote.isNotEmpty() && !selectedCategory.isNullOrEmpty()) {
                val newNote = Note(
                    id = note?.id ?: database.push().key?.hashCode() ?: generateId(judulNote, deskripsiNote, selectedCategory),
                    judul = judulNote,
                    deskripsi = deskripsiNote,
                    matkulId = matkulId,
                    tanggal = currentDate,
                    category = selectedCategory
                )

                database.child(newNote.id.toString()).setValue(newNote)
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

        dialogBinding.btnSubmitCategory.setOnClickListener {
            val newCategory = dialogBinding.edNewCategory.text.toString().trim()
            if (newCategory.isNotEmpty()) {
                categories.add(newCategory)
                saveCategories()
                adapter.notifyDataSetChanged()
                dialogBinding.edNewCategory.text?.clear()
                Toast.makeText(this, "Kategori berhasil ditambahkan", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Isi nama kategori", Toast.LENGTH_SHORT).show()
            }
        }

        dialogBinding.btnDeleteCategory.setOnClickListener {
            val categoryToDelete = dialogBinding.spinnerCategory.selectedItem?.toString()
            if (!categoryToDelete.isNullOrEmpty() && categoryToDelete != "Umum") {
                categories.remove(categoryToDelete)
                saveCategories()
                adapter.notifyDataSetChanged()
                Toast.makeText(this, "Kategori berhasil dihapus", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Kategori 'Umum' tidak dapat dihapus", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun deleteNoteFromFirebase(note: Note) {
        database.child(note.id.toString()).removeValue()
        noteViewModel.delete(note)
        Toast.makeText(this, "Catatan ${note.judul} dihapus", Toast.LENGTH_SHORT).show()
    }

    private fun fetchNotesFromFirebase() {
        database.orderByChild("matkulId").equalTo(matkulId.toDouble()).addValueEventListener(object :
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val notesList = mutableListOf<Note>()
                snapshot.children.forEach { dataSnapshot ->
                    val note = dataSnapshot.getValue(Note::class.java)
                    note?.let { notesList.add(it) }
                }
                val categorizedList = createCategorizedList(notesList)
                adapter.submitList(categorizedList)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@NoteActivity, "Gagal mengambil data: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun generateId(judul: String, deskripsi: String, category: String): Int {
        val combinedData = "$judul$deskripsi$category"
        return combinedData.hashCode()
    }
}