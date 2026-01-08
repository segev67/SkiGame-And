package com.example.homework1

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.homework1.databinding.FragmentScoreMapBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

/**
 * Fragment that displays a Google Map and shows the saved location
 * of a selected TopScore entry from the Top Scores list.
 */
class ScoreMapFragment : Fragment(), OnMapReadyCallback {

    private var _binding: FragmentScoreMapBinding? = null
    private val binding get() = _binding!!

    private lateinit var mapView: MapView
    private var googleMap: GoogleMap? = null

    /**
     * Holds the score that was clicked by the user.
     * If the map is not ready yet, the score will be stored here
     * and applied once onMapReady() is called.
     */
    private var pendingScore: TopScore? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        //Inflate view binding
        _binding = FragmentScoreMapBinding.inflate(inflater, container, false)

        //Get reference to MapView from layout
        mapView = binding.mapView

        //Initialize MapView lifecycle state
        mapView.onCreate(savedInstanceState)

        //Register callback to receive GoogleMap once ready
        mapView.getMapAsync(this)

        return binding.root
    }

    //Forward lifecycle events to MapView

    override fun onStart() {
        super.onStart()
        mapView.onStart()
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        mapView.onPause()
        super.onPause()
    }

    override fun onStop() {
        mapView.onStop()
        super.onStop()
    }

    override fun onDestroyView() {

        // Properly destroy MapView to avoid memory leaks
        mapView.onDestroy()

        _binding = null
        super.onDestroyView()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        // Save map instance state
        mapView.onSaveInstanceState(outState)
    }

    /**
     * Called when the Google Map is fully initialized and ready to use.
     * If a score was already selected before map readiness,
     * we update the map with that score's location now.
     */
    override fun onMapReady(map: GoogleMap) {
        googleMap = map

        //Apply pending score if exists
        pendingScore?.let { updateMapForScore(it) }
    }

    /**
     * Called externally from TopScoresActivity when the user
     * clicks on a score item in the list.
     *
     * If the map is ready, update immediately
     * else, store as pending until onMapReady()
     */
    fun showLocation(score: TopScore) {
        pendingScore = score

        val map = googleMap
        if (map != null) {
            updateMapForScore(score)
        }
    }

    /**
     * Clears the map and displays a marker at the score location.
     * If no location exists for this score, a message is shown instead.
     */
    private fun updateMapForScore(score: TopScore) {

        val map = googleMap ?: return

        //Score does not contain location data
        if (!score.hasLocation) {
            Toast.makeText(
                requireContext(),
                "No location was recorded for this score",
                Toast.LENGTH_SHORT
            ).show()

            map.clear()
            return
        }

        //Create LatLng from saved coordinates
        val position = LatLng(score.latitude, score.longitude)

        //Clear old markers
        map.clear()

        //Add marker for selected score
        map.addMarker(
            MarkerOptions()
                .position(position)
                .title(score.playerName)
                .snippet("Score: ${score.score}, Dist: ${score.distance}m")
        )

        //Move camera to location with zoom level
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(position, 15f))
    }
}
