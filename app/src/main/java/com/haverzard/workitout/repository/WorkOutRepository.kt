package com.haverzard.workitout.repository

import androidx.annotation.WorkerThread
import com.haverzard.workitout.dao.SingleExerciseScheduleDao
import com.haverzard.workitout.entities.SingleExerciseSchedule
import kotlinx.coroutines.flow.Flow

class WorkOutRepository(private val singleExerciseScheduleDao: SingleExerciseScheduleDao) {

    // live data
    val schedules: Flow<List<SingleExerciseSchedule>> = singleExerciseScheduleDao.getSchedules()

    // run everything on worker thread
    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun insertSingleSchedule(schedule: SingleExerciseSchedule) {
        singleExerciseScheduleDao.insert(schedule)
    }
}