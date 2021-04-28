package com.haverzard.workitout.repository

import androidx.annotation.WorkerThread
import com.haverzard.workitout.dao.HistoryDao
import com.haverzard.workitout.dao.RoutineExerciseScheduleDao
import com.haverzard.workitout.dao.SingleExerciseScheduleDao
import com.haverzard.workitout.entities.History
import com.haverzard.workitout.entities.RoutineExerciseSchedule
import com.haverzard.workitout.entities.SingleExerciseSchedule
import kotlinx.coroutines.flow.Flow

class WorkOutRepository(
    private val singleExerciseScheduleDao: SingleExerciseScheduleDao,
    private val routineExerciseScheduleDao: RoutineExerciseScheduleDao,
    private val historyDao: HistoryDao,
) {

    // live data
    val schedules: Flow<List<SingleExerciseSchedule>> = singleExerciseScheduleDao.getCurrentSchedules()
    val routines: Flow<List<RoutineExerciseSchedule>> = routineExerciseScheduleDao.getSchedules()
    val histories: Flow<List<History>> = historyDao.getHistories()
    
    // run everything on worker thread
    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun deleteSingleSchedule(schedule: SingleExerciseSchedule) {
        singleExerciseScheduleDao.delete(schedule)
    }
    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun deleteRoutineSchedule(schedule: RoutineExerciseSchedule) {
        routineExerciseScheduleDao.delete(schedule)
    }
    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun insertHistory(history: History) {
        historyDao.insert(history)
    }

    fun insertRoutineSchedule(schedule: RoutineExerciseSchedule) = routineExerciseScheduleDao.insert(schedule)
    fun insertSingleSchedule(schedule: SingleExerciseSchedule) = singleExerciseScheduleDao.insert(schedule)

    fun getSingleSchedule(id: Int): SingleExerciseSchedule {
        return singleExerciseScheduleDao.getSchedule(id)
    }

    fun getRoutineSchedule(id: Int): RoutineExerciseSchedule {
        return routineExerciseScheduleDao.getSchedule(id)
    }
}