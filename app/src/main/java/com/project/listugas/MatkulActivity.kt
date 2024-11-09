package com.project.listugas

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.project.listugas.adapter.MatkulAdapter
import com.project.listugas.databinding.ActivityMatkulBinding
import com.project.listugas.databinding.AddMatkulBinding
import com.project.listugas.entity.Matkul
import com.project.listugas.viewmodel.MatkulViewModel

class MatkulActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMatkulBinding
    private val matkulViewModel: MatkulViewModel by viewModels()
    private lateinit var adapter: MatkulAdapter

    // Daftar kategori yang sudah ada
    private val categories = mutableListOf<String>()

    // Menambahkan SharedPreferences untuk menyimpan kategori
    private val sharedPreferences by lazy {
        getSharedPreferences("MatkulPreferences", MODE_PRIVATE)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMatkulBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Ambil kategori yang disimpan di SharedPreferences saat aplikasi dibuka
        loadCategories()

        adapter = MatkulAdapter(
            onEditClick = { matkul -> showMatkulPopup(matkul) },
            onDeleteClick = { matkul -> deleteMatkul(matkul) }
        )

        binding.rvMatkul.layoutManager = LinearLayoutManager(this)
        binding.rvMatkul.adapter = adapter

        matkulViewModel.allMatkuls.observe(this) { matkuls ->
            matkuls?.let {
                val categorizedList = createCategorizedList(it)
                adapter.submitList(categorizedList)
            }
        }

        binding.btnMatkul.setOnClickListener {
            showMatkulPopup()
        }
    }

    private fun loadCategories() {
        // Ambil kategori yang sudah disimpan di SharedPreferences
        val storedCategories = sharedPreferences.getStringSet("categories", setOf())
        categories.clear()
        categories.addAll(storedCategories ?: setOf("Umum"))
    }

    private fun saveCategories() {
        // Simpan kategori yang ada ke SharedPreferences
        sharedPreferences.edit().putStringSet("categories", categories.toSet()).apply()
    }

    private fun createCategorizedList(matkuls: List<Matkul>): List<Any> {
        val categorizedList = mutableListOf<Any>()
        val groupedMatkuls = matkuls.groupBy { it.category }

        for ((category, items) in groupedMatkuls) {
            categorizedList.add(MatkulAdapter.CategoryItem(category))
            categorizedList.addAll(items)
        }

        return categorizedList
    }

    private fun showMatkulPopup(matkul: Matkul? = null) {
        val dialogBinding = AddMatkulBinding.inflate(LayoutInflater.from(this))
        val dialog = androidx.appcompat.app.AlertDialog.Builder(this)
            .setView(dialogBinding.root)
            .create()

        dialog.setCanceledOnTouchOutside(true)
        dialog.show()

        dialog.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.85).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        // Setup Spinner untuk kategori
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        dialogBinding.spinnerCategory.adapter = adapter

        // Set selected category jika sedang edit
        matkul?.let {
            dialogBinding.edNama.setText(it.namaMatkul)
            dialogBinding.edDesk.setText(it.deskripsi)
            val categoryIndex = categories.indexOf(it.category)
            dialogBinding.spinnerCategory.setSelection(categoryIndex)
        }

        // Tombol submit untuk matkul
        dialogBinding.btnSubmit.setOnClickListener {
            val namaMatkul = dialogBinding.edNama.text.toString().trim()
            val deskripsi = dialogBinding.edDesk.text.toString().trim()
            val selectedCategory = dialogBinding.spinnerCategory.selectedItem.toString()

            if (namaMatkul.isNotEmpty() && deskripsi.isNotEmpty() && selectedCategory.isNotEmpty()) {
                val newMatkul = Matkul(
                    id = matkul?.id ?: 0,
                    namaMatkul = namaMatkul,
                    deskripsi = deskripsi,
                    category = selectedCategory
                )

                if (matkul == null) {
                    matkulViewModel.insert(newMatkul)
                    Toast.makeText(this, "Matkul berhasil ditambahkan", Toast.LENGTH_SHORT).show()
                } else {
                    matkulViewModel.update(newMatkul)
                    Toast.makeText(this, "Matkul berhasil diperbarui", Toast.LENGTH_SHORT).show()
                }
                dialog.dismiss()
            } else {
                Toast.makeText(this, "Isi semua field", Toast.LENGTH_SHORT).show()
            }
        }

        // Tombol untuk menambahkan kategori baru
        dialogBinding.btnSubmitCategory.setOnClickListener {
            val newCategory = dialogBinding.edNewCategory.text.toString().trim()
            if (newCategory.isNotEmpty()) {
                categories.add(newCategory)
                saveCategories()  // Simpan kategori baru
                adapter.notifyDataSetChanged()
                dialogBinding.edNewCategory.text?.clear()
                Toast.makeText(this, "Kategori berhasil ditambahkan", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Isi nama kategori", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun deleteMatkul(matkul: Matkul) {
        matkulViewModel.delete(matkul)
        Toast.makeText(this, "Matkul ${matkul.namaMatkul} dihapus", Toast.LENGTH_SHORT).show()
    }
}
