package com.project.listugas

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.project.ListTugas.databinding.ActivityTugasBinding
import com.project.listugas.adapter.TugasAdapter
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

        matkulId = intent.getIntExtra("MATKUL_ID", -1)

        adapter = TugasAdapter(
            onDeleteClick = { tugas ->
                tugasViewModel.delete(tugas)
            },
            onStatusChange = { tugas, isCompleted ->
                tugas.isCompleted = isCompleted
                tugasViewModel.updateStatus(tugas.id, isCompleted)
            }
        )

        binding.rvTugas.layoutManager = LinearLayoutManager(this)
        binding.rvTugas.adapter = adapter

        tugasViewModel.getTugasByMatkulId(matkulId).observe(this) { tugasList ->
            tugasList?.let {
                adapter.setTugas(it)
            }
        }

        binding.btnTugas.setOnClickListener {
            val intent = Intent(this, AddTugasActivity::class.java)
            intent.putExtra("MATKUL_ID", matkulId)
            startActivity(intent)
        }
    }
}