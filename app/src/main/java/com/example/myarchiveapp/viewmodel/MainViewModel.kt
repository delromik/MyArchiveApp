package com.example.myarchiveapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myarchiveapp.model.AppModel
import com.example.myarchiveapp.repository.AppRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MainUiState(
    val apps: List<AppModel> = emptyList(),
    val selectedPackages: Set<String> = emptySet(),
    val isLoading: Boolean = false
)

class MainViewModel(private val repository: AppRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    init {
        loadApps()
    }

    fun loadApps() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val apps = repository.getInstalledUserApps()
                _uiState.update { 
                    it.copy(
                        apps = apps,
                        isLoading = false
                    ) 
                }
            } catch (e: Exception) {
                // Handle error
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun toggleSelection(packageName: String) {
        _uiState.update { currentState ->
            val newSelection = if (currentState.selectedPackages.contains(packageName)) {
                currentState.selectedPackages - packageName
            } else {
                currentState.selectedPackages + packageName
            }
            currentState.copy(selectedPackages = newSelection)
        }
    }
    
    fun clearSelection() {
        _uiState.update { it.copy(selectedPackages = emptySet()) }
    }
}
