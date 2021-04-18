package com.haverzard.workitout.ui.schedule

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.observe
import androidx.navigation.Navigation
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.haverzard.workitout.R
import com.haverzard.workitout.WorkOutApplication
import com.haverzard.workitout.adapter.ScheduleListAdapter
import com.haverzard.workitout.entities.SingleExerciseSchedule
import com.haverzard.workitout.viewmodel.ScheduleViewModel
import com.haverzard.workitout.viewmodel.ScheduleViewModelFactory

class ScheduleFragment : Fragment() {

    private lateinit var scheduleViewModel: ScheduleViewModel

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        scheduleViewModel = ViewModelProviders.of(
        this, ScheduleViewModelFactory((activity?.application as WorkOutApplication).repository)
        ).get(ScheduleViewModel::class.java)

        val root = inflater.inflate(R.layout.fragment_schedule, container, false)
        root.findViewById<FloatingActionButton>(R.id.fab)?.setOnClickListener(
            Navigation.createNavigateOnClickListener(R.id.action_add_schedule, null)
        )

        val recyclerView = root.findViewById<RecyclerView>(R.id.recyclerview)
        val adapter = ScheduleListAdapter(object:ScheduleListAdapter.ScheduleSelectedListener {
            override fun onScheduleSelected(schedule: SingleExerciseSchedule) {
                scheduleViewModel.delete(schedule)
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

//        view.findViewById<View>(R.id.button_home).setOnClickListener {
//            val action = HomeFragmentDirections
//                    .actionHomeFragmentToHomeSecondFragment("From ScheduleFragment")
//            NavHostFragment.findNavController(this@HomeFragment)
//                    .navigate(action)
//        }
    }
}
