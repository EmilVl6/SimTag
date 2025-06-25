package org.simtag.androidapp.ui.theme

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.simtag.androidapp.data.SimTag
import org.simtag.androidapp.data.SimTagRepository

class SimTagViewModel(private val repository: SimTagRepository) : ViewModel() {

    // Using StateFlow to expose the list of tags to the UI
    val allSimTags: StateFlow<List<SimTag>> = repository.allSimTags
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000), // Keep active for 5s after last subscriber
            initialValue = emptyList() // Initial value while loading
        )

    fun insert(tag: SimTag) = viewModelScope.launch {
        repository.insert(tag)
    }

    fun update(tag: SimTag) = viewModelScope.launch {
        repository.update(tag)
    }

    fun delete(tag: SimTag) = viewModelScope.launch {
        repository.delete(tag)
    }

    // You might add functions here to get specific tags by ID if needed for an edit screen
    // fun getTagById(id: Int): StateFlow<SimTag?> = repository.getSimTagById(id)
    //    .stateIn(...)
}

// Factory to provide the SimTagRepository to the ViewModel
class SimTagViewModelFactory(private val repository: SimTagRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SimTagViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SimTagViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}