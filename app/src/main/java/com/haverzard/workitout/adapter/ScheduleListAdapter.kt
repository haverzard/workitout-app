package com.haverzard.workitout.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.haverzard.workitout.R
import com.haverzard.workitout.entities.ExerciseType
import com.haverzard.workitout.entities.SingleExerciseSchedule
import java.sql.Date

class ScheduleListAdapter(scheduleSelectedListener: ScheduleSelectedListener) : ListAdapter<SingleExerciseSchedule, ScheduleListAdapter.ScheduleViewHolder>(SchedulesComparator()) {

    private val scheduleSelectedListener: ScheduleSelectedListener = scheduleSelectedListener

    interface ScheduleSelectedListener {
        fun onScheduleSelected(schedule: SingleExerciseSchedule)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScheduleViewHolder {
        return ScheduleViewHolder.create(parent)
    }

    override fun onBindViewHolder(holder: ScheduleViewHolder, position: Int) {
        val current = getItem(position)
        val date = Date(current.date.year-1900, current.date.month, current.date.date).toLocaleString()
        val time = "%02d:%02d - %02d:%02d".format(
            current.start_time.hours,
            current.start_time.minutes,
            current.end_time.hours,
            current.end_time.minutes,
        )
        val target = "Target: %.2f %s".format(
            current.target,
            if (current.exercise_type == ExerciseType.Cycling) "km" else "steps",
        )
        holder.itemView.findViewById<ImageButton>(R.id.schedule_delete).setOnClickListener {
            scheduleSelectedListener.onScheduleSelected(current)
        }
        holder.bind(date.substring(0, date.length-9), time, target)
    }

    class ScheduleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val dateItemView: TextView = itemView.findViewById(R.id.schedule_date)
        private val timeItemView: TextView = itemView.findViewById(R.id.schedule_time)
        private val targetItemView: TextView = itemView.findViewById(R.id.schedule_target)

        fun bind(date: String?, time: String?, target: String?) {
            dateItemView.text = date
            timeItemView.text = time
            targetItemView.text = target
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