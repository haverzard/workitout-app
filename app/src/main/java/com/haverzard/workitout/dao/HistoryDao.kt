package com.haverzard.workitout.dao

import androidx.room.*
import com.haverzard.workitout.entities.History
import com.haverzard.workitout.entities.RoutineExerciseSchedule
import kotlinx.coroutines.flow.Flow

@Dao
interface HistoryDao  {

    @Query("SELECT * FROM histories")
    fun getHistories(): Flow<List<History>>

    @Query("SELECT * FROM histories WHERE id = :id")
    fun getHistory(id: Int): History

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(history: History)

    @Delete
    suspend fun delete(history: History)
}