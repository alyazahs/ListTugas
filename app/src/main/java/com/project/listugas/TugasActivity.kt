package com.project.listugas

import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.project.listugas.adapter.TugasAdapter
import com.project.listugas.databinding.ActivityTugasBinding
import com.project.listugas.databinding.AddTugasBinding
import com.project.listugas.entity.Tugas
import com.project.listugas.viewmodel.MatkulViewModel
import com.project.listugas.viewmodel.TugasViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TugasActivity : AppCompatActivity(), BottomNavigationView.OnNavigationItemSelectedListener {

    private lateinit var binding: ActivityTugasBinding
    private val tugasViewModel: TugasViewModel by viewModels()
    private val matkulViewModel: MatkulViewModel by viewModels()
    private lateinit var adapter: TugasAdapter
    private var matkulName: String = ""
    private var matkulId: Int = -1
    private val database: DatabaseReference = FirebaseDatabase.getInstance().getReference("tugas")

    // SQLite Database
    private lateinit var localDatabase: SQLiteDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTugasBinding.inflate(layoutInflater)
        setContentView(binding.root)

        matkulName = intent.getStringExtra("MATKUL_NAME") ?: ""
        matkulId = intent.getIntExtra("MATKUL_ID", -1)

        // Inisialisasi SQLite Database
        localDatabase = openOrCreateDatabase("listugas.db", MODE_PRIVATE, null)
        createTableIfNotExists()

        adapter = TugasAdapter(
            onDeleteClick = { tugas -> deleteTugas(tugas) },
            onStatusChange = { tugas, isCompleted -> updateTugasStatusInFirebase(tugas, isCompleted) },
            onItemClick = { tugas -> showTugasPopup(tugas) }
        )

        binding.rvTugas.layoutManager = LinearLayoutManager(this)
        binding.rvTugas.adapter = adapter

        observeViewModel()
        fetchTugasFromFirebase()

        val currentDate = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()).format(Date())
        binding.tvTime.text = currentDate

        binding.btnTugas.setOnClickListener {
            showTugasPopup(null)
        }

        binding.bottomNavigation.setOnNavigationItemSelectedListener(this)
        binding.bottomNavigation.selectedItemId = R.id.action_todo // Set selected item in BottomNavigationView
    }

    private fun createTableIfNotExists() {
        val createTableQuery = """
            CREATE TABLE IF NOT EXISTS tugas (
                id INTEGER PRIMARY KEY,
                matkul_id INTEGER,
                matkul_name TEXT,
                nama_tugas TEXT,
                is_completed INTEGER
            )
        """
        localDatabase.execSQL(createTableQuery)
    }

    private fun observeViewModel() {
        matkulViewModel.getMatkulById(matkulId).observe(this) { matkul ->
            matkul?.let {
                binding.tvName.text = it.namaMatkul
            }
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        val intent = Intent(this, MatkulActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        startActivity(intent)
        finish()
    }

    private fun showTugasPopup(tugas: Tugas?) {
        val dialogBinding = AddTugasBinding.inflate(LayoutInflater.from(this))
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

        tugas?.let {
            dialogBinding.edTugas.setText(it.namaTugas)
        }

        dialogBinding.btnSubmit.setOnClickListener {
            val namaTugas = dialogBinding.edTugas.text.toString().trim()

            if (namaTugas.isNotEmpty()) {
                val newTugas = Tugas(
                    id = tugas?.id ?: generateId(namaTugas, matkulName),
                    matkulName = matkulName,
                    matkulId = matkulId,
                    namaTugas = namaTugas,
                    isCompleted = tugas?.isCompleted ?: false
                )

                if (tugas == null) {
                    insertTugasToLocalDatabase(newTugas)
                    val key = database.push().key
                    key?.let {
                        newTugas.id = key.hashCode()
                        database.child(newTugas.id.toString()).setValue(newTugas)
                        tugasViewModel.insert(newTugas)
                        Toast.makeText(this, "Tugas berhasil ditambahkan", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    updateTugasInLocalDatabase(newTugas)
                    database.child(tugas.id.toString()).setValue(newTugas)
                    tugasViewModel.update(newTugas)
                    Toast.makeText(this, "Tugas diperbarui", Toast.LENGTH_SHORT).show()
                }
                dialog.dismiss()
            } else {
                Toast.makeText(this, "Isi semua field", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun deleteTugas(tugas: Tugas) {
        deleteTugasFromLocalDatabase(tugas)
        database.child(tugas.id.toString()).removeValue()
        tugasViewModel.delete(tugas)
        Toast.makeText(this, "Tugas berhasil dihapus", Toast.LENGTH_SHORT).show()
    }

    private fun fetchTugasFromFirebase() {
        database.orderByChild("matkulId").equalTo(matkulId.toDouble()).addValueEventListener(object :
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val tugasList = mutableListOf<Tugas>()
                snapshot.children.forEach { dataSnapshot ->
                    val tugas = dataSnapshot.getValue(Tugas::class.java)
                    tugas?.let {
                        tugasList.add(it)
                        insertTugasToLocalDatabase(it) // Simpan ke SQLite
                    }
                }
                adapter.submitList(tugasList)
            }

            override fun onCancelled(error: DatabaseError) {
                val tugasList = fetchTugasFromLocalDatabase() // Gunakan SQLite jika gagal
                adapter.submitList(tugasList)
            }
        })
    }

    private fun updateTugasStatusInFirebase(tugas: Tugas, isCompleted: Boolean) {
        val updatedTugas = tugas.copy(isCompleted = isCompleted)
        updateTugasInLocalDatabase(updatedTugas)
        database.child(tugas.id.toString()).setValue(updatedTugas)
        tugasViewModel.update(updatedTugas)
    }

    private fun fetchTugasFromLocalDatabase(): List<Tugas> {
        val tugasList = mutableListOf<Tugas>()
        val cursor = localDatabase.rawQuery("SELECT * FROM tugas WHERE matkul_id = ?", arrayOf(matkulId.toString()))

        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getInt(cursor.getColumnIndexOrThrow("id"))
                val matkulId = cursor.getInt(cursor.getColumnIndexOrThrow("matkul_id"))
                val matkulName = cursor.getString(cursor.getColumnIndexOrThrow("matkul_name"))
                val namaTugas = cursor.getString(cursor.getColumnIndexOrThrow("nama_tugas"))
                val isCompleted = cursor.getInt(cursor.getColumnIndexOrThrow("is_completed")) == 1

                tugasList.add(Tugas(id, matkulName, matkulId, namaTugas, isCompleted))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return tugasList
    }

    private fun insertTugasToLocalDatabase(tugas: Tugas) {
        val query = """
            INSERT OR REPLACE INTO tugas (id, matkul_id, matkul_name, nama_tugas, is_completed)
            VALUES (?, ?, ?, ?, ?)
        """
        localDatabase.execSQL(query, arrayOf(tugas.id, tugas.matkulId, tugas.matkulName, tugas.namaTugas, if (tugas.isCompleted) 1 else 0))
    }

    private fun updateTugasInLocalDatabase(tugas: Tugas) {
        val query = """
            UPDATE tugas SET nama_tugas = ?, is_completed = ? WHERE id = ?
        """
        localDatabase.execSQL(query, arrayOf(tugas.namaTugas, if (tugas.isCompleted) 1 else 0, tugas.id))
    }

    private fun deleteTugasFromLocalDatabase(tugas: Tugas) {
        localDatabase.execSQL("DELETE FROM tugas WHERE id = ?", arrayOf(tugas.id))
    }

    private fun generateId(namaTugas: String, matkulName: String): Int {
        val combinedData = "$namaTugas$matkulName"
        return combinedData.hashCode()
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_note -> {
                val intent = Intent(this, NoteActivity::class.java)
                intent.putExtra("MATKUL_ID", matkulId)
                intent.putExtra("MATKUL_NAME", matkulName)
                startActivity(intent)
                finish()
                return true
            }
            R.id.action_todo -> {
                return true
            }
        }
        return false
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::localDatabase.isInitialized) {
            localDatabase.close()
        }
    }
}
