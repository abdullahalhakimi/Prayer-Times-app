package com.example.ui

import android.app.Application
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import com.example.data.QiblaMathUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.math.sqrt

class CompassViewModel(application: Application) : AndroidViewModel(application), SensorEventListener {

    private val sensorManager = application.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometer: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val magnetometer: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

    private val _azimuth = MutableStateFlow(0f)         // Phone rotation angle relative to True North
    val azimuth: StateFlow<Float> = _azimuth

    private val _qiblaBearing = MutableStateFlow(119f)   // absolute Qibla angle from user location to Mecca
    val qiblaBearing: StateFlow<Float> = _qiblaBearing

    private val _magneticField = MutableStateFlow(48.2f) // magnetic field strength in microTesla (µT)
    val magneticField: StateFlow<Float> = _magneticField

    private var gravityValues = FloatArray(3)
    private var geomagneticValues = FloatArray(3)
    private var hasGravity = false
    private var hasGeomagnetic = false

    // Default current coordinate (City of London)
    private var currentLat = 51.5074
    private var currentLon = -0.1278

    init {
        updateLocation(currentLat, currentLon)
    }

    fun updateLocation(lat: Double, lon: Double) {
        currentLat = lat
        currentLon = lon
        val bearing = QiblaMathUtils.calculateQiblaBearing(lat, lon)
        _qiblaBearing.value = bearing.toFloat()
    }

    fun registerListeners() {
        accelerometer?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
        magnetometer?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
    }

    fun unregisterListeners() {
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            System.arraycopy(event.values, 0, gravityValues, 0, event.values.size)
            hasGravity = true
        } else if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
            System.arraycopy(event.values, 0, geomagneticValues, 0, event.values.size)
            hasGeomagnetic = true
            
            // Calculate Magnetic Field Strength in microTesla (µT)
            val strength = sqrt(
                event.values[0] * event.values[0] +
                event.values[1] * event.values[1] +
                event.values[2] * event.values[2]
            )
            _magneticField.value = strength
        }

        if (hasGravity && hasGeomagnetic) {
            val rValues = FloatArray(9)
            val iValues = FloatArray(9)
            val success = SensorManager.getRotationMatrix(rValues, iValues, gravityValues, geomagneticValues)
            if (success) {
                val orientationValues = FloatArray(3)
                SensorManager.getOrientation(rValues, orientationValues)
                val azimuthRad = orientationValues[0]
                var azimuthDeg = Math.toDegrees(azimuthRad.toDouble()).toFloat()
                azimuthDeg = (azimuthDeg + 360) % 360
                _azimuth.value = azimuthDeg
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
        // Not used
    }

    override fun onCleared() {
        super.onCleared()
        unregisterListeners()
    }
}
