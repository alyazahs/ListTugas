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
import com.project.listugas.entity.Note
import kotlinx.coroutines.launch

class NoteViewModel(application: Application) : AndroidViewModel(application) {
    private val firebaseDatabase = FirebaseDatabase.getInstance().reference.child("notes")

    // Fungsi untuk mengambil catatan dari Firebase berdasarkan matkulId
    fun fetchNotesFromFirebase(matkulId: Int): LiveData<List<Note>> {
        val notesLiveData = MutableLiveData<List<Note>>()
        firebaseDatabase.orderByChild("matkulId").equalTo(matkulId.toDouble())
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val notesList = mutableListOf<Note>()
                    for (noteSnapshot in snapshot.children) {
                        val note = noteSnapshot.getValue(Note::class.java)
                        note?.let { notesList.add(it) }
                    }
                    notesLiveData.value = notesList
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("NoteViewModel", "Error fetching notes: ${error.message}")
                }
            })
        return notesLiveData
    }

    // Fungsi untuk menambahkan catatan ke Firebase
    fun insertNoteToFirebase(note: Note) {
        // Cek apakah ID sudah ada
        if (note.id == 0) {
            // Jika ID masih 0, berarti ini catatan baru, maka kita bisa menggunakan push()
            val noteId = firebaseDatabase.push().key ?: return
            val newNote = note.copy(id = noteId.hashCode()) // Menggunakan ID baru
            firebaseDatabase.child(noteId).setValue(newNote)
                .addOnSuccessListener {
                    Log.d("NoteViewModel", "Catatan ditambahkan ke Firebase: $newNote")
                }
                .addOnFailureListener {
                    Log.e("NoteViewModel", "Gagal menambahkan catatan ke Firebase")
                }
        } else {
            // Jika ID sudah ada, langsung update menggunakan ID yang sudah ada
            firebaseDatabase.child(note.id.toString()).setValue(note)
                .addOnSuccessListener {
                    Log.d("NoteViewModel", "Catatan diperbarui di Firebase: $note")
                }
                .addOnFailureListener {
                    Log.e("NoteViewModel", "Gagal memperbarui catatan di Firebase")
                }
        }
    }


    // Fungsi untuk memperbarui catatan di Firebase
    fun updateNoteInFirebase(note: Note) {
        val noteId = note.id.toString() // Pastikan menggunakan ID yang tepat
        firebaseDatabase.child(noteId).setValue(note) // Perbarui data menggunakan ID yang benar
            .addOnSuccessListener {
                Log.d("NoteViewModel", "Catatan diperbarui di Firebase: $note")
            }
            .addOnFailureListener {
                Log.e("NoteViewModel", "Gagal memperbarui catatan di Firebase")
            }
    }

    // Fungsi untuk menghapus catatan di Firebase
    fun deleteNoteFromFirebase(noteId: Int) {
        firebaseDatabase.child(noteId.toString()).removeValue()
            .addOnSuccessListener {
                Log.d("NoteViewModel", "Catatan dihapus dari Firebase: $noteId")
            }
            .addOnFailureListener {
                Log.e("NoteViewModel", "Gagal menghapus catatan dari Firebase")
            }
    }
}
