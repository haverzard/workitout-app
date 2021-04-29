package com.haverzard.workitout.ui.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.navArgs
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.PolylineOptions
import com.haverzard.workitout.R
import com.haverzard.workitout.WorkOutApplication
import com.haverzard.workitout.entities.ExerciseType
import com.haverzard.workitout.entities.History
import com.haverzard.workitout.util.CalendarPlus


class HistoryDetailFragment : Fragment(), OnMapReadyCallback {

    private lateinit var historyViewModel: HistoryViewModel
    private lateinit var mapView: MapView
    private val safeArgs: HistoryDetailFragmentArgs by navArgs()

    private var gMap: GoogleMap? = null
    private var history: History? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        historyViewModel = ViewModelProviders.of(
            this, HistoryViewModelFactory((activity?.application as WorkOutApplication).repository)
        ).get(HistoryViewModel::class.java)

        val historyId = if (arguments != null) arguments!!.getInt("history_id") else safeArgs.historyId
        val root = inflater.inflate(R.layout.fragment_history_detail, container, false)
        mapView = root.findViewById(R.id.map_route)
        mapView.onCreate(savedInstanceState)
        mapView.visibility = View.GONE

        historyViewModel.getHistory(historyId)
        historyViewModel.currentHistory.observe(this) {
            if (it != null) {
                val date = CalendarPlus.toLocaleString(it.date)
                val time = "%02d:%02d - %02d:%02d".format(
                    it.start_time.hours,
                    it.start_time.minutes,
                    it.end_time.hours,
                    it.end_time.minutes,
                )
                val target: String = if (it.exercise_type == ExerciseType.Cycling) {
                    "You have cycled for  %.2f km".format(
                        it.target_reached
                    )
                } else {
                    "You have walked for %d steps".format(
                        it.target_reached.toInt()
                    )
                }

                root.findViewById<TextView>(R.id.date).text = date
                root.findViewById<TextView>(R.id.time).text = time
                root.findViewById<TextView>(R.id.target).text = target

                if (it.exercise_type == ExerciseType.Cycling) {
                    history = it
                    mapView.getMapAsync(this)
                    mapView.visibility = View.VISIBLE
                }
            }
        }
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }

    override fun onMapReady(map: GoogleMap?) {
        gMap = map
        val points: List<LatLng> = history!!.points

        if (points.isNotEmpty()) {
            gMap?.addPolyline(
                PolylineOptions()
                    .clickable(true)
                    .addAll(points)
            )

            val builder = LatLngBounds.Builder()
            points.forEach {
                builder.include(it)
            }
            val bounds = builder.build()
            val cu = CameraUpdateFactory.newLatLngBounds(bounds, 50)
            gMap?.moveCamera(cu)
        }
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }
}
