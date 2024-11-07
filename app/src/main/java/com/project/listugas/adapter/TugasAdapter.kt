package com.project.listugas.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.project.listugas.databinding.ItemTugasBinding
import com.project.listugas.entity.Tugas

class TugasAdapter(
    private val onDeleteClick: (Tugas) -> Unit,
    private val onStatusChange: (Tugas, Boolean) -> Unit,
    private val onItemClick: (Tugas) -> Unit
) : ListAdapter<Tugas, TugasAdapter.TugasViewHolder>(TugasDiffCallback()) {

    inner class TugasViewHolder(private val binding: ItemTugasBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(tugas: Tugas) {
            binding.namaTugas.text = tugas.namaTugas
            binding.checkboxTask.isChecked = tugas.isCompleted

            binding.checkboxTask.setOnCheckedChangeListener { _, isChecked ->
                onStatusChange(tugas, isChecked)
            }

            binding.btnDeleteTgs.setOnClickListener {
                onDeleteClick(tugas)
            }

            binding.root.setOnClickListener {
                onItemClick(tugas)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TugasViewHolder {
        val binding = ItemTugasBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TugasViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TugasViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class TugasDiffCallback : DiffUtil.ItemCallback<Tugas>() {
        override fun areItemsTheSame(oldItem: Tugas, newItem: Tugas): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Tugas, newItem: Tugas): Boolean {
            return oldItem == newItem
        }
    }
}
