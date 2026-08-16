package com.farid.practice

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class NoteViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: NoteRepository
    val allNotes: StateFlow<List<Note>>

    private val _uiEvents = MutableSharedFlow<UiEvent>()
    val uiEvents: SharedFlow<UiEvent> = _uiEvents

    init {
        val dao = AppDatabase.getDatabase(application).noteDao()
        repository = NoteRepository(dao)
        allNotes = repository.allNotes.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    }

    fun insertNote(text: String) {
        if (text.isBlank()) {
                viewModelScope.launch {
                _uiEvents.emit(UiEvent.ShowError(getApplication<Application>().getString(R.string.error_empty_note)))
            }
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.insert(Note(text))
                _uiEvents.emit(UiEvent.NoteSaved)
            } catch (e: Exception) {
                _uiEvents.emit(UiEvent.ShowError(getApplication<Application>().getString(R.string.error_save_failed)))
            }
        }
    }

    fun deleteNote(note: Note) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.delete(note)
                _uiEvents.emit(UiEvent.NoteDeleted)
            } catch (e: Exception) {
                _uiEvents.emit(UiEvent.ShowError(getApplication<Application>().getString(R.string.error_delete_failed)))
            }
        }
    }

    sealed class UiEvent {
        object NoteSaved : UiEvent()
        object NoteDeleted : UiEvent()
        data class ShowError(val message: String) : UiEvent()
    }
}
