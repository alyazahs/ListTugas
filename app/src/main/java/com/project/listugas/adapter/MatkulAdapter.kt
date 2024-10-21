package com.project.listugas.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.project.ListTugas.R
import com.project.listugas.entity.Matkul

class MatkulAdapter(
    private val onEditClick: (Matkul) -> Unit,
    private val onDeleteClick: (Matkul) -> Unit
) : RecyclerView.Adapter<MatkulAdapter.MatkulViewHolder>() {

    private var matkuls = listOf<Matkul>()

    inner class MatkulViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nama: TextView = itemView.findViewById(R.id.nama)
        val deskripsi: TextView = itemView.findViewById(R.id.deskripsi)
        val btnEdit: Button = itemView.findViewById(R.id.btn_editMk)
        val btnDelete: Button = itemView.findViewById(R.id.btn_deleteMk)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MatkulViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_matkul, parent, false)
        return MatkulViewHolder(view)
    }

    override fun onBindViewHolder(holder: MatkulViewHolder, position: Int) {
        val matkul = matkuls[position]
        holder.nama.text = matkul.namaMatkul
        holder.deskripsi.text = matkul.deskripsi

        holder.itemView.setOnClickListener {
            // Mengarahkan ke TugasActivity saat matkul diklik
            val context = holder.itemView.context
            val intent = Intent(context, ListActivity::class.java)
            intent.putExtra("MATKUL_ID", matkul.id) // Kirim ID matkul ke TugasActivity
            context.startActivity(intent)
        }

        holder.btnEdit.setOnClickListener {
            onEditClick(matkul)
        }

        holder.btnDelete.setOnClickListener {
            onDeleteClick(matkul)
        }
    }

    override fun getItemCount(): Int = matkuls.size

    fun setMatkul(matkuls: List<Matkul>) {
        this.matkuls = matkuls
        notifyDataSetChanged()
    }
}