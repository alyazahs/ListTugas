package com.project.listugas

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.project.ListTugas.databinding.ActivityListBinding

class ListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityListBinding
    private var matkulId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        matkulId = intent.getIntExtra("MATKUL_ID", -1)

        binding.iconNote.setOnClickListener {
            val intent = Intent(this, NoteActivity::class.java)
            intent.putExtra("MATKUL_ID", matkulId)
            startActivity(intent)
        }

        binding.iconTodo.setOnClickListener {
            val intent = Intent(this, TugasActivity::class.java)
            intent.putExtra("MATKUL_ID", matkulId)
            startActivity(intent)
        }
    }
}