package com.project.listugas

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.bottomnavigation.BottomNavigationView
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

class NoteActivity : AppCompatActivity(), BottomNavigationView.OnNavigationItemSelectedListener {
    private lateinit var binding: ActivityNoteBinding
    private val matkulViewModel: MatkulViewModel by viewModels()
    private lateinit var adapter: NoteAdapter
    private var matkulId: Int = -1
    private var matkulName: String = ""

    private val categories = mutableListOf("Umum")
    private val database: DatabaseReference by lazy {
        FirebaseDatabase.getInstance().getReference("notes")
    }

    private val sharedPreferences by lazy {
        getSharedPreferences("NotePreferences", MODE_PRIVATE)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNoteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        matkulId = intent.getIntExtra("MATKUL_ID", -1)
        matkulName = intent.getStringExtra("MATKUL_NAME") ?: ""

        setupUI()
        loadCategories()
        observeViewModel()
        fetchNotesFromFirebase()
    }

    private fun setupUI() {
        binding.tvname.text = matkulName

        adapter = NoteAdapter(
            onDeleteClick = { note ->
                deleteNoteFromFirebase(note)
                Toast.makeText(this, "Catatan dihapus: ${note.judul}", Toast.LENGTH_SHORT).show()
            },
            onNoteClick = { note ->
                val intent = Intent(this, NoteDetailActivity::class.java).apply {
                    putExtra("NOTE_TITLE", note.judul)
                    putExtra("NOTE_CONTENT", note.deskripsi)
                    putExtra("NOTE_ID", note.id)
                    putExtra("MATKUL_ID", matkulId)
                }
                startActivity(intent)
            }
        )

        val layoutManager = GridLayoutManager(this, 2).apply {
            spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                override fun getSpanSize(position: Int): Int {
                    return when (adapter.getItemViewType(position)) {
                        NoteAdapter.ViewType.HEADER.ordinal -> 2
                        else -> 1
                    }
                }
            }
        }

        binding.rvNote.layoutManager = layoutManager
        binding.rvNote.adapter = adapter

        binding.btnNote.setOnClickListener { showNotePopup() }
        binding.bottomNavigation.setOnNavigationItemSelectedListener(this)
    }

    private fun loadCategories() {
        val storedCategories = sharedPreferences.getStringSet("categories", null)
        categories.clear()
        categories.addAll(storedCategories ?: listOf("Umum"))
    }

    private fun saveCategories() {
        sharedPreferences.edit()
            .putStringSet("categories", categories.toSet())
            .apply()
    }

    private fun observeViewModel() {
        matkulViewModel.getMatkulById(matkulId).observe(this) { matkul ->
            matkul?.let { binding.tvname.text = it.namaMatkul }
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

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        dialogBinding.spinnerCategory.adapter = adapter

        note?.let {
            dialogBinding.edNama.setText(it.judul)
            dialogBinding.edDesk.setText(it.deskripsi)
            val categoryIndex = categories.indexOf(it.category)
            dialogBinding.spinnerCategory.setSelection(categoryIndex)
        }

        dialogBinding.btnSubmit.setOnClickListener {
            val judul = dialogBinding.edNama.text.toString().trim()
            val deskripsi = dialogBinding.edDesk.text.toString().trim()
            val selectedCategory = dialogBinding.spinnerCategory.selectedItem?.toString()
            val currentDate = DateUtils.getCurrentDate()

            if (judul.isNotEmpty() && deskripsi.isNotEmpty() && !selectedCategory.isNullOrEmpty()) {
                val newNote = Note(
                    id = note?.id ?: generateId(judul, deskripsi, selectedCategory),
                    judul = judul,
                    deskripsi = deskripsi,
                    matkulId = matkulId,
                    tanggal = currentDate,
                    category = selectedCategory
                )
                database.child(newNote.id.toString()).setValue(newNote)
                dialog.dismiss()
                Toast.makeText(this, "Catatan berhasil ditambahkan", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Isi semua field", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun deleteNoteFromFirebase(note: Note) {
        database.child(note.id.toString()).removeValue()
    }

    private fun fetchNotesFromFirebase() {
        database.orderByChild("matkulId").equalTo(matkulId.toDouble()).addValueEventListener(object :
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val notes = snapshot.children.mapNotNull { it.getValue(Note::class.java) }
                adapter.submitList(createCategorizedList(notes))
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@NoteActivity, "Gagal mengambil data: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun createCategorizedList(notes: List<Note>): List<Any> {
        return notes.groupBy { it.category }.flatMap { (category, notes) ->
            listOf(NoteAdapter.CategoryItem(category)) + notes
        }
    }

    private fun generateId(judul: String, deskripsi: String, category: String): Int {
        return "$judul$deskripsi$category".hashCode()
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_note -> {
                binding.bottomNavigation.selectedItemId = R.id.action_note
                return true
            }
            R.id.action_todo -> {
                Intent(this, TugasActivity::class.java).apply {
                    putExtra("MATKUL_ID", matkulId)
                    putExtra("MATKUL_NAME", matkulName)
                    startActivity(this)
                }
                return true
            }
        }
        return false
    }
}
