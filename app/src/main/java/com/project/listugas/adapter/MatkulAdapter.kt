package com.project.listugas.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.project.listugas.NoteActivity
import com.project.listugas.databinding.ItemMatkulBinding
import com.project.listugas.databinding.ItemHeaderBinding
import com.project.listugas.entity.Matkul

class MatkulAdapter(
    private val onEditClick: (Matkul) -> Unit,
    private val onDeleteClick: (Matkul) -> Unit
) : ListAdapter<Any, RecyclerView.ViewHolder>(MatkulDiffCallback()) {

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

    inner class MatkulViewHolder(private val binding: ItemMatkulBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(matkul: Matkul) {
            binding.nama.text = matkul.namaMatkul
            binding.deskripsi.text = matkul.deskripsi

            binding.root.setOnClickListener {
                val context = binding.root.context
                val intent = Intent(context, NoteActivity::class.java)
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

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is CategoryItem -> ViewType.HEADER.ordinal
            is Matkul -> ViewType.ITEM.ordinal
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
                val binding = ItemMatkulBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                MatkulViewHolder(binding)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = getItem(position)) {
            is CategoryItem -> (holder as HeaderViewHolder).bind(item.categoryName)
            is Matkul -> (holder as MatkulViewHolder).bind(item)
        }
    }

    class MatkulDiffCallback : DiffUtil.ItemCallback<Any>() {
        override fun areItemsTheSame(oldItem: Any, newItem: Any): Boolean {
            return when {
                oldItem is Matkul && newItem is Matkul -> oldItem.id == newItem.id
                oldItem is CategoryItem && newItem is CategoryItem -> oldItem.categoryName == newItem.categoryName
                else -> false
            }
        }

        override fun areContentsTheSame(oldItem: Any, newItem: Any): Boolean {
            return oldItem == newItem
        }
    }
}
