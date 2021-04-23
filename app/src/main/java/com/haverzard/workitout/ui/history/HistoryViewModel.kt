package com.haverzard.workitout.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.haverzard.workitout.repository.WorkOutRepository


class HistoryViewModel(private val repository: WorkOutRepository) : ViewModel() {

}

class HistoryViewModelFactory(private val repository: WorkOutRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HistoryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HistoryViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}