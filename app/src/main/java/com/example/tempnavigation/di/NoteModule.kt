package com.example.tempnavigation.di

import android.content.Context
import com.example.tempnavigation.helpers.GeoFancingManger
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.LocationServices
import dagger.Module
import dagger.Provides
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NoteModule {

    @Provides
    fun provideGeoFancingClient(
        @ApplicationContext context: Context
    ): GeofencingClient {
        return LocationServices.getGeofencingClient(context)
    }
    @Provides
    fun geoFancingManger(
        @ApplicationContext context: Context,
        geofencingClient: GeofencingClient
    ): GeoFancingManger{
        return GeoFancingManger(context,geofencingClient)
    }
}