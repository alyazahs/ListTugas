package com.project.listugas

import android.content.Intent
import android.os.Bundle
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.project.listugas.databinding.ActivityTugasBinding
import com.project.listugas.adapter.TugasAdapter
import com.project.listugas.entity.Tugas
import com.project.listugas.viewmodel.TugasViewModel

class TugasActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTugasBinding
    private val tugasViewModel: TugasViewModel by viewModels()
    private lateinit var adapter: TugasAdapter
    private var matkulId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTugasBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Mendapatkan ID Matkul dari intent
        matkulId = intent.getIntExtra("MATKUL_ID", -1)

        // Menginisialisasi adapter dengan fungsi delete dan status change
        adapter = TugasAdapter(
            onDeleteClick = { tugas ->
                deleteTugas(tugas)
            },
            onStatusChange = { tugas, isCompleted ->
                tugasViewModel.updateStatus(tugas.id, isCompleted)
            },
            onItemClick = { tugas ->
                showTugasPopup(tugas) // Menampilkan popup untuk tugas yang dipilih
            }
        )

        // Mengatur RecyclerView
        binding.rvTugas.layoutManager = LinearLayoutManager(this)
        binding.rvTugas.adapter = adapter

        // Mengamati perubahan data tugas berdasarkan Matkul ID
        tugasViewModel.getTugasByMatkulId(matkulId).observe(this) { tugasList ->
            tugasList?.let {
                adapter.setTugas(it)
            }
        }

        // Menampilkan popup untuk menambah tugas baru
        binding.btnTugas.setOnClickListener {
            showTugasPopup(null) // null berarti tambah tugas baru
        }
    }

    // Fungsi untuk menampilkan popup tugas (baik untuk tambah maupun edit)
    private fun showTugasPopup(tugas: Tugas?) {
        val dialogView = layoutInflater.inflate(R.layout.add_tugas, null)

        // Inisialisasi input field
        val inputNama = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.ed_tugas)

        // Jika tugas tidak null (mengedit), isi field dengan nama tugas yang ada
        tugas?.let {
            inputNama.setText(it.namaTugas)
        }

        // Buat AlertDialog dan set view
        val dialog = androidx.appcompat.app.AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        // Pastikan popup bisa ditutup dengan mengklik di luar area
        dialog.setCanceledOnTouchOutside(true)

        // Tampilkan dialog
        dialog.show()

        // Atur ukuran popup
        dialog.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.85).toInt(),  // Lebar 85% layar
            ViewGroup.LayoutParams.WRAP_CONTENT                      // Tinggi otomatis sesuai konten
        )
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)  // Background transparan

        // Tombol submit di dalam popup
        dialogView.findViewById<Button>(R.id.btn_submit).setOnClickListener {
            val namaTugas = inputNama.text.toString().trim()

            // Jika nama tugas diisi, lanjutkan
            if (namaTugas.isNotEmpty()) {
                val newTugas = Tugas(
                    id = tugas?.id ?: 0,        // Jika null, berarti tugas baru (id 0)
                    matkulId = matkulId,         // Menggunakan ID matkul yang diterima dari intent
                    namaTugas = namaTugas,
                    isCompleted = tugas?.isCompleted ?: false // Status awal: belum selesai
                )

                // Jika tugas ada (mengedit), cukup gunakan insert untuk memasukkan tugas baru
                if (tugas == null) {
                    tugasViewModel.insert(newTugas)
                    Toast.makeText(this, "Tugas berhasil ditambahkan", Toast.LENGTH_SHORT).show()
                } else {
                    // Mengupdate status selesai
                    tugasViewModel.update(newTugas)
                    Toast.makeText(this, "Tugas diperbarui", Toast.LENGTH_SHORT).show()
                }

                dialog.dismiss()  // Tutup popup setelah submit
            } else {
                Toast.makeText(this, "Isi semua field", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Fungsi untuk menghapus tugas
    private fun deleteTugas(tugas: Tugas) {
        tugasViewModel.delete(tugas)
        Toast.makeText(this, "Tugas berhasil dihapus", Toast.LENGTH_SHORT).show()
    }
}
