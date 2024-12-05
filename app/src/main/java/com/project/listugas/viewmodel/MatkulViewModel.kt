package com.project.listugas.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.project.listugas.database.ListDatabase
import com.project.listugas.entity.Matkul
import kotlinx.coroutines.launch

class MatkulViewModel(application: Application) : AndroidViewModel(application) {
    private val matkulDao = ListDatabase.getDatabase(application).matkulDao()
    private val _allMatkuls = MutableLiveData<List<Matkul>>()
    val allMatkuls: LiveData<List<Matkul>> get() = _allMatkuls
    private val database = FirebaseDatabase.getInstance().getReference("matkul")

    init {
        fetchFromFirebase()
    }

    fun getMatkulById(matkulId: Int): LiveData<Matkul> {
        return matkulDao.getMatkulById(matkulId)
    }

    fun insert(matkul: Matkul) = viewModelScope.launch {
        matkulDao.insert(matkul)
        insertToFirebase(matkul)
    }

    fun update(matkul: Matkul) = viewModelScope.launch {
        matkulDao.update(matkul)
        updateToFirebase(matkul)
    }

    fun delete(matkul: Matkul) = viewModelScope.launch {
        matkulDao.delete(matkul)
        deleteFromFirebase(matkul)
    }

    private fun insertToFirebase(matkul: Matkul) {
        val newRef = database.push()
        val newMatkul = matkul.copy(id = newRef.key?.hashCode() ?: 0)
        newRef.setValue(newMatkul)
    }

    private fun updateToFirebase(matkul: Matkul) {
        database.child(matkul.id.toString()).setValue(matkul)
    }

    private fun deleteFromFirebase(matkul: Matkul) {
        database.child(matkul.id.toString()).removeValue()
    }

    private fun fetchFromFirebase() {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val matkuls = mutableListOf<Matkul>()
                for (data in snapshot.children) {
                    val matkul = data.getValue(Matkul::class.java)
                    if (matkul != null) {
                        matkuls.add(matkul)
                        Log.d("Firebase", "Data diterima: $matkul")
                    }
                }
                _allMatkuls.postValue(matkuls)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase", "Gagal mengambil data", error.toException())
            }
        })
    }
}
