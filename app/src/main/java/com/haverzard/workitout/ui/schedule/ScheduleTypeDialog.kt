package com.haverzard.workitout.ui.schedule

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.DialogFragment
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import com.haverzard.workitout.R

class ScheduleTypeDialog : DialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return inflater.inflate(R.layout.fragment_dialog_schedule, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        setupClickListeners(view)
    }

    private fun setupClickListeners(view: View) {
        view.findViewById<Button>(R.id.btn_routine_schedule).setOnClickListener {
            val action = ScheduleFragmentDirections.actionAddSchedule("routine")
            findNavController().navigate(action)
            dismiss()
        }
        view.findViewById<Button>(R.id.btn_single_schedule).setOnClickListener {
            val action = ScheduleFragmentDirections.actionAddSchedule("single")
            findNavController().navigate(action)
            dismiss()
        }
    }

}