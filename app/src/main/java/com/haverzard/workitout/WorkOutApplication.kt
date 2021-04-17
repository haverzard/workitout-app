package com.haverzard.workitout

import android.app.Application
import com.haverzard.workitout.database.WorkOutDatabase
import com.haverzard.workitout.repository.WorkOutRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

class WorkOutApplication : Application() {

    val applicationScope = CoroutineScope(SupervisorJob())
    val database by lazy { WorkOutDatabase.getDatabase(this, applicationScope) }
    val repository by lazy { WorkOutRepository(database.singleExerciseScheduleDao()) }

}