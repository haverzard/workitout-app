package com.haverzard.workitout.ui.tracker

import androidx.lifecycle.*
import com.haverzard.workitout.entities.RoutineExerciseSchedule
import com.haverzard.workitout.entities.SingleExerciseSchedule
import com.haverzard.workitout.repository.WorkOutRepository
import kotlinx.coroutines.launch


class TrackerViewModel(private val repository: WorkOutRepository) : ViewModel() {

}

class TrackerViewModelFactory(private val repository: WorkOutRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TrackerViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TrackerViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}