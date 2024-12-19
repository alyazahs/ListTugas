package com.project.listugas

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.*
import com.project.listugas.adapter.MatkulAdapter
import com.project.listugas.databinding.ActivityMatkulBinding
import com.project.listugas.databinding.AddMatkulBinding
import com.project.listugas.entity.Matkul
import com.project.listugas.viewmodel.MatkulViewModel

class MatkulActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMatkulBinding
    private val matkulViewModel: MatkulViewModel by viewModels() // ViewModel untuk mengelola data Matkul
    private lateinit var adapter: MatkulAdapter // Adapter untuk RecyclerView Matkul
    private val database: DatabaseReference = FirebaseDatabase.getInstance().getReference("matkul") // Referensi ke database Firebase untuk entitas "matkul"
    private val categories = mutableListOf<String>() // Daftar kategori Matkul yang dapat dipilih pengguna

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMatkulBinding.inflate(layoutInflater) // Menggunakan view binding
        setContentView(binding.root)

        loadCategories() // Memuat kategori yang disimpan sebelumnya dari SharedPreferences
        fetchMatkulFromFirebase() // Mengambil data Matkul dari Firebase

        // Inisialisasi adapter dengan aksi untuk edit dan hapus Matkul
        adapter = MatkulAdapter(
            onEditClick = { matkul -> showMatkulPopup(matkul) }, // Membuka popup untuk edit
            onDeleteClick = { matkul -> deleteMatkul(matkul) } // Menghapus Matkul dari Firebase
        )

        // Mengatur RecyclerView dengan LinearLayoutManager
        binding.rvMatkul.layoutManager = LinearLayoutManager(this)
        binding.rvMatkul.adapter = adapter

        // Mengamati perubahan data Matkul di ViewModel dan memperbarui adapter
        matkulViewModel.allMatkuls.observe(this) { matkuls ->
            matkuls?.let {
                val categorizedList = createCategorizedList(it)
                adapter.submitList(categorizedList)
            }
        }

        // Tombol untuk menambah Matkul baru
        binding.btnMatkul.setOnClickListener {
            showMatkulPopup()
        }
    }

    // Membuat daftar Matkul terkelompok berdasarkan kategori
    private fun createCategorizedList(matkul: List<Matkul>): List<Any> {
        val categorizedList = mutableListOf<Any>()
        val groupedMatkuls = matkul.groupBy { it.category } // Mengelompokkan Matkul berdasarkan kategori

        for ((category, items) in groupedMatkuls) {
            categorizedList.add(MatkulAdapter.CategoryItem(category)) // Menambahkan header kategori
            categorizedList.addAll(items) // Menambahkan item Matkul
        }

        return categorizedList
    }

    // Menampilkan popup untuk menambah atau mengedit Matkul
    private fun showMatkulPopup(matkul: Matkul? = null) {
        val dialogBinding = AddMatkulBinding.inflate(LayoutInflater.from(this))
        val dialog = AlertDialog.Builder(this)
            .setView(dialogBinding.root)
            .create()

        dialog.setCanceledOnTouchOutside(true) // Popup ditutup jika pengguna menyentuh area luar
        dialog.show()

        // Mengatur ukuran popup agar proporsional dengan layar
        dialog.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.85).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        // Inisialisasi spinner kategori dengan daftar kategori yang tersedia
        val categoryAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        dialogBinding.spinnerCategory.adapter = categoryAdapter

        // Jika mengedit Matkul, isi field dengan data Matkul yang ada
        matkul?.let {
            dialogBinding.edNama.setText(it.namaMatkul)
            dialogBinding.edDesk.setText(it.deskripsi)
            val categoryIndex = categories.indexOf(it.category)
            dialogBinding.spinnerCategory.setSelection(categoryIndex)
        }

        // Tombol submit untuk menyimpan atau memperbarui Matkul
        dialogBinding.btnSubmit.setOnClickListener {
            val namaMatkul = dialogBinding.edNama.text.toString().trim()
            val deskripsi = dialogBinding.edDesk.text.toString().trim()
            val selectedCategory = dialogBinding.spinnerCategory.selectedItem?.toString()

            if (namaMatkul.isNotEmpty() && deskripsi.isNotEmpty() && !selectedCategory.isNullOrEmpty()) {
                val newMatkul = Matkul(
                    id = matkul?.id ?: database.push().key.hashCode(), // Jika Matkul baru, buat ID baru
                    namaMatkul = namaMatkul,
                    deskripsi = deskripsi,
                    category = selectedCategory
                )

                database.child(newMatkul.id.toString()).setValue(newMatkul) // Simpan Matkul ke Firebase
                if (matkul == null) {
                    matkulViewModel.insert(newMatkul) // Tambah Matkul baru ke ViewModel
                    Toast.makeText(this, "Matkul berhasil ditambahkan", Toast.LENGTH_SHORT).show()
                } else {
                    matkulViewModel.update(newMatkul) // Perbarui Matkul di ViewModel
                    Toast.makeText(this, "Matkul berhasil diperbarui", Toast.LENGTH_SHORT).show()
                }
                dialog.dismiss() // Tutup popup setelah selesai
            } else {
                Toast.makeText(this, "Isi semua field", Toast.LENGTH_SHORT).show()
            }
        }

        // Tombol untuk menambah kategori baru
        dialogBinding.btnSubmitCategory.setOnClickListener {
            val newCategory = dialogBinding.edNewCategory.text.toString().trim()
            if (newCategory.isNotEmpty()) {
                categories.add(newCategory)
                saveCategories()
                categoryAdapter.notifyDataSetChanged() // Perbarui adapter spinner
                dialogBinding.edNewCategory.text?.clear()
                Toast.makeText(this, "Kategori berhasil ditambahkan", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Isi nama kategori", Toast.LENGTH_SHORT).show()
            }
        }

        // Tombol untuk menghapus kategori
        dialogBinding.btnDeleteCategory.setOnClickListener {
            val categoryToDelete = dialogBinding.spinnerCategory.selectedItem?.toString()
            if (!categoryToDelete.isNullOrEmpty() && categoryToDelete != "Umum") {
                categories.remove(categoryToDelete)
                saveCategories()
                categoryAdapter.notifyDataSetChanged()
                Toast.makeText(this, "Kategori berhasil dihapus", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Kategori 'Umum' tidak dapat dihapus", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Mengambil data Matkul dari Firebase
    private fun fetchMatkulFromFirebase() {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val matkuls = snapshot.children.mapNotNull { it.getValue(Matkul::class.java) }
                val categorizedList = createCategorizedList(matkuls)
                adapter.submitList(categorizedList) // Perbarui daftar di adapter
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@MatkulActivity, "Gagal mengambil data: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // Menghapus Matkul dari Firebase
    private fun deleteMatkul(matkul: Matkul) {
        database.child(matkul.id.toString()).removeValue().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                matkulViewModel.delete(matkul) // Hapus Matkul dari ViewModel
                Toast.makeText(this, "Matkul ${matkul.namaMatkul} dihapus", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Gagal menghapus data dari Firebase", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Menyimpan kategori ke SharedPreferences
    private fun saveCategories() {
        val sharedPreferences = getSharedPreferences("CategoriesPrefs", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putStringSet("categories", categories.toSet())
        editor.apply()
    }

    // Memuat kategori dari SharedPreferences
    private fun loadCategories() {
        val sharedPreferences = getSharedPreferences("CategoriesPrefs", MODE_PRIVATE)
        val savedCategories = sharedPreferences.getStringSet("categories", setOf())
        categories.clear()
        savedCategories?.let {
            categories.addAll(it)
        }
    }
}
