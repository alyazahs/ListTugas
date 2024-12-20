package com.project.listugas.adapter

import android.annotation.SuppressLint
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

    // Enum untuk menentukan jenis tampilan (header atau item)
    enum class ViewType {
        HEADER,
        ITEM
    }

    // Data class untuk mewakili item kategori (header)
    data class CategoryItem(val categoryName: String)

    // ViewHolder untuk kategori (header)
    inner class HeaderViewHolder(private val binding: ItemHeaderBinding) :
        RecyclerView.ViewHolder(binding.root) {
        // Mengatur teks kategori di header
        fun bind(categoryName: String) {
            binding.headerTitle.text = categoryName
        }
    }

    // ViewHolder untuk matkul
    inner class MatkulViewHolder(private val binding: ItemMatkulBinding) :
        RecyclerView.ViewHolder(binding.root) {

        // Mengatur data matkul dan mengatur event klik
        fun bind(matkul: Matkul) {
            binding.nama.text = matkul.namaMatkul // Mengatur nama matkul
            binding.deskripsi.text = matkul.deskripsi // Mengatur deskripsi matkul

            // Klik pada root item membuka NoteActivity dengan membawa ID matkul
            binding.root.setOnClickListener {
                val context = binding.root.context
                val intent = Intent(context, NoteActivity::class.java)
                intent.putExtra("MATKUL_ID", matkul.id) // Menyisipkan data ID matkul
                context.startActivity(intent)
            }

            // Tombol edit memanggil callback onEditClick
            binding.btnEditMk.setOnClickListener {
                onEditClick(matkul)
            }

            // Tombol delete memanggil callback onDeleteClick
            binding.btnDeleteMk.setOnClickListener {
                onDeleteClick(matkul)
            }
        }
    }

    // Menentukan jenis tampilan (header atau item) berdasarkan posisi
    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is CategoryItem -> ViewType.HEADER.ordinal // Jika item adalah kategori
            is Matkul -> ViewType.ITEM.ordinal // Jika item adalah matkul
            else -> throw IllegalArgumentException("Unknown item type") // Jika tipe tidak dikenal
        }
    }

    // Membuat ViewHolder berdasarkan jenis tampilan
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (ViewType.entries[viewType]) {
            ViewType.HEADER -> { // Membuat ViewHolder untuk header
                val binding = ItemHeaderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                HeaderViewHolder(binding)
            }
            ViewType.ITEM -> { // Membuat ViewHolder untuk item matkul
                val binding = ItemMatkulBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                MatkulViewHolder(binding)
            }
        }
    }

    // Menghubungkan data ke ViewHolder
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = getItem(position)) {
            is CategoryItem -> (holder as HeaderViewHolder).bind(item.categoryName) // Mengatur data kategori
            is Matkul -> (holder as MatkulViewHolder).bind(item) // Mengatur data matkul
        }
    }

    // DiffUtil untuk membandingkan data lama dan baru dalam daftar
    class MatkulDiffCallback : DiffUtil.ItemCallback<Any>() {
        override fun areItemsTheSame(oldItem: Any, newItem: Any): Boolean {
            return when {
                oldItem is Matkul && newItem is Matkul -> oldItem.id == newItem.id // Membandingkan ID matkul
                oldItem is CategoryItem && newItem is CategoryItem -> oldItem.categoryName == newItem.categoryName // Membandingkan nama kategori
                else -> false // Jika tipe tidak cocok
            }
        }

        @SuppressLint("DiffutilEquals")
        override fun areContentsTheSame(oldItem: Any, newItem: Any): Boolean {
            return oldItem == newItem // Membandingkan isi data
        }
    }
}
