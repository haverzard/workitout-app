package com.haverzard.workitout.ui.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CalendarView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.haverzard.workitout.R


class HistoryFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_history, container, false)

        val calendarView = root.findViewById<CalendarView>(R.id.calendar)
        calendarView.setOnDateChangeListener { _, year, month, day ->
            val action =
                HistoryFragmentDirections.actionHistory("%04d-%02d-%02d".format(year, month+1, day))
            findNavController().navigate(action)
        }

        return root
    }
}
