package com.project.listugas.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.project.listugas.ListActivity
import com.project.listugas.databinding.ItemMatkulBinding
import com.project.listugas.entity.Matkul

class MatkulAdapter(
    private val onEditClick: (Matkul) -> Unit,
    private val onDeleteClick: (Matkul) -> Unit
) : ListAdapter<Matkul, MatkulAdapter.MatkulViewHolder>(MatkulDiffCallback()) {

    inner class MatkulViewHolder(private val binding: ItemMatkulBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(matkul: Matkul) {
            binding.nama.text = matkul.namaMatkul
            binding.deskripsi.text = matkul.deskripsi

            binding.root.setOnClickListener {
                val context = binding.root.context
                val intent = Intent(context, ListActivity::class.java)
                intent.putExtra("MATKUL_ID", matkul.id)
                context.startActivity(intent)
            }

            binding.btnEditMk.setOnClickListener {
                onEditClick(matkul)
            }

            binding.btnDeleteMk.setOnClickListener {
                onDeleteClick(matkul)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MatkulViewHolder {
        val binding = ItemMatkulBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MatkulViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MatkulViewHolder, position: Int) {
        holder.bind(getItem(position))
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
