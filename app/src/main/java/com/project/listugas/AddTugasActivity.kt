package com.project.listugas

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.project.ListTugas.databinding.AddTugasBinding
import com.project.listugas.entity.Tugas
import com.project.listugas.viewmodel.TugasViewModel

class AddTugasActivity : AppCompatActivity() {

    private lateinit var binding: AddTugasBinding
    private val tugasViewModel: TugasViewModel by viewModels()
    private var matkulId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = AddTugasBinding.inflate(layoutInflater)
        setContentView(binding.root)

        matkulId = intent.getIntExtra("MATKUL_ID", -1)

        binding.btnSubmit.setOnClickListener {
            val namaTugas = binding.edTugas.text.toString().trim()

            if (namaTugas.isNotEmpty() && matkulId != -1) {
                val tugas = Tugas(matkulId = matkulId, namaTugas = namaTugas, isCompleted = false)
                tugasViewModel.insert(tugas)
                Toast.makeText(this, "Tugas berhasil ditambahkan", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "Isi semua field", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
