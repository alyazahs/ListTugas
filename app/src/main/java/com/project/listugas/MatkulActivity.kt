package com.project.listugas

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.project.listugas.adapter.MatkulAdapter
import com.project.listugas.databinding.ActivityMatkulBinding
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
            matkuls?.let { adapter.setMatkul(it) }
        }

        binding.btnMatkul.setOnClickListener {
            showMatkulPopup()
        }
    }

    private fun showMatkulPopup(matkul: Matkul? = null) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.add_matkul, null)

        val inputNama = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.ed_nama)
        val inputDesk = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.ed_desk)

        matkul?.let {
            inputNama.setText(it.namaMatkul)
            inputDesk.setText(it.deskripsi)
        }

        val dialog = androidx.appcompat.app.AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        dialog.setCanceledOnTouchOutside(true)

        dialog.show()

        dialog.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.85).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        dialogView.findViewById<Button>(R.id.btn_submit).setOnClickListener {
            val namaMatkul = inputNama.text.toString().trim()
            val deskripsi = inputDesk.text.toString().trim()

            if (namaMatkul.isNotEmpty() && deskripsi.isNotEmpty()) {
                val newMatkul = Matkul(
                    id = matkul?.id ?: 0,
                    namaMatkul = namaMatkul,
                    deskripsi = deskripsi
                )

                if (matkul == null) {
                    matkulViewModel.insert(newMatkul)
                    Toast.makeText(this, "Matkul berhasil ditambahkan", Toast.LENGTH_SHORT).show()
                } else {
                    matkulViewModel.update(newMatkul)
                    Toast.makeText(this, "Matkul berhasil diperbarui", Toast.LENGTH_SHORT).show()
                }
                dialog.dismiss()
            } else {
                Toast.makeText(this, "Isi semua field", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun deleteMatkul(matkul: Matkul) {
        matkulViewModel.delete(matkul)
        Toast.makeText(this, "Matkul berhasil dihapus", Toast.LENGTH_SHORT).show()
    }
}