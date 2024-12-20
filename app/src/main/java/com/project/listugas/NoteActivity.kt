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

// Aktivitas untuk menampilkan dan mengelola daftar catatan
class NoteActivity : AppCompatActivity(), BottomNavigationView.OnNavigationItemSelectedListener {

    // View binding untuk mengakses elemen UI di activity_note.xml
    private lateinit var binding: ActivityNoteBinding

    // ViewModel untuk mengambil data matkul berdasarkan ID
    private val matkulViewModel: MatkulViewModel by viewModels()

    // Adapter untuk RecyclerView yang menampilkan daftar catatan
    private lateinit var adapter: NoteAdapter

    // ID dan nama mata kuliah yang diterima dari Intent
    private var matkulId: Int = -1
    private var matkulName: String = ""

    // Daftar kategori untuk catatan, dengan kategori default "Umum"
    private val categories = mutableListOf("Umum")

    // Referensi ke database Firebase
    private val database: DatabaseReference by lazy {
        FirebaseDatabase.getInstance().getReference("notes")
    }

    // SharedPreferences untuk menyimpan dan memuat kategori secara lokal
    private val sharedPreferences by lazy {
        getSharedPreferences("NotePreferences", MODE_PRIVATE)
    }

    // Override method onCreate untuk mengatur apa yang terjadi ketika Activity dibuat
    override fun onCreate(savedInstanceState: Bundle?) {
        // Memanggil method onCreate dari superclass (AppCompatActivity atau Activity)
        super.onCreate(savedInstanceState)

        // Menginisialisasi binding menggunakan kelas yang dihasilkan dari View Binding
        // View Binding menghasilkan file binding berdasarkan layout XML (contoh: activity_note.xml).
        binding = ActivityNoteBinding.inflate(layoutInflater)

        // Mengatur tampilan konten Activity ke root view dari binding
        // Root view adalah elemen teratas dalam layout XML yang dihubungkan ke binding.
        setContentView(binding.root)

        // Ambil data ID dan nama mata kuliah dari Intent
        matkulId = intent.getIntExtra("MATKUL_ID", -1)
        matkulName = intent.getStringExtra("MATKUL_NAME") ?: ""

        // Inisialisasi UI, kategori, dan data
        setupUI()
        loadCategories()
        observeViewModel()
        fetchNotesFromFirebase()
    }

    // Mengatur elemen-elemen UI
    private fun setupUI() {
        // Menampilkan nama mata kuliah
        binding.tvname.text = matkulName

        // Inisialisasi adapter untuk RecyclerView
        adapter = NoteAdapter(
            onDeleteClick = { note ->
                deleteNoteFromFirebase(note) // Hapus catatan dari Firebase
                Toast.makeText(this, "Catatan dihapus: ${note.judul}", Toast.LENGTH_SHORT).show()
            },
            onNoteClick = { note ->
                // Buka detail catatan saat catatan diklik
                val intent = Intent(this, NoteDetailActivity::class.java).apply {
                    putExtra("NOTE_TITLE", note.judul)
                    putExtra("NOTE_CONTENT", note.deskripsi)
                    putExtra("NOTE_ID", note.id)
                    putExtra("MATKUL_ID", matkulId)
                }
                startActivity(intent)
            }
        )

        // Menentukan tata letak RecyclerView (Grid dengan 2 kolom)
        val layoutManager = GridLayoutManager(this, 2).apply {
            spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                override fun getSpanSize(position: Int): Int {
                    // Header memakan 2 kolom, item biasa memakan 1 kolom
                    return when (adapter.getItemViewType(position)) {
                        NoteAdapter.ViewType.HEADER.ordinal -> 2
                        else -> 1
                    }
                }
            }
        }

        binding.rvNote.layoutManager = layoutManager // Set tata letak RecyclerView
        binding.rvNote.adapter = adapter             // Set adapter untuk RecyclerView

