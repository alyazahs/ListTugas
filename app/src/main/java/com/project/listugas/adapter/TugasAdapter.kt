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
) : ListAdapter<Any, RecyclerView.ViewHolder>(TugasDiffCallback()) {

    companion object {
        private const val VIEW_TYPE_HEADER = 0
        private const val VIEW_TYPE_TASK = 1
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is String -> VIEW_TYPE_HEADER
            is Tugas -> VIEW_TYPE_TASK
            else -> throw IllegalArgumentException("Unknown item type")
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_HEADER -> {
                val binding = ItemHeaderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                HeaderViewHolder(binding)
            }
            VIEW_TYPE_TASK -> {
                val binding = ItemTugasBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                TugasViewHolder(binding)
            }
            else -> throw IllegalArgumentException("Unknown view type $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is HeaderViewHolder -> holder.bind(getItem(position) as String)
            is TugasViewHolder -> holder.bind(getItem(position) as Tugas)
        }
    }

    fun submitTugasList(tugasList: List<Tugas>) {
        val items = mutableListOf<Any>()
        items.add("Belum Selesai")
        items.addAll(tugasList.filter { !it.isCompleted })
        items.add("Selesai")
        items.addAll(tugasList.filter { it.isCompleted })
        submitList(items)
    }

    inner class HeaderViewHolder(private val binding: ItemHeaderBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(header: String) {
            binding.headerTitle.text = header
        }
    }

    inner class TugasViewHolder(private val binding: ItemTugasBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(tugas: Tugas) {
            binding.namaTugas.text = tugas.namaTugas
            binding.checkboxTask.isChecked = tugas.isCompleted
            binding.checkboxTask.setOnCheckedChangeListener { _, isChecked: Boolean ->
                onStatusChange(tugas, isChecked)
            }
            binding.root.setOnClickListener {
                onItemClick(tugas)
            }
            binding.btnDeleteTgs.setOnClickListener {
                onDeleteClick(tugas)
            }
        }
    }

    class TugasDiffCallback : DiffUtil.ItemCallback<Any>() {
        override fun areItemsTheSame(oldItem: Any, newItem: Any): Boolean {
            return when {
                oldItem is Tugas && newItem is Tugas -> oldItem.id == newItem.id
                oldItem is String && newItem is String -> oldItem == newItem
                else -> false
            }
        }

        override fun areContentsTheSame(oldItem: Any, newItem: Any): Boolean {
            return when {
                oldItem is Tugas && newItem is Tugas -> oldItem == newItem
                oldItem is String && newItem is String -> oldItem == newItem
                else -> false
            }
        }
    }

}
