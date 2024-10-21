package com.project.listugas

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.project.listugas.databinding.ActivityMatkulBinding
import com.project.listugas.adapter.MatkulAdapter
import com.project.listugas.entity.Matkul
import com.project.listugas.viewmodel.MatkulViewModel

class MatkulActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMatkulBinding
    private val matkulViewModel: MatkulViewModel by viewModels()
    private lateinit var adapter: MatkulAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMatkulBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapter = MatkulAdapter(
            onEditClick = { matkul -> editMatkul(matkul) },
            onDeleteClick = { matkul -> deleteMatkul(matkul) }
        )

        binding.rvMatkul.layoutManager = LinearLayoutManager(this)
        binding.rvMatkul.adapter = adapter

        matkulViewModel.allMatkuls.observe(this) { matkuls ->
            matkuls?.let {
                adapter.setMatkul(it)
            }
        }

        binding.btnMatkul.setOnClickListener {
            Log.d("MatkulActivity", "Tombol '+' diklik!") // Logging untuk debug
            val intent = Intent(this, AddMatkulActivity::class.java)
            startActivity(intent)
        }
    }

    private fun editMatkul(matkul: Matkul) {
        val intent = Intent(this,AddMatkulActivity::class.java)
        intent.putExtra("MATKUL_ID", matkul.id)
        startActivity(intent)
    }

    private fun deleteMatkul(matkul: Matkul) {
        matkulViewModel.delete(matkul)
    }
}
