package com.haverzard.workitout.repository

import androidx.annotation.WorkerThread
import com.haverzard.workitout.dao.RoutineExerciseScheduleDao
import com.haverzard.workitout.dao.SingleExerciseScheduleDao
import com.haverzard.workitout.entities.RoutineExerciseSchedule
import com.haverzard.workitout.entities.SingleExerciseSchedule
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class WorkOutRepository(
    private val singleExerciseScheduleDao: SingleExerciseScheduleDao,
    private val routineExerciseScheduleDao: RoutineExerciseScheduleDao
) {

    // live data
    val schedules: Flow<List<SingleExerciseSchedule>> = singleExerciseScheduleDao.getNonRoutineSchedules()
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
}