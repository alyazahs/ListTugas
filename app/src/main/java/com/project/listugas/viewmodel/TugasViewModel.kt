package com.project.listugas.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.project.listugas.entity.Tugas
import kotlinx.coroutines.launch

class TugasViewModel(application: Application) : AndroidViewModel(application) {

    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    private val tugasRef: DatabaseReference = database.getReference("tugas")

    private val _tugasList = MutableLiveData<List<Tugas>>()
    val tugasList: LiveData<List<Tugas>> get() = _tugasList

    // Mendapatkan tugas berdasarkan matkulId dan memantau perubahan data secara realtime
    fun getTugasByMatkulId(matkulId: Int) {
        tugasRef.orderByChild("matkulId").equalTo(matkulId.toDouble())
            .addValueEventListener(object : com.google.firebase.database.ValueEventListener {
                override fun onDataChange(dataSnapshot: com.google.firebase.database.DataSnapshot) {
                    val list = mutableListOf<Tugas>()
                    dataSnapshot.children.forEach { snapshot ->
                        val tugas = snapshot.getValue(Tugas::class.java)
                        tugas?.let {
                            it.id = snapshot.key?.hashCode() ?: 0 // Set ID manually
                            list.add(it)
                        }
                    }
                    _tugasList.postValue(list) // Update LiveData dengan list terbaru
                }

                override fun onCancelled(error: com.google.firebase.database.DatabaseError) {
                    // Handle error jika diperlukan
                }
            })
    }

    // Insert tugas baru ke Firebase
    fun insert(tugas: Tugas) = viewModelScope.launch {
        val newTugasId = tugasRef.push().key // Firebase auto-generate key (String)
        if (newTugasId != null) {
            // Convert Firebase ID to Int
            val intId = newTugasId.hashCode() // Hash menjadi ID int
            tugas.id = intId // Assign the generated ID as integer
            tugasRef.child(newTugasId).setValue(tugas)
        }
    }

    fun delete(tugas: Tugas) {
        viewModelScope.launch {
            // Misalnya Anda menggunakan Firebase untuk menghapus data tugas
            tugasRef.child(tugas.id.toString()).removeValue()

            // Setelah menghapus, refresh data untuk mendapatkan data terbaru
            getTugasByMatkulId(tugas.matkulId)
        }
    }


    // Update tugas di Firebase
    fun update(tugas: Tugas) = viewModelScope.launch {
        tugasRef.child(tugas.id.toString()).setValue(tugas)
    }

    // Update status isCompleted tugas
    fun updateStatus(tugasId: Int, isCompleted: Boolean) = viewModelScope.launch {
        tugasRef.child(tugasId.toString()).child("isCompleted").setValue(isCompleted)
    }
}
