package com.example.ui.qibla

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class CompassSensorManager(context: Context) {
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    fun getAzimuthFlow(): Flow<Float> = callbackFlow {
        var gravity: FloatArray? = null
        var geomagnetic: FloatArray? = null
        var smoothedAzimuth = 0f
        var lastEmittedAzimuth = -1f

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                if (event == null) return

                when (event.sensor.type) {
                    Sensor.TYPE_ROTATION_VECTOR -> {
                        val rotationMatrix = FloatArray(9)
                        SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
                        val orientation = FloatArray(3)
                        SensorManager.getOrientation(rotationMatrix, orientation)
                        var azimuth = Math.toDegrees(orientation[0].toDouble()).toFloat()
                        if (azimuth < 0) azimuth += 360f

                        // Smooth interpolation
                        smoothedAzimuth = smoothAzimuth(smoothedAzimuth, azimuth)
                        emitIfSignificant(smoothedAzimuth)
                    }
                    Sensor.TYPE_ACCELEROMETER -> {
                        gravity = event.values.clone()
                        computeFromAccelAndMag()
                    }
                    Sensor.TYPE_MAGNETIC_FIELD -> {
                        geomagnetic = event.values.clone()
                        computeFromAccelAndMag()
                    }
                }
            }

            private fun emitIfSignificant(azimuth: Float) {
                if (lastEmittedAzimuth < 0f) {
                    lastEmittedAzimuth = azimuth
                    trySend(azimuth)
                    return
                }
                var diff = kotlin.math.abs(azimuth - lastEmittedAzimuth)
                if (diff > 180f) diff = 360f - diff
                if (diff >= 0.35f) {
                    lastEmittedAzimuth = azimuth
                    trySend(azimuth)
                }
            }

            private fun computeFromAccelAndMag() {
                val g = gravity ?: return
                val m = geomagnetic ?: return

                val r = FloatArray(9)
                val i = FloatArray(9)
                if (SensorManager.getRotationMatrix(r, i, g, m)) {
                    val orientation = FloatArray(3)
                    SensorManager.getOrientation(r, orientation)
                    var azimuth = Math.toDegrees(orientation[0].toDouble()).toFloat()
                    if (azimuth < 0) azimuth += 360f
                    smoothedAzimuth = smoothAzimuth(smoothedAzimuth, azimuth)
                    emitIfSignificant(smoothedAzimuth)
                }
            }

            private fun smoothAzimuth(current: Float, target: Float): Float {
                var diff = target - current
                while (diff < -180) diff += 360
                while (diff > 180) diff -= 360
                return (current + diff * 0.2f + 360) % 360
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        val rotationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        val accelSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        val magSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

        if (rotationSensor != null) {
            sensorManager.registerListener(listener, rotationSensor, SensorManager.SENSOR_DELAY_UI)
        } else {
            if (accelSensor != null) sensorManager.registerListener(listener, accelSensor, SensorManager.SENSOR_DELAY_UI)
            if (magSensor != null) sensorManager.registerListener(listener, magSensor, SensorManager.SENSOR_DELAY_UI)
        }

        awaitClose {
            sensorManager.unregisterListener(listener)
        }
    }
}
