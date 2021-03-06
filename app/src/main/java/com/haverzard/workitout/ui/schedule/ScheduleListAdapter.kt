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
import com.haverzard.workitout.util.CalendarPlus

class ScheduleListAdapter(private val scheduleSelectedListener: ScheduleSelectedListener) : ListAdapter<ScheduleListAdapter.Schedule, ScheduleListAdapter.ScheduleViewHolder>(
    SchedulesComparator()
) {

    interface ScheduleSelectedListener {
        fun onScheduleSelected(schedule: SingleExerciseSchedule)
        fun onScheduleSelected(schedule: RoutineExerciseSchedule)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScheduleViewHolder {
        return ScheduleViewHolder.create(parent)
    }

    override fun onBindViewHolder(holder: ScheduleViewHolder, position: Int) {
        val singleExerciseSchedule = getItem(position).getSingleSchedule()
        val routineExerciseSchedule = getItem(position).getRoutineSchedule()
        if (singleExerciseSchedule != null) {
            val date = CalendarPlus.toLocaleString(singleExerciseSchedule.date)
            val time = "%02d:%02d - %02d:%02d".format(
                singleExerciseSchedule.start_time.hours,
                singleExerciseSchedule.start_time.minutes,
                singleExerciseSchedule.end_time.hours,
                singleExerciseSchedule.end_time.minutes,
            )
            val target: String = if (singleExerciseSchedule.exercise_type == ExerciseType.Cycling) {
                "Target: %.2f %s".format(
                    singleExerciseSchedule.target,
                    "km",
                )
            } else {
                "Target: %d %s".format(
                    singleExerciseSchedule.target.toInt(),
                    "steps",
                )
            }
            holder.itemView.findViewById<ImageButton>(R.id.schedule_delete).setOnClickListener {
                scheduleSelectedListener.onScheduleSelected(singleExerciseSchedule)
            }
            holder.bind(
                singleExerciseSchedule.exercise_type,
                date,
                time,
                target
            )
        } else if (routineExerciseSchedule != null) {
            val days = routineExerciseSchedule.days.joinToString(separator = "-")
            val time = "%02d:%02d - %02d:%02d".format(
                routineExerciseSchedule.start_time.hours,
                routineExerciseSchedule.start_time.minutes,
                routineExerciseSchedule.end_time.hours,
                routineExerciseSchedule.end_time.minutes,
            )
            val target: String = if (routineExerciseSchedule.exercise_type == ExerciseType.Cycling) {
                "Target: %.2f %s".format(
                    routineExerciseSchedule.target,
                    "km",
                )
            } else {
                "Target: %d %s".format(
                    routineExerciseSchedule.target.toInt(),
                    "steps",
                )
            }
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
        override fun equals(other: Any?): Boolean {
            if (other is Schedule) {
                if (singleExerciseSchedule != null && other.singleExerciseSchedule != null) {
                    return singleExerciseSchedule.id == other.singleExerciseSchedule.id
                } else if (routineExerciseSchedule != null && other.routineExerciseSchedule != null) {
                    return routineExerciseSchedule.id == other.routineExerciseSchedule.id
                }
            }
            return false
        }

        override fun hashCode(): Int {
            var result = singleExerciseSchedule?.hashCode() ?: 0
            result = 31 * result + (routineExerciseSchedule?.hashCode() ?: 0)
            return result
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
            return oldItem == newItem
        }
    }
}