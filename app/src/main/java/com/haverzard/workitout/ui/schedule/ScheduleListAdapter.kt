package com.haverzard.workitout.ui.schedule

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
import com.haverzard.workitout.entities.RoutineExerciseSchedule
import com.haverzard.workitout.entities.SingleExerciseSchedule
import java.sql.Date

class ScheduleListAdapter(scheduleSelectedListener: ScheduleSelectedListener) : ListAdapter<ScheduleListAdapter.Schedule, ScheduleListAdapter.ScheduleViewHolder>(
    SchedulesComparator()
) {

    private val scheduleSelectedListener: ScheduleSelectedListener = scheduleSelectedListener

    interface ScheduleSelectedListener {
        fun onScheduleSelected(schedule: SingleExerciseSchedule)
        fun onScheduleSelected(schedule: RoutineExerciseSchedule)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScheduleViewHolder {
        return ScheduleViewHolder.create(
            parent
        )
    }

    override fun onBindViewHolder(holder: ScheduleViewHolder, position: Int) {
        val singleExerciseSchedule = getItem(position).getSingleSchedule()
        val routineExerciseSchedule = getItem(position).getRoutineSchedule()
        if (singleExerciseSchedule != null) {
            val date = Date(
                singleExerciseSchedule.date.year - 1900,
                singleExerciseSchedule.date.month,
                singleExerciseSchedule.date.date
            ).toLocaleString()
            val time = "%02d:%02d - %02d:%02d".format(
                singleExerciseSchedule.start_time.hours,
                singleExerciseSchedule.start_time.minutes,
                singleExerciseSchedule.end_time.hours,
                singleExerciseSchedule.end_time.minutes,
            )
            val target = "Target: %.2f %s".format(
                singleExerciseSchedule.target,
                if (singleExerciseSchedule.exercise_type == ExerciseType.Cycling) "km" else "steps",
            )
            holder.itemView.findViewById<ImageButton>(R.id.schedule_delete).setOnClickListener {
                scheduleSelectedListener.onScheduleSelected(singleExerciseSchedule)
            }
            holder.bind(singleExerciseSchedule.exercise_type, date.substring(0, date.length - 9), time, target)
        } else if (routineExerciseSchedule != null) {
            val days = routineExerciseSchedule.days.joinToString(separator = "-")
            val time = "%02d:%02d - %02d:%02d".format(
                routineExerciseSchedule.start_time.hours,
                routineExerciseSchedule.start_time.minutes,
                routineExerciseSchedule.end_time.hours,
                routineExerciseSchedule.end_time.minutes,
            )
            val target = "Target: %.2f %s".format(
                routineExerciseSchedule.target,
                if (routineExerciseSchedule.exercise_type == ExerciseType.Cycling) "km" else "steps",
            )
            holder.itemView.findViewById<ImageButton>(R.id.schedule_delete).setOnClickListener {
                scheduleSelectedListener.onScheduleSelected(routineExerciseSchedule)
            }
            holder.bind(routineExerciseSchedule.exercise_type, days, time, target)
        }
    }

    class Schedule(
        private val singleExerciseSchedule: SingleExerciseSchedule?,
        private val routineExerciseSchedule: RoutineExerciseSchedule?,
    ) {
        fun getSingleSchedule(): SingleExerciseSchedule? {
            return singleExerciseSchedule
        }
        fun getRoutineSchedule(): RoutineExerciseSchedule? {
            return routineExerciseSchedule
        }
        fun equals(schedule: Schedule): Boolean {
            if (singleExerciseSchedule != null && schedule.singleExerciseSchedule != null) {
                return singleExerciseSchedule.id == schedule.singleExerciseSchedule.id
            } else if (routineExerciseSchedule != null && schedule.routineExerciseSchedule != null) {
                return routineExerciseSchedule.id == schedule.routineExerciseSchedule.id
            }
            return false
        }
    }

    class ScheduleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageButton: ImageButton = itemView.findViewById(R.id.schedule_exercise)
        private val dateItemView: TextView = itemView.findViewById(R.id.schedule_date)
        private val timeItemView: TextView = itemView.findViewById(R.id.schedule_time)
        private val targetItemView: TextView = itemView.findViewById(R.id.schedule_target)

        fun bind(type: ExerciseType?, date: String?, time: String?, target: String?) {
            if (type == ExerciseType.Cycling) {
                imageButton.setImageResource(R.drawable.ic_directions_bike_white_24dp)
            } else {
                imageButton.setImageResource(R.drawable.ic_directions_walk_white_24dp)
            }
            dateItemView.text = date
            timeItemView.text = time
            targetItemView.text = target
        }

        companion object {
            fun create(parent: ViewGroup): ScheduleViewHolder {
                val view: View = LayoutInflater.from(parent.context)
                        .inflate(R.layout.schedule_item, parent, false)
                return ScheduleViewHolder(
                    view
                )
            }
        }
    }

    class SchedulesComparator : DiffUtil.ItemCallback<Schedule>() {
        override fun areItemsTheSame(
            oldItem: Schedule,
            newItem: Schedule
        ): Boolean {
            return oldItem === newItem
        }

        override fun areContentsTheSame(
            oldItem: Schedule,
            newItem: Schedule
        ): Boolean {
            return oldItem.equals(newItem)
        }
    }
}