package com.haverzard.workitout.ui.history

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
import com.haverzard.workitout.entities.History
import com.haverzard.workitout.util.CalendarPlus


class HistoryListAdapter(private val historySelectedListener: HistorySelectedListener) : ListAdapter<History, HistoryListAdapter.HistoryViewHolder>(
    HistoriesComparator()
) {

    interface HistorySelectedListener {
        fun onHistorySelected(history: History)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        return HistoryViewHolder.create(parent)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        val history = getItem(position)
        val date = CalendarPlus.toLocaleString(history.date)
        val time = "%02d:%02d - %02d:%02d".format(
            history.start_time.hours,
            history.start_time.minutes,
            history.end_time.hours,
            history.end_time.minutes,
        )
        val target: String = if (history.exercise_type == ExerciseType.Cycling) {
            "Reached: %.2f %s".format(
                history.target_reached,
                "km",
            )
        } else {
            "Reached: %d %s".format(
                history.target_reached.toInt(),
                "steps",
            )
        }
        holder.itemView.findViewById<ImageButton>(R.id.show_detail).setOnClickListener {
            historySelectedListener.onHistorySelected(history)
        }
        holder.bind(history.exercise_type, date, time, target)
    }

    class HistoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
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
            fun create(parent: ViewGroup): HistoryViewHolder {
                val view: View = LayoutInflater.from(parent.context)
                    .inflate(R.layout.history_item, parent, false)
                return HistoryViewHolder(
                    view
                )
            }
        }
    }

    class HistoriesComparator : DiffUtil.ItemCallback<History>() {
        override fun areItemsTheSame(
            oldItem: History,
            newItem: History
        ): Boolean {
            return oldItem === newItem
        }

        override fun areContentsTheSame(
            oldItem: History,
            newItem: History
        ): Boolean {
            return oldItem.id == newItem.id
        }
    }
}