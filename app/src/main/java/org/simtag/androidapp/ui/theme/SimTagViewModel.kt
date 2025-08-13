package org.simtag.androidapp.ui.theme

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.simtag.androidapp.data.FirestoreSimTag
import org.simtag.androidapp.data.FirestoreTagRepository
import org.simtag.androidapp.data.SimTag

class SimTagViewModel(
    private val repository: FirestoreTagRepository = FirestoreTagRepository()
) : ViewModel() {
    private val _allSimTags = MutableStateFlow<List<FirestoreSimTag>>(emptyList())
    val allSimTags: StateFlow<List<FirestoreSimTag>> = _allSimTags

    init {
        fetchTags()
    }

    fun fetchTags() {
        viewModelScope.launch {
            _allSimTags.value = repository.getAllTags()
        }
    }

    fun insert(tag: FirestoreSimTag) {
        viewModelScope.launch {
            repository.addOrUpdateTag(tag)
            fetchTags()
        }
    }

    fun update(tag: FirestoreSimTag) {
        viewModelScope.launch {
            repository.addOrUpdateTag(tag)
            fetchTags()
        }
    }

    fun delete(tag: FirestoreSimTag) {
        viewModelScope.launch {
            repository.deleteTag(tag.id)
            fetchTags()
        }
    }

    // In your ViewModel, after local insert/update:
    fun insert(tag: SimTag) {
        viewModelScope.launch {
            localRepository.insert(tag)
            firestoreRepository.addOrUpdateTag(tag.toFirestore())
            fetchTags()
        }
    }

    fun update(tag: SimTag) {
        viewModelScope.launch {
            localRepository.update(tag)
            firestoreRepository.addOrUpdateTag(tag.toFirestore())
            fetchTags()
        }
    }
}