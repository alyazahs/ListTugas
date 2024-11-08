package com.project.listugas

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.project.listugas.adapter.MatkulAdapter
import com.project.listugas.adapter.MatkulAdapter.CategoryItem
import com.project.listugas.databinding.ActivityMatkulBinding
import com.project.listugas.databinding.AddMatkulBinding
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

        binding.btnMatkul.setOnClickListener {
            showMatkulPopup()
        }
    }

    private fun createCategorizedList(matkuls: List<Matkul>): List<Any> {
        val categorizedList = mutableListOf<Any>()
        val groupedMatkuls = matkuls.groupBy { it.category }

        for ((category, items) in groupedMatkuls) {
            categorizedList.add(CategoryItem(category))
            categorizedList.addAll(items)
        }

        return categorizedList
    }

    private fun showMatkulPopup(matkul: Matkul? = null) {
        // Functionality for showing the popup to add or edit Matkul
    }

    private fun deleteMatkul(matkul: Matkul) {
        matkulViewModel.delete(matkul)
        Toast.makeText(this, "Matkul berhasil dihapus", Toast.LENGTH_SHORT).show()
    }
}
