package com.project.listugas

import android.content.Intent
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTugasBinding.inflate(layoutInflater)
        setContentView(binding.root)

        matkulName = intent.getStringExtra("MATKUL_NAME") ?: ""
        matkulId = intent.getIntExtra("MATKUL_ID", -1)

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
                    namaTugas = namaTugas,
                    isCompleted = tugas?.isCompleted ?: false
                )

                if (tugas == null) {
                    val key = database.push().key
                    key?.let {
                        newTugas.id = key.hashCode()
                        database.child(newTugas.id.toString()).setValue(newTugas)
                        tugasViewModel.insert(newTugas)
                        Toast.makeText(this, "Tugas berhasil ditambahkan", Toast.LENGTH_SHORT).show()
                    }
                } else {
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
        database.child(tugas.id.toString()).removeValue()
        tugasViewModel.delete(tugas)
        Toast.makeText(this, "Tugas berhasil dihapus", Toast.LENGTH_SHORT).show()
    }

    private fun fetchTugasFromFirebase() {
        database.orderByChild("matkulName").equalTo(matkulName).addValueEventListener(object :
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val tugasList = mutableListOf<Tugas>()
                snapshot.children.forEach { dataSnapshot ->
                    val tugas = dataSnapshot.getValue(Tugas::class.java)
                    tugas?.let { tugasList.add(it) }
                }
                adapter.submitList(tugasList)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@TugasActivity, "Gagal mengambil data: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun updateTugasStatusInFirebase(tugas: Tugas, isCompleted: Boolean) {
        val updatedTugas = tugas.copy(isCompleted = isCompleted)
        database.child(tugas.id.toString()).setValue(updatedTugas)
        tugasViewModel.update(updatedTugas)
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
                return true
            }
            R.id.action_todo -> {
                val intent = Intent(this, TugasActivity::class.java)
                intent.putExtra("MATKUL_ID", matkulId)
                intent.putExtra("MATKUL_NAME", matkulName)
                startActivity(intent)
                return true
            }
        }
        return false
    }
}
