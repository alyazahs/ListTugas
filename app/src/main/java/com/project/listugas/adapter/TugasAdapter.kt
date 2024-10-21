package com.project.listugas.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.project.listugas.R
import com.project.listugas.entity.Tugas

class TugasAdapter(
    private val onDeleteClick: (Tugas) -> Unit,
    private val onStatusChange: (Tugas, Boolean) -> Unit
) : RecyclerView.Adapter<TugasAdapter.TugasViewHolder>() {

    private var tugasList = listOf<Tugas>()

    inner class TugasViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nama: TextView = itemView.findViewById(R.id.namaTugas)
        val checkBox: CheckBox = itemView.findViewById(R.id.checkbox_task)
        val btnDelete: Button = itemView.findViewById(R.id.btn_deleteTgs)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TugasViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_tugas, parent, false)
        return TugasViewHolder(view)
    }

    override fun onBindViewHolder(holder: TugasViewHolder, position: Int) {
        val tugas = tugasList[position]
        holder.nama.text = tugas.namaTugas
        holder.checkBox.isChecked = tugas.isCompleted

        holder.checkBox.setOnCheckedChangeListener { _, isChecked ->
            onStatusChange(tugas, isChecked)
        }

        holder.btnDelete.setOnClickListener {
            onDeleteClick(tugas)
        }
    }

    override fun getItemCount(): Int = tugasList.size

    fun setTugas(tugas: List<Tugas>) {
        this.tugasList = tugas
        notifyDataSetChanged()
    }
}
