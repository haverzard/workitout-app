package com.haverzard.workitout.ui.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import com.haverzard.workitout.R
import com.haverzard.workitout.WorkOutApplication
import com.haverzard.workitout.viewmodel.ScheduleViewModel
import com.haverzard.workitout.viewmodel.ScheduleViewModelFactory


class HistoryDetailFragment : Fragment(), OnMapReadyCallback {

    private lateinit var scheduleViewModel: ScheduleViewModel
    private lateinit var mapView: MapView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        scheduleViewModel = ViewModelProviders.of(
            this, ScheduleViewModelFactory((activity?.application as WorkOutApplication).repository)
        ).get(ScheduleViewModel::class.java)

        val root = inflater.inflate(R.layout.fragment_history_detail, container, false)
        mapView = root.findViewById(R.id.map_route)
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)


//        val recyclerView = root.findViewById<RecyclerView>(R.id.recyclerview)
//        val adapter = ScheduleListAdapter()
//        recyclerView.adapter = adapter
//        recyclerView.layoutManager = LinearLayoutManager(activity)

//        scheduleViewModel.schedules.observe(owner = this) { schedules ->
//            schedules.let { adapter.submitList(it) }
//        }
        return root
    }

    override fun onMapReady(map: GoogleMap?) {
//        mapV
        System.out.println(map)
        val polyline: Polyline? = map?.addPolyline(
            PolylineOptions()
                .clickable(true)
                .add(
                    LatLng(-35.016, 143.321),
                    LatLng(-34.747, 145.592),
                    LatLng(-34.364, 147.891),
                    LatLng(-33.501, 150.217),
                    LatLng(-32.306, 149.248),
                    LatLng(-32.491, 147.309)
                )
        )
//        map?.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(-23.684, 133.903), 4f))
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }
}
