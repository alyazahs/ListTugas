package com.project.listugas.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.project.listugas.databinding.ItemNoteBinding
import com.project.listugas.databinding.ItemHeaderBinding
import com.project.listugas.date.DateUtils
import com.project.listugas.entity.Note

class NoteAdapter(
    private val onDeleteClick: (Note) -> Unit,
    private val onNoteClick: (Note) -> Unit
) : ListAdapter<Any, RecyclerView.ViewHolder>(NoteDiffCallback()) {

    enum class ViewType {
        HEADER,
        ITEM
    }

    data class CategoryItem(val categoryName: String)

    inner class HeaderViewHolder(private val binding: ItemHeaderBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(categoryName: String) {
            binding.headerTitle.text = categoryName
        }
    }

    inner class NoteViewHolder(private val binding: ItemNoteBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(note: Note) {
            binding.judul.text = note.judul
            binding.catatan.text = note.deskripsi
            binding.tanggal.text = DateUtils.formatDisplayDate(DateUtils.parseDate(note.tanggal))

            binding.btnDeleteNt.setOnClickListener {
                onDeleteClick(note)
            }

            binding.root.setOnClickListener {
                onNoteClick(note)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is CategoryItem -> ViewType.HEADER.ordinal
            is Note -> ViewType.ITEM.ordinal
            else -> throw IllegalArgumentException("Unknown item type")
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (ViewType.entries[viewType]) {
            ViewType.HEADER -> {
                val binding = ItemHeaderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                HeaderViewHolder(binding)
            }
            ViewType.ITEM -> {
                val binding = ItemNoteBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                NoteViewHolder(binding)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = getItem(position)) {
            is CategoryItem -> (holder as HeaderViewHolder).bind(item.categoryName)
            is Note -> (holder as NoteViewHolder).bind(item)
        }
    }

    class NoteDiffCallback : DiffUtil.ItemCallback<Any>() {
        override fun areItemsTheSame(oldItem: Any, newItem: Any): Boolean {
            return when {
                oldItem is Note && newItem is Note -> oldItem.id == newItem.id
                oldItem is CategoryItem && newItem is CategoryItem -> oldItem.categoryName == newItem.categoryName
                else -> false
            }
        }

        @SuppressLint("DiffutilEquals")
        override fun areContentsTheSame(oldItem: Any, newItem: Any): Boolean {
            return oldItem == newItem
        }
    }
}
