package com.farid.practice

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var viewModel: NoteViewModel
    private lateinit var adapter: NotesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        viewModel = ViewModelProvider(this)[NoteViewModel::class.java]

        val etNote = findViewById<EditText>(R.id.etNote)
        val btnAdd = findViewById<Button>(R.id.btnAdd)
        val rvNotes = findViewById<RecyclerView>(R.id.rvNotes)

        adapter = NotesAdapter { noteToDelete ->
            viewModel.deleteNote(noteToDelete)
        }
        rvNotes.adapter = adapter

        btnAdd.setOnClickListener {
            viewModel.insertNote(etNote.text.toString())
            etNote.text.clear()
        }

        lifecycleScope.launch {
            viewModel.allNotes.collect { notes ->
                adapter.submitList(notes)
            }
        }

        lifecycleScope.launch {
            viewModel.uiEvents.collect { event ->
                when (event) {
                    is NoteViewModel.UiEvent.NoteSaved -> { }
                    is NoteViewModel.UiEvent.NoteDeleted ->
                        Toast.makeText(this@MainActivity, getString(R.string.note_deleted), Toast.LENGTH_SHORT).show()
                    is NoteViewModel.UiEvent.ShowError ->
                        Toast.makeText(this@MainActivity, event.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
