package com.project.listugas.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.project.listugas.R
import com.project.listugas.date.DateUtils
import com.project.listugas.entity.Note

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

        val date = DateUtils.parseDate(note.tanggal)
        holder.tanggal.text = DateUtils.formatDisplayDate(date)

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