package com.project.listugas.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.project.listugas.databinding.ItemHeaderBinding
import com.project.listugas.databinding.ItemTugasBinding
import com.project.listugas.entity.Tugas

class TugasAdapter(
    private val onDeleteClick: (Tugas) -> Unit,
    private val onStatusChange: (Tugas, Boolean) -> Unit,
    private val onItemClick: (Tugas) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val tasks = mutableListOf<Any>()

    companion object {
        private const val VIEW_TYPE_HEADER = 0
        private const val VIEW_TYPE_TASK = 1
    }

    fun submitList(tugasList: List<Tugas>) {
        tasks.clear()
        tasks.add("Belum Selesai")
        tasks.addAll(tugasList.filter { !it.isCompleted })
        tasks.add("Selesai")
        tasks.addAll(tugasList.filter { it.isCompleted })
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int {
        return if (tasks[position] is String) VIEW_TYPE_HEADER else VIEW_TYPE_TASK
    }

    override fun getItemCount(): Int = tasks.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_HEADER) {
            val binding = ItemHeaderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            HeaderViewHolder(binding)
        } else {
            val binding = ItemTugasBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            TugasViewHolder(binding)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is TugasViewHolder) {
            holder.bind(tasks[position] as Tugas)
        } else if (holder is HeaderViewHolder) {
            holder.bind(tasks[position] as String)
        }
    }

    inner class TugasViewHolder(private val binding: ItemTugasBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(tugas: Tugas) {
            binding.namaTugas.text = tugas.namaTugas

            binding.checkboxTask.setOnCheckedChangeListener(null)
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

    inner class HeaderViewHolder(private val binding: ItemHeaderBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(header: String) {
            binding.headerTitle.text = header
        }
    }
}