package com.example.kernel.ui.cp

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kernel.data.model.Contest
import com.example.kernel.data.repository.ContestRepository
import com.example.kernel.worker.ContestNotificationScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CPViewModel @Inject constructor(
    private val repository: ContestRepository,
    private val notificationScheduler: ContestNotificationScheduler
) : ViewModel() {

    private val _uiState = MutableLiveData<CPUiState>(CPUiState.Loading)
    val uiState: LiveData<CPUiState> = _uiState

    private val _userRating = MutableLiveData<Int?>()
    val userRating: LiveData<Int?> = _userRating

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
                                // Schedule notifications for upcoming contests
                                scheduleNotifications(contests)
                            }
                        },
                        onFailure = { e ->
                            _uiState.value = CPUiState.Error(e.message ?: "Failed to load contests")
                        }
                    )
                }
        }
    }

    /**
     * Schedule notifications for all upcoming contests
     */
    private fun scheduleNotifications(contests: List<Contest>) {
        notificationScheduler.scheduleNotifications(contests)
    }

    fun refresh() {
        loadContests()
    }

    /**
     * Filter contests by platform
     */
    fun filterByPlatform(platform: Contest.Platform?) {
        viewModelScope.launch {
            _uiState.value = CPUiState.Loading

            val flow = if (platform != null) {
                repository.getContestsByPlatform(platform)
            } else {
                repository.getAllContests()
            }

            flow.catch { e ->
                _uiState.value = CPUiState.Error(e.message ?: "Unknown error occurred")
            }.collect { result ->
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

    /**
     * Update Codeforces handle and refresh rating filter
     */
    fun updateCodeforcesHandle(handle: String) {
        viewModelScope.launch {
            val result = repository.updateCodeforcesHandle(handle)
            result.fold(
                onSuccess = { rating ->
                    _userRating.value = rating
                    loadContests() // Refresh contests with new rating filter
                },
                onFailure = { /* Handle error */ }
            )
        }
    }
}

sealed class CPUiState {
    data object Loading : CPUiState()
    data object Empty : CPUiState()
    data class Success(val contests: List<Contest>) : CPUiState()
    data class Error(val message: String) : CPUiState()
}
