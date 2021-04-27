package com.haverzard.workitout.ui.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.haverzard.workitout.R
import com.haverzard.workitout.WorkOutApplication
import com.haverzard.workitout.entities.History

class HistoryListFragment : Fragment() {

    private lateinit var historyViewModel: HistoryViewModel
    private val safeArgs: HistoryListFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        historyViewModel = ViewModelProviders.of(
            this, HistoryViewModelFactory((activity?.application as WorkOutApplication).repository)
        ).get(HistoryViewModel::class.java)

        val root = inflater.inflate(R.layout.fragment_history_list, container, false)

        val textView = root.findViewById<TextView>(R.id.no_history)
        val recyclerView = root.findViewById<RecyclerView>(R.id.recyclerview)
        val adapter = HistoryListAdapter(object :
            HistoryListAdapter.HistorySelectedListener {
            override fun onHistorySelected(history: History) {
                val action =
                    HistoryListFragmentDirections.actionHistoryList(history.id)
                findNavController().navigate(action)
            }
        })
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(activity)

        historyViewModel.getHistories(safeArgs.date)
        historyViewModel.histories.observe(this) { histories ->
            textView.visibility = if (histories.isEmpty()) View.VISIBLE else View.GONE
            histories.let { adapter.submitList(it) }
        }
        return root
    }
}
