package com.haverzard.workitout.viewmodel

import androidx.lifecycle.*
import com.haverzard.workitout.adapter.ScheduleListAdapter
import com.haverzard.workitout.entities.RoutineExerciseSchedule
import com.haverzard.workitout.entities.SingleExerciseSchedule
import com.haverzard.workitout.repository.WorkOutRepository
import kotlinx.coroutines.launch


class ScheduleViewModel(private val repository: WorkOutRepository) : ViewModel() {

    var schedules = MediatorLiveData<List<ScheduleListAdapter.Schedule>>()
    private var singleSchedules = MutableList<ScheduleListAdapter.Schedule>(0) {
        _ -> ScheduleListAdapter.Schedule(null, null)
    }
    private var routineSchedules = MutableList<ScheduleListAdapter.Schedule>(0) {
        _ -> ScheduleListAdapter.Schedule(null, null)
    }

    init {
        schedules.addSource(repository.schedules.asLiveData()) { value ->
            System.out.println(value)
            run {
                singleSchedules.clear()
                value.forEach {
                    singleSchedules.add(ScheduleListAdapter.Schedule(it, null))
                }
                schedules.setValue(routineSchedules + singleSchedules)
            }
        }
        schedules.addSource(repository.routines.asLiveData()) { value ->
            System.out.println(value)
            run {
                routineSchedules.clear()
                value.forEach {
                    routineSchedules.add(ScheduleListAdapter.Schedule(null, it))
                }
                schedules.setValue(routineSchedules + singleSchedules)
            }
        }
    }

    /**
     * Launching a new coroutine to insert the data in a non-blocking way
     */
    fun insertSingleSchedule(schedule: SingleExerciseSchedule) = viewModelScope.launch {
        repository.insertSingleSchedule(schedule)
    }
    fun insertRoutineSchedule(schedule: RoutineExerciseSchedule) = viewModelScope.launch {
        repository.insertRoutineSchedule(schedule)
    }
    fun deleteSingleSchedule(schedule: SingleExerciseSchedule) = viewModelScope.launch {
        repository.deleteSingleSchedule(schedule)
    }
    fun deleteRoutineSchedule(schedule: RoutineExerciseSchedule) = viewModelScope.launch {
        repository.deleteRoutineSchedule(schedule)
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