package com.project.listugas

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
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
            onDeleteClick = { tugas ->
                deleteTugas(tugas)
            },
            onStatusChange = { tugas, isCompleted ->
                tugasViewModel.updateStatus(tugas.id, isCompleted)
            },
            onItemClick = { tugas ->
                showTugasPopup(tugas)
            }
        )

        binding.rvTugas.layoutManager = LinearLayoutManager(this)
        binding.rvTugas.adapter = adapter

        tugasViewModel.getTugasByMatkulId(matkulId).observe(this) { tugasList ->
            tugasList?.let {
                adapter.submitList(it)
            }
        }

        matkulViewModel.getMatkulById(matkulId).observe(this) { matkul ->
            matkul?.let {
                binding.tvName.text = it.namaMatkul
            }
        }

        val currentDate = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()).format(Date())
        binding.tvTime.text = currentDate

        binding.btnTugas.setOnClickListener {
            showTugasPopup(null)
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
    }
}