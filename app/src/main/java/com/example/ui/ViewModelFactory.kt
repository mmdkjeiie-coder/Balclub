package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.data.BalClubRepository
import com.example.data.BalClubViewModel

class BalClubViewModelFactory(private val repository: BalClubRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BalClubViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return BalClubViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
