package com.haverzard.workitout

import android.app.Application
import com.haverzard.workitout.database.WorkOutDatabase
import com.haverzard.workitout.repository.WorkOutRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

class WorkOutApplication : Application() {

    val applicationScope = CoroutineScope(SupervisorJob())
    private val database by lazy { WorkOutDatabase.getDatabase(this) }
    val repository by lazy {
        WorkOutRepository(
            database.singleExerciseScheduleDao(),
            database.routineExerciseScheduleDao(),
            database.historyDao(),
        )
    }

}