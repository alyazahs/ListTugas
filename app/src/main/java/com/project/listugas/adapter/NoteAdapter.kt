package com.project.listugas.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.project.listugas.R
import com.project.listugas.entity.Note
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class NoteAdapter(
    private val onDeleteClick: (Note) -> Unit,
    private val onNoteClick: (Note) -> Unit
) : ListAdapter<Note, NoteAdapter.NoteViewHolder>(NoteDiffCallback()) {

    inner class NoteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val judul: TextView = itemView.findViewById(R.id.judul)
        val deskripsi: TextView = itemView.findViewById(R.id.catatan)
        val tanggal: TextView = itemView.findViewById(R.id.tanggal)
        val deleteButton: Button = itemView.findViewById(R.id.btn_deleteNt)

        init {
            deleteButton.setOnClickListener {
                val note = getItem(adapterPosition)
                onDeleteClick(note)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_note, parent, false)
        return NoteViewHolder(view)
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        val note = getItem(position)
        holder.judul.text = note.judul
        holder.deskripsi.text = note.deskripsi

        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        val date = LocalDate.parse(note.tanggal, formatter)
        holder.tanggal.text = date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))

        holder.itemView.setOnClickListener {
            onNoteClick(note)
        }
    }

    class NoteDiffCallback : DiffUtil.ItemCallback<Note>() {
        override fun areItemsTheSame(oldItem: Note, newItem: Note): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Note, newItem: Note): Boolean {
            return oldItem == newItem
        }
    }
}