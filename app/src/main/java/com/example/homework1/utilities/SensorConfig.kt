package com.example.homework1.utilities

/**
 * Configuration values for accelerometer-based controls.
 * All sensor sensitivity and timing parameters are defined here
 */
object SensorConfig {
    const val MAX_TILT = 5.5f

    // Back-and-forth speed bonus (Y axis)
    const val Y_SWING_THRESHOLD = 2.0f
    const val SPEED_BOOST_DURATION_MS = 2000L
    const val FAST_SPEED_MULTIPLIER = 2
}
