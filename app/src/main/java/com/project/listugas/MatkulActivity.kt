package com.project.listugas

import android.database.sqlite.SQLiteDatabase
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
    private val matkulViewModel: MatkulViewModel by viewModels()
    private lateinit var adapter: MatkulAdapter
    private val firebaseDatabase: DatabaseReference = FirebaseDatabase.getInstance().getReference("matkul")

    private lateinit var database: SQLiteDatabase
    private val DATABASE_NAME = "matkul.db"
    private val TABLE_MATKUL = "matkul"
    private val COLUMN_ID = "id"
    private val COLUMN_NAME = "namaMatkul"
    private val COLUMN_DESCRIPTION = "deskripsi"
    private val COLUMN_CATEGORY = "category"

    private val categories = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMatkulBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Membuat atau membuka database SQLite
        database = openOrCreateDatabase(DATABASE_NAME, MODE_PRIVATE, null)
        createTableIfNotExists()

        loadCategories()
        loadMatkulsFromLocal()
        fetchMatkulFromFirebase()

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

        binding.btnaddMatkul.setOnClickListener {
            showMatkulPopup()
        }
    }

    private fun createTableIfNotExists() {
        val createTableQuery = """
            CREATE TABLE IF NOT EXISTS $TABLE_MATKUL (
                $COLUMN_ID INTEGER PRIMARY KEY,
                $COLUMN_NAME TEXT NOT NULL,
                $COLUMN_DESCRIPTION TEXT NOT NULL,
                $COLUMN_CATEGORY TEXT NOT NULL
            )
        """.trimIndent()
        database.execSQL(createTableQuery)
    }

    private fun createCategorizedList(matkul: List<Matkul>): List<Any> {
        val categorizedList = mutableListOf<Any>()
        val groupedMatkuls = matkul.groupBy { it.category }

        for ((category, items) in groupedMatkuls) {
            categorizedList.add(MatkulAdapter.CategoryItem(category))
            categorizedList.addAll(items)
        }

        return categorizedList
    }

    private fun showMatkulPopup(matkul: Matkul? = null) {
        val dialogBinding = AddMatkulBinding.inflate(LayoutInflater.from(this))
        val dialog = AlertDialog.Builder(this)
            .setView(dialogBinding.root)
            .create()

        dialog.setCanceledOnTouchOutside(true)
        dialog.show()

        dialog.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.85).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val categoryAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        dialogBinding.spinnerCategory.adapter = categoryAdapter

        matkul?.let {
            dialogBinding.edNama.setText(it.namaMatkul)
            dialogBinding.edDesk.setText(it.deskripsi)
            val categoryIndex = categories.indexOf(it.category)
            dialogBinding.spinnerCategory.setSelection(categoryIndex)
        }

        dialogBinding.btnSubmit.setOnClickListener {
            val namaMatkul = dialogBinding.edNama.text.toString().trim()
            val deskripsi = dialogBinding.edDesk.text.toString().trim()
            val selectedCategory = dialogBinding.spinnerCategory.selectedItem?.toString()

            if (namaMatkul.isNotEmpty() && deskripsi.isNotEmpty() && !selectedCategory.isNullOrEmpty()) {
                val newMatkul = Matkul(
                    id = matkul?.id ?: System.currentTimeMillis().toInt(),
                    namaMatkul = namaMatkul,
                    deskripsi = deskripsi,
                    category = selectedCategory
                )

                firebaseDatabase.child(newMatkul.id.toString()).setValue(newMatkul)
                insertMatkulToLocal(newMatkul)

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

        dialogBinding.btnSubmitCategory.setOnClickListener {
            val newCategory = dialogBinding.edNewCategory.text.toString().trim()
            if (newCategory.isNotEmpty()) {
                categories.add(newCategory)
                saveCategories()
                categoryAdapter.notifyDataSetChanged()
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
                categoryAdapter.notifyDataSetChanged()
                Toast.makeText(this, "Kategori berhasil dihapus", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Kategori 'Umum' tidak dapat dihapus", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun fetchMatkulFromFirebase() {
        firebaseDatabase.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val matkuls = snapshot.children.mapNotNull { it.getValue(Matkul::class.java) }
                matkuls.forEach { matkul ->
                    matkulViewModel.insert(matkul)
                    insertMatkulToLocal(matkul)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@MatkulActivity, "Gagal mengambil data: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun insertMatkulToLocal(matkul: Matkul) {
        val query = """
            INSERT OR REPLACE INTO $TABLE_MATKUL ($COLUMN_ID, $COLUMN_NAME, $COLUMN_DESCRIPTION, $COLUMN_CATEGORY)
            VALUES (?, ?, ?, ?)
        """.trimIndent()
        database.execSQL(query, arrayOf(matkul.id, matkul.namaMatkul, matkul.deskripsi, matkul.category))
    }

    private fun loadMatkulsFromLocal() {
        val matkuls = mutableListOf<Matkul>()
        val cursor = database.rawQuery("SELECT * FROM $TABLE_MATKUL", null)
        cursor.use {
            while (it.moveToNext()) {
                val matkul = Matkul(
                    id = it.getInt(it.getColumnIndexOrThrow(COLUMN_ID)),
                    namaMatkul = it.getString(it.getColumnIndexOrThrow(COLUMN_NAME)),
                    deskripsi = it.getString(it.getColumnIndexOrThrow(COLUMN_DESCRIPTION)),
                    category = it.getString(it.getColumnIndexOrThrow(COLUMN_CATEGORY))
                )
                matkuls.add(matkul)
            }
        }
        matkuls.forEach { matkul ->
            matkulViewModel.insert(matkul) // Insert satu per satu
        }
    }

    private fun deleteMatkul(matkul: Matkul) {
        firebaseDatabase.child(matkul.id.toString()).removeValue().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                matkulViewModel.delete(matkul)
                deleteMatkulFromLocal(matkul.id)
                Toast.makeText(this, "Matkul ${matkul.namaMatkul} dihapus", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Gagal menghapus data", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun deleteMatkulFromLocal(matkulId: Int) {
        val query = "DELETE FROM $TABLE_MATKUL WHERE $COLUMN_ID = ?"
        database.execSQL(query, arrayOf(matkulId))
    }

    private fun loadCategories() {
        val sharedPreferences = getSharedPreferences("CategoriesPrefs", MODE_PRIVATE)
        val savedCategories = sharedPreferences.getStringSet("categories", setOf())
        categories.clear()
        savedCategories?.let { categories.addAll(it) }
    }

    private fun saveCategories() {
        val sharedPreferences = getSharedPreferences("CategoriesPrefs", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putStringSet("categories", categories.toSet())
        editor.apply()
    }
}
