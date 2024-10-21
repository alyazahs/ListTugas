package com.project.listugas

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.project.listugas.databinding.AddMatkulBinding
import com.project.listugas.entity.Matkul
import com.project.listugas.viewmodel.MatkulViewModel

class AddMatkulActivity : AppCompatActivity() {

    private lateinit var binding: AddMatkulBinding
    private val matkulViewModel: MatkulViewModel by viewModels()
    private var matkulId: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = AddMatkulBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Ambil matkulId dari intent (jika ada)
        matkulId = intent.getIntExtra("MATKUL_ID", -1).takeIf { it != -1 }
        matkulId?.let {
            // Jika matkulId tidak null, artinya kita sedang mengedit matkul yang sudah ada
            matkulViewModel.getMatkulById(it).observe(this) { matkul ->
                matkul?.let {
                    binding.edNama.setText(matkul.namaMatkul)
                    binding.edDesk.setText(matkul.deskripsi)
                }
            }
        }

        // Tombol untuk menyimpan atau memperbarui matkul
        binding.btnSubmit.setOnClickListener {
            val namaMatkul = binding.edNama.text.toString().trim()
            val deskripsi = binding.edDesk.text.toString().trim()

            // Validasi input pengguna
            if (namaMatkul.isNotEmpty() && deskripsi.isNotEmpty()) {
                val matkul = Matkul(
                    id = matkulId ?: 0, // Jika matkulId ada, artinya kita mengedit; jika tidak, kita menambah matkul baru
                    namaMatkul = namaMatkul,
                    deskripsi = deskripsi
                )

                // Jika matkulId tidak null, kita update matkul
                if (matkulId != null) {
                    matkulViewModel.update(matkul)
                    Toast.makeText(this, "Matkul berhasil diperbarui", Toast.LENGTH_SHORT).show()
                } else {
                    // Jika null, berarti menambah matkul baru
                    matkulViewModel.insert(matkul)
                    Toast.makeText(this, "Matkul berhasil ditambahkan", Toast.LENGTH_SHORT).show()
                }
                // Kembali ke layar sebelumnya setelah menyimpan
                finish()
            } else {
                // Jika ada field yang kosong
                Toast.makeText(this, "Isi semua field", Toast.LENGTH_SHORT).show()
            }
        }
    }
}