package com.barteqcz.loqa.location

import android.location.Location
import com.barteqcz.loqa.data.repository.SettingsRepository
import com.barteqcz.loqa.data.model.LocationInfo
import com.barteqcz.loqa.data.util.NetworkResult
import com.google.android.gms.location.Priority
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.roundToInt

@Singleton
class LocationManager @Inject constructor(
    private val locationClient: LocationClient,
    private val locationRepository: LocationRepository,
    private val settingsRepository: SettingsRepository,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    
    private val _currentLocation = MutableStateFlow<Location?>(null)
    val currentLocation: StateFlow<Location?> = _currentLocation.asStateFlow()

    private val _locationInfo = MutableStateFlow(LocationInfo())
    val locationInfo: StateFlow<LocationInfo> = _locationInfo.asStateFlow()

    private var trackingJob: Job? = null

    fun startTracking() {
        if (trackingJob != null) return
        
        trackingJob = scope.launch {
            loadSavedLocation()

            locationClient.getLastLocation()?.let { location ->
                updateLocation(location)
            }

            locationClient.getLocationUpdates(
                interval = 30000L,
                minDistance = 1000f,
                priority = Priority.PRIORITY_BALANCED_POWER_ACCURACY
            ).collect { location ->
                updateLocation(location)
            }
        }
    }

    fun stopTracking() {
        trackingJob?.cancel()
        trackingJob = null
    }

    private suspend fun loadSavedLocation() {
        val settings = settingsRepository.settingsFlow.first()
        if (settings.lastLatitude != null && settings.lastLongitude != null) {
            val savedLoc = Location("saved").apply {
                latitude = settings.lastLatitude
                longitude = settings.lastLongitude
            }
            _currentLocation.value = savedLoc
            
            if (settings.lastCity != null) {
                _locationInfo.value = LocationInfo(
                    city = settings.lastCity,
                    countryCode = settings.lastCountryCode
                )
            }
        }
    }

    private fun updateLocation(location: Location) {
        _currentLocation.value = location
        scope.launch {
            settingsRepository.updateLastLocation(
                city = _locationInfo.value.city,
                code = _locationInfo.value.countryCode,
                latitude = location.latitude,
                longitude = location.longitude
            )
        }
        handleGeocoding(location)
    }

    private fun handleGeocoding(location: Location) {
        scope.launch {
            val result = locationRepository.getAddressesFromLocation(location)
            val addresses = (result as? NetworkResult.Success)?.data ?: return@launch
            val firstAddress = addresses.firstOrNull() ?: return@launch

            val bestCandidate = AddressRefiner.findBestCityCandidate(addresses)
            val newCity = AddressRefiner.cleanCityName(
                bestCandidate ?: extractRawCityName(firstAddress)
            )
            
            val citySearchQuery = buildCitySearchQuery(newCity, firstAddress)
            val isSpecificCity = bestCandidate != null || isValidCityName(firstAddress.locality)
            
            val distKm = if (isSpecificCity && citySearchQuery != null) {
                calculateCityDistance(location, citySearchQuery)
            } else {
                null
            }

            if (newCity != _locationInfo.value.city || distKm != _locationInfo.value.distanceKm) {
                val newInfo = LocationInfo(
                    city = newCity ?: _locationInfo.value.city,
                    country = firstAddress.countryName ?: _locationInfo.value.country,
                    countryCode = firstAddress.countryCode ?: _locationInfo.value.countryCode,
                    distanceKm = distKm
                )
                _locationInfo.value = newInfo
                
                settingsRepository.updateLastLocation(
                    city = newInfo.city,
                    code = newInfo.countryCode,
                    latitude = location.latitude,
                    longitude = location.longitude
                )
            }
        }
    }

    private fun extractRawCityName(address: android.location.Address): String? {
        return listOfNotNull(address.locality, address.subAdminArea, address.adminArea, address.featureName)
            .firstOrNull { isValidCityName(it) }
    }

    private fun isValidCityName(name: String?): Boolean = 
        name != null && name.length > 2 && !name.all { it.isDigit() }

    private fun buildCitySearchQuery(city: String?, address: android.location.Address): String? {
        val terms = listOfNotNull(city, address.subAdminArea, address.adminArea, address.countryName)
            .distinct()
        return if (terms.isNotEmpty()) terms.joinToString(", ") else null
    }

    private suspend fun calculateCityDistance(currentLocation: Location, cityName: String): Int? {
        val result = locationRepository.getCityLocation(cityName, proximity = currentLocation)
        val cityLoc = (result as? NetworkResult.Success)?.data ?: return _locationInfo.value.distanceKm
        val results = FloatArray(1)
        Location.distanceBetween(
            currentLocation.latitude, currentLocation.longitude,
            cityLoc.latitude, cityLoc.longitude,
            results,
        )
        return (results[0] / 1000).roundToInt()
    }
}
