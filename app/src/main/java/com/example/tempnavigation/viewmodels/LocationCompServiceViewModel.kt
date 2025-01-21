package com.example.tempnavigation.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.tempnavigation.repositories.NoteRepository
import com.example.tempnavigation.repositories.room.NoteRoomDatabase
import com.example.tempnavigation.repositories.room.entity.NoteEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlin.math.cos
import kotlin.math.log

class LocationCompServiceViewModel (application: Application) : AndroidViewModel(application) {
    private val noteRepository: NoteRepository

    val _nearByLocationFlow = MutableStateFlow<List<Pair<String, Pair<Double, Double>>>>(value = emptyList())
    val  nearByLocationFlow: Flow<List<Pair<String, Pair<Double, Double>>>> = _nearByLocationFlow.asStateFlow()
    var catchList = listOf<Pair<String, Pair<Double, Double>>>()

    init {
        val noteDb = NoteRoomDatabase.getDatabase(application)
        noteRepository = NoteRepository(noteDb.noteDao())
    }
    fun getNote(
        id: String,
        onSuccess: (note: NoteEntity) -> Unit,
        onFailed: (message: String) -> Unit
    ) {
        noteRepository.getNoteById(id,
            onSuccess = { note ->
                onSuccess(note)
            },
            onFailed = { onFailed(it) })
    }

    suspend fun getNoteNearbyLocation(lat: Double, lng: Double, radiusInMeters: Double) : List<Pair<String, Pair<Double, Double>>> {

        val boundingBox = calculateBoundingBox(lat, lng, radiusInMeters)
        return suspendCoroutine { continuation ->
            noteRepository.getNearbyLocation(
                boundingBox.first.first,
                boundingBox.first.second,
                boundingBox.second.first,
                boundingBox.second.second,
                { nearbyNotes ->
                    val list = nearbyNotes.map { Pair(it.id, Pair(it.locationLat, it.locationLong)) }
                    continuation.resume(list)
                },
                { continuation.resume(emptyList() )})
        }

    }



    fun calculateBoundingBox(currentLat: Double, currentLng: Double, radiusInMeters: Double): Pair<Pair<Double, Double>, Pair<Double, Double>> {
        val earthRadius = 6378137.0 // Earth's radius in meters

        // Convert radius to angular distance
        val angularDistance = radiusInMeters / earthRadius

        // Convert current location to radians
        val currentLatRad = Math.toRadians(currentLat)
        val currentLngRad = Math.toRadians(currentLng)

        // Calculate new latitude and longitude offsets
        val latOffset = angularDistance
        val lngOffset = angularDistance / cos(currentLatRad)

        // Calculate the bounding box
        val minLat = currentLat - Math.toDegrees(latOffset)
        val maxLat = currentLat + Math.toDegrees(latOffset)
        val minLng = currentLng - Math.toDegrees(lngOffset)
        val maxLng = currentLng + Math.toDegrees(lngOffset)

        return Pair(Pair(minLat, minLng), Pair(maxLat, maxLng))
    }



}