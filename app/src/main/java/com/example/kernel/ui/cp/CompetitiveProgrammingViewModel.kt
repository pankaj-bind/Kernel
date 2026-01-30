package com.example.kernel.ui.cp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kernel.data.model.Contest
import com.example.kernel.data.repository.ContestRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the Competitive Programming screen.
 * Manages UI state and business logic for contest data.
 */
@HiltViewModel
class CompetitiveProgrammingViewModel @Inject constructor(
    private val repository: ContestRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ContestUiState>(ContestUiState.Loading)
    val uiState: StateFlow<ContestUiState> = _uiState.asStateFlow()

    private val _selectedPlatforms = MutableStateFlow<Set<String>>(
        setOf("CodeForces", "CodeChef", "LeetCode", "AtCoder")
    )
    val selectedPlatforms: StateFlow<Set<String>> = _selectedPlatforms.asStateFlow()

    init {
        loadContests()
    }

    /**
     * Load contests from the repository
     */
    fun loadContests() {
        viewModelScope.launch {
            _uiState.value = ContestUiState.Loading

            repository.getAllContests().collect { result ->
                _uiState.value = result.fold(
                    onSuccess = { contests ->
                        if (contests.isEmpty()) {
                            ContestUiState.Empty
                        } else {
                            ContestUiState.Success(contests)
                        }
                    },
                    onFailure = { exception ->
                        ContestUiState.Error(exception.message ?: "Unknown error occurred")
                    }
                )
            }
        }
    }

    /**
     * Refresh the contest list
     */
    fun refresh() {
        loadContests()
    }

    /**
     * Toggle platform filter selection
     */
    fun togglePlatform(platform: String) {
        val current = _selectedPlatforms.value.toMutableSet()
        if (current.contains(platform)) {
            current.remove(platform)
        } else {
            current.add(platform)
        }
        _selectedPlatforms.value = current
    }
}

/**
 * UI State for the CP screen
 */
sealed class ContestUiState {
    data object Loading : ContestUiState()
    data object Empty : ContestUiState()
    data class Success(val contests: List<Contest>) : ContestUiState()
    data class Error(val message: String) : ContestUiState()
}
