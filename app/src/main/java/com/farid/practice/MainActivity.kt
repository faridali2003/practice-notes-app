package com.farid.practice

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    private lateinit var database: AppDatabase
    private lateinit var adapter: NotesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        database = AppDatabase.getDatabase(this)

        val etNote = findViewById<EditText>(R.id.etNote)
        val btnAdd = findViewById<Button>(R.id.btnAdd)
        val rvNotes = findViewById<RecyclerView>(R.id.rvNotes)

        adapter = NotesAdapter(emptyList()) { noteToDelete ->
            lifecycleScope.launch(Dispatchers.IO) {
                database.noteDao().deleteNote(noteToDelete)
                loadNotes()
            }
            Toast.makeText(this@MainActivity, "Note Deleted", Toast.LENGTH_SHORT).show()
        }
        rvNotes.adapter = adapter

        btnAdd.setOnClickListener {
            val text = etNote.text.toString()
            if (text.isNotBlank()) {
                val note = Note(text)
                lifecycleScope.launch(Dispatchers.IO) {
                    database.noteDao().insertNote(note)
                    loadNotes()
                }
                etNote.text.clear()
            } else {
                Toast.makeText(this, "Please enter a note", Toast.LENGTH_SHORT).show()
            }
        }

        loadNotes()
    }

    private fun loadNotes() {
        lifecycleScope.launch(Dispatchers.IO) {
            val notesList = database.noteDao().getAllNotes()
            withContext(Dispatchers.Main) {
                adapter.updateNotes(notesList)
            }
        }
    }
}
