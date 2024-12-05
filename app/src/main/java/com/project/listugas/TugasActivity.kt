package com.project.listugas

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.project.listugas.databinding.ActivityTugasBinding
import com.project.listugas.adapter.TugasAdapter
import com.project.listugas.databinding.AddTugasBinding
import com.project.listugas.entity.Tugas
import com.project.listugas.viewmodel.MatkulViewModel
import com.project.listugas.viewmodel.TugasViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TugasActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTugasBinding
    private val tugasViewModel: TugasViewModel by viewModels()
    private val matkulViewModel: MatkulViewModel by viewModels()
    private lateinit var adapter: TugasAdapter
    private var matkulId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTugasBinding.inflate(layoutInflater)
        setContentView(binding.root)

        matkulId = intent.getIntExtra("MATKUL_ID", -1)

        adapter = TugasAdapter(
            onDeleteClick = { tugas -> deleteTugas(tugas) },
            onStatusChange = { tugas, isCompleted -> tugasViewModel.updateStatus(tugas.id, isCompleted) },
            onItemClick = { tugas -> showTugasPopup(tugas) }
        )

        binding.rvTugas.layoutManager = LinearLayoutManager(this)
        binding.rvTugas.adapter = adapter

        // Mengobservasi perubahan data tugas
        tugasViewModel.getTugasByMatkulId(matkulId)

        tugasViewModel.tugasList.observe(this) { tugasList ->
            tugasList?.let {
                adapter.submitTugasList(it) // Update RecyclerView dengan data terbaru
            }
        }

        // Mengambil data matkul untuk ditampilkan pada UI
        matkulViewModel.getMatkulById(matkulId).observe(this) { matkul ->
            matkul?.let {
                binding.tvName.text = it.namaMatkul
            }
        }

        // Menampilkan tanggal saat ini
        val currentDate = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()).format(Date())
        binding.tvTime.text = currentDate

        binding.btnTugas.setOnClickListener {
            showTugasPopup(null)
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
                    id = tugas?.id ?: 0,
                    matkulId = matkulId,
                    namaTugas = namaTugas,
                    isCompleted = tugas?.isCompleted ?: false
                )

                if (tugas == null) {
                    tugasViewModel.insert(newTugas)
                    Toast.makeText(this, "Tugas berhasil ditambahkan", Toast.LENGTH_SHORT).show()
                } else {
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
        tugasViewModel.delete(tugas)
        Toast.makeText(this, "Tugas berhasil dihapus", Toast.LENGTH_SHORT).show()

        // Refresh data setelah tugas dihapus
        tugasViewModel.getTugasByMatkulId(matkulId)
    }

    override fun onBackPressed() {
        // Menangani tombol back, untuk mengarahkan kembali ke List Tugas
        val intent = Intent(this, MatkulActivity::class.java) // Ganti dengan aktivitas yang menampilkan daftar tugas
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP // Clear stack aktivitas sebelumnya
        startActivity(intent)
        finish() // Menutup aktivitas TugasActivity

        super.onBackPressed() // Memanggil super untuk mempertahankan perilaku default (back stack)
    }

}
