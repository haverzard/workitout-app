package com.haverzard.workitout.viewmodel

import androidx.lifecycle.*
import com.haverzard.workitout.entities.SingleExerciseSchedule
import com.haverzard.workitout.repository.WorkOutRepository
import kotlinx.coroutines.launch
import java.util.*

class ScheduleViewModel(private val repository: WorkOutRepository) : ViewModel() {

    val schedules: LiveData<List<SingleExerciseSchedule>> = repository.schedules.asLiveData()

    /**
     * Launching a new coroutine to insert the data in a non-blocking way
     */
    fun insert(schedule: SingleExerciseSchedule) = viewModelScope.launch {
        repository.insertSingleSchedule(schedule)
    }
    fun delete(schedule: SingleExerciseSchedule) = viewModelScope.launch {
        repository.deleteSingleSchedule(schedule)
    }
}

class ScheduleViewModelFactory(private val repository: WorkOutRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ScheduleViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ScheduleViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}