        binding.btnNote.setOnClickListener { showNotePopup() } // Tombol tambah catatan
        binding.bottomNavigation.setOnNavigationItemSelectedListener(this) // Navigasi bawah
    }

    // Memuat kategori dari SharedPreferences
    private fun loadCategories() {
        val storedCategories = sharedPreferences.getStringSet("categories", null)
        categories.clear()
        categories.addAll(storedCategories ?: listOf("Umum"))
    }

    // Menyimpan kategori ke SharedPreferences
    private fun saveCategories() {
        sharedPreferences.edit()
            .putStringSet("categories", categories.toSet())
            .apply()
    }

    // Mengamati data mata kuliah dari ViewModel
    private fun observeViewModel() {
        matkulViewModel.getMatkulById(matkulId).observe(this) { matkul ->
            matkul?.let { binding.tvname.text = it.namaMatkul }
        }
    }

    // Menampilkan dialog untuk menambah atau mengedit catatan
    private fun showNotePopup(note: Note? = null) {
        val dialogBinding = AddNoteBinding.inflate(LayoutInflater.from(this))
        val dialog = androidx.appcompat.app.AlertDialog.Builder(this)
            .setView(dialogBinding.root)
            .create()

        dialog.show()

        // Mengatur ukuran dialog
        dialog.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.85).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        // Adapter untuk spinner kategori
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        dialogBinding.spinnerCategory.adapter = adapter

        // Jika ada catatan, isi data ke dialog untuk diedit
        note?.let {
            dialogBinding.edNama.setText(it.judul)
            dialogBinding.edDesk.setText(it.deskripsi)
            val categoryIndex = categories.indexOf(it.category)
            dialogBinding.spinnerCategory.setSelection(categoryIndex)
        }

        // Tombol tambah atau perbarui catatan
        dialogBinding.btnSubmit.setOnClickListener {
            val judul = dialogBinding.edNama.text.toString().trim()
            val deskripsi = dialogBinding.edDesk.text.toString().trim()
            val selectedCategory = dialogBinding.spinnerCategory.selectedItem?.toString()
            val currentDate = DateUtils.getCurrentDate()

            if (judul.isNotEmpty() && deskripsi.isNotEmpty() && !selectedCategory.isNullOrEmpty()) {
                val noteId = note?.id ?: generateId(judul, deskripsi, selectedCategory)
                val newNote = Note(
                    id = noteId,
                    judul = judul,
                    deskripsi = deskripsi,
                    matkulId = matkulId,
                    tanggal = currentDate,
                    category = selectedCategory
                )

                // Simpan catatan ke Firebase
                database.child(noteId.toString()).setValue(newNote)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Catatan berhasil ditambahkan/diperbarui", Toast.LENGTH_SHORT).show()
                        fetchNotesFromFirebase() // Ambil data catatan terbaru
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Gagal menambahkan/memperbarui catatan: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                dialog.dismiss()
            } else {
                Toast.makeText(this, "Isi semua field", Toast.LENGTH_SHORT).show()
            }
        }

        // Tombol tambah kategori baru
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

        // Tombol hapus kategori
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

    // Hapus catatan dari Firebase
    private fun deleteNoteFromFirebase(note: Note) {
        database.child(note.id.toString()).removeValue()
    }

    // Ambil data catatan dari Firebase
    private fun fetchNotesFromFirebase() {
        database.orderByChild("matkulId").equalTo(matkulId.toDouble()).addValueEventListener(object :
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val notes = snapshot.children.mapNotNull { it.getValue(Note::class.java) }
                adapter.submitList(createCategorizedList(notes)) // Tampilkan data ke RecyclerView
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@NoteActivity, "Gagal mengambil data: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // Buat daftar dengan kategori untuk RecyclerView
    private fun createCategorizedList(notes: List<Note>): List<Any> {
        return notes.groupBy { it.category }.flatMap { (category, notes) ->
            listOf(NoteAdapter.CategoryItem(category)) + notes
        }
    }

    // Buat ID unik untuk catatan
    private fun generateId(judul: String, deskripsi: String, category: String): Int {
        return "$judul$deskripsi$category".hashCode()
    }

    // Navigasi antar halaman di BottomNavigation
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

    // Kembali ke halaman sebelumnya
    override fun onBackPressed() {
        super.onBackPressed()
        val intent = Intent(this, MatkulActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        startActivity(intent)
        finish()
    }
}