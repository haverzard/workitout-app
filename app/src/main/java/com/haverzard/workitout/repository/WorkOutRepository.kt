package com.haverzard.workitout.repository

import android.icu.util.Calendar
import androidx.annotation.WorkerThread
import com.haverzard.workitout.dao.RoutineExerciseScheduleDao
import com.haverzard.workitout.dao.SingleExerciseScheduleDao
import com.haverzard.workitout.entities.Day
import com.haverzard.workitout.entities.RoutineExerciseSchedule
import com.haverzard.workitout.entities.SingleExerciseSchedule
import kotlinx.coroutines.flow.Flow

class WorkOutRepository(
    private val singleExerciseScheduleDao: SingleExerciseScheduleDao,
    private val routineExerciseScheduleDao: RoutineExerciseScheduleDao
) {

    // live data
    val schedules: Flow<List<SingleExerciseSchedule>> = singleExerciseScheduleDao.getCurrentSchedules()
    val routines: Flow<List<RoutineExerciseSchedule>> = routineExerciseScheduleDao.getSchedules()
    
    // run everything on worker thread
    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun insertSingleSchedule(schedule: SingleExerciseSchedule) {
        singleExerciseScheduleDao.insert(schedule)
    }
    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun deleteSingleSchedule(schedule: SingleExerciseSchedule) {
        singleExerciseScheduleDao.delete(schedule)
    }
    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun insertRoutineSchedule(schedule: RoutineExerciseSchedule) {
        routineExerciseScheduleDao.insert(schedule)

    }
    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun deleteRoutineSchedule(schedule: RoutineExerciseSchedule) {
        routineExerciseScheduleDao.delete(schedule)
    }

    fun getCurrentSingleSchedule(): SingleExerciseSchedule? {
        return singleExerciseScheduleDao.getCurrentSchedule()
    }

    fun getCurrentRoutineSchedule(): RoutineExerciseSchedule? {
        val calendar: Calendar = Calendar.getInstance()
        val day: Int = calendar.get(Calendar.DAY_OF_WEEK)
        return routineExerciseScheduleDao.getCurrentSchedule(Day.values()[day].name)
    }
}