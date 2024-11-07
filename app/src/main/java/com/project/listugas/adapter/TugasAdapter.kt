package com.project.listugas.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.project.listugas.R
import com.project.listugas.entity.Tugas

class TugasAdapter(
    private val onDeleteClick: (Tugas) -> Unit,
    private val onStatusChange: (Tugas, Boolean) -> Unit,
    private val onItemClick: (Tugas) -> Unit
) : ListAdapter<Tugas, TugasAdapter.TugasViewHolder>(TugasDiffCallback()) {

    inner class TugasViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nama: TextView = itemView.findViewById(R.id.namaTugas)
        val checkBox: CheckBox = itemView.findViewById(R.id.checkbox_task)
        val btnDelete: Button = itemView.findViewById(R.id.btn_deleteTgs)

        init {
            itemView.setOnClickListener {
                val tugas = getItem(adapterPosition)
                onItemClick(tugas)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TugasViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_tugas, parent, false)
        return TugasViewHolder(view)
    }

    override fun onBindViewHolder(holder: TugasViewHolder, position: Int) {
        val tugas = getItem(position)
        holder.nama.text = tugas.namaTugas
        holder.checkBox.isChecked = tugas.isCompleted

        holder.checkBox.setOnCheckedChangeListener { _, isChecked ->
            onStatusChange(tugas, isChecked)
        }

        holder.btnDelete.setOnClickListener {
            onDeleteClick(tugas)
        }
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