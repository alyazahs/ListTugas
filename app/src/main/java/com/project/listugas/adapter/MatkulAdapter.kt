package com.project.listugas.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.project.listugas.ListActivity
import com.project.listugas.R
import com.project.listugas.entity.Matkul

class MatkulAdapter(
    private val onEditClick: (Matkul) -> Unit,
    private val onDeleteClick: (Matkul) -> Unit
) : ListAdapter<Matkul, MatkulAdapter.MatkulViewHolder>(MatkulDiffCallback()) {

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
        val matkul = getItem(position)
        holder.nama.text = matkul.namaMatkul
        holder.deskripsi.text = matkul.deskripsi

        holder.itemView.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, ListActivity::class.java)
            intent.putExtra("MATKUL_ID", matkul.id)
            context.startActivity(intent)
        }

        holder.btnEdit.setOnClickListener {
            onEditClick(matkul)
        }

        holder.btnDelete.setOnClickListener {
            onDeleteClick(matkul)
        }
    }

    class MatkulDiffCallback : DiffUtil.ItemCallback<Matkul>() {
        override fun areItemsTheSame(oldItem: Matkul, newItem: Matkul): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Matkul, newItem: Matkul): Boolean {
            return oldItem == newItem
        }
    }
}