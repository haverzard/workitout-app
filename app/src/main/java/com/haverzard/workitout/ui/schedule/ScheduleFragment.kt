package com.haverzard.workitout.ui.schedule

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.observe
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.haverzard.workitout.R
import com.haverzard.workitout.WorkOutApplication
import com.haverzard.workitout.entities.RoutineExerciseSchedule
import com.haverzard.workitout.entities.SingleExerciseSchedule
import com.haverzard.workitout.viewmodel.ScheduleViewModel
import com.haverzard.workitout.viewmodel.ScheduleViewModelFactory

class ScheduleFragment : Fragment() {

    private lateinit var scheduleViewModel: ScheduleViewModel
    private val scheduleTypeDialog = ScheduleTypeDialog()

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        scheduleViewModel = ViewModelProviders.of(
        this, ScheduleViewModelFactory((activity?.application as WorkOutApplication).repository)
        ).get(ScheduleViewModel::class.java)

        val root = inflater.inflate(R.layout.fragment_schedule, container, false)
        root.findViewById<FloatingActionButton>(R.id.fab)?.setOnClickListener {
            scheduleTypeDialog.show(fragmentManager, "timePicker")
        }

        val recyclerView = root.findViewById<RecyclerView>(R.id.recyclerview)
        val adapter = ScheduleListAdapter(object :
            ScheduleListAdapter.ScheduleSelectedListener {
            override fun onScheduleSelected(schedule: SingleExerciseSchedule) {
                scheduleViewModel.deleteSingleSchedule(schedule)
            }

            override fun onScheduleSelected(schedule: RoutineExerciseSchedule) {
                scheduleViewModel.deleteRoutineSchedule(schedule)
            }
        })
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(activity)

        scheduleViewModel.schedules.observe(owner = this) { schedules ->
            schedules.let { adapter.submitList(it) }
        }
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }
}
