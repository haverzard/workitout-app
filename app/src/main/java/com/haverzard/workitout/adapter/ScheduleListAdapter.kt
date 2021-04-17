package com.haverzard.workitout.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.haverzard.workitout.R
import com.haverzard.workitout.entities.SingleExerciseSchedule

class ScheduleListAdapter : ListAdapter<SingleExerciseSchedule, ScheduleListAdapter.ScheduleViewHolder>(SchedulesComparator()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScheduleViewHolder {
        return ScheduleViewHolder.create(parent)
    }

    override fun onBindViewHolder(holder: ScheduleViewHolder, position: Int) {
        val current = getItem(position)
        holder.bind(current.date.toString())
    }

    class ScheduleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val wordItemView: TextView = itemView.findViewById(R.id.textView)

        fun bind(text: String?) {
            wordItemView.text = text
        }

        companion object {
            fun create(parent: ViewGroup): ScheduleViewHolder {
                val view: View = LayoutInflater.from(parent.context)
                        .inflate(R.layout.schedule_item, parent, false)
                return ScheduleViewHolder(view)
            }
        }
    }

    class SchedulesComparator : DiffUtil.ItemCallback<SingleExerciseSchedule>() {
        override fun areItemsTheSame(
            oldItem: SingleExerciseSchedule,
            newItem: SingleExerciseSchedule
        ): Boolean {
            return oldItem === newItem
        }

        override fun areContentsTheSame(
            oldItem: SingleExerciseSchedule,
            newItem: SingleExerciseSchedule
        ): Boolean {
            return (
                oldItem.date == newItem.date
                && oldItem.start_time == newItem.start_time
                && oldItem.end_time == newItem.end_time
            )
        }
    }
}