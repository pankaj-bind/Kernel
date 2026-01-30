package com.example.kernel.ui.cp

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kernel.data.model.Contest
import com.example.kernel.data.repository.ContestRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CPViewModel @Inject constructor(
    private val repository: ContestRepository
) : ViewModel() {

    private val _uiState = MutableLiveData<CPUiState>(CPUiState.Loading)
    val uiState: LiveData<CPUiState> = _uiState

    init {
        loadContests()
    }

    fun loadContests() {
        viewModelScope.launch {
            _uiState.value = CPUiState.Loading

            repository.getAllContests()
                .catch { e ->
                    _uiState.value = CPUiState.Error(e.message ?: "Unknown error occurred")
                }
                .collect { result ->
                    result.fold(
                        onSuccess = { contests ->
                            if (contests.isEmpty()) {
                                _uiState.value = CPUiState.Empty
                            } else {
                                _uiState.value = CPUiState.Success(contests)
                            }
                        },
                        onFailure = { e ->
                            _uiState.value = CPUiState.Error(e.message ?: "Failed to load contests")
                        }
                    )
                }
        }
    }

    fun refresh() {
        loadContests()
    }
}

sealed class CPUiState {
    data object Loading : CPUiState()
    data object Empty : CPUiState()
    data class Success(val contests: List<Contest>) : CPUiState()
    data class Error(val message: String) : CPUiState()
}
