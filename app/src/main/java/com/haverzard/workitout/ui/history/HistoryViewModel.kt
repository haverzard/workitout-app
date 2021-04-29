package com.haverzard.workitout.ui.history

import androidx.lifecycle.*
import com.haverzard.workitout.entities.History
import com.haverzard.workitout.repository.WorkOutRepository


class HistoryViewModel(private val repository: WorkOutRepository) : ViewModel() {
    val histories = MediatorLiveData<List<History>>()
    val currentHistory = MediatorLiveData<History?>()

    fun getHistories(date: String) {
        histories.addSource(repository.getHistories(date).asLiveData()) {
            run {
                histories.setValue(it)
            }
        }
    }

    fun getHistory(id: Int) {
        currentHistory.addSource(repository.getHistory(id).asLiveData()) {
            run {
                currentHistory.setValue(it)
            }
        }
    }
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