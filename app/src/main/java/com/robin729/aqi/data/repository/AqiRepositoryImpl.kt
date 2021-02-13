package com.robin729.aqi.data.repository

import android.content.Context
import com.mapbox.mapboxsdk.geometry.LatLng
import com.robin729.aqi.R
import com.robin729.aqi.data.model.Resource
import com.robin729.aqi.data.model.aqi.Info
import com.robin729.aqi.data.model.favouritesAqi.Response
import com.robin729.aqi.data.model.favouritesAqi.Result
import com.robin729.aqi.data.model.mapsAqi.MapsAqiData
import com.robin729.aqi.data.model.weather.WeatherData
import com.robin729.aqi.network.AqiService
import com.robin729.aqi.network.MapsAqiService
import com.robin729.aqi.network.WeatherService
import com.robin729.aqi.utils.Constants
import com.robin729.aqi.utils.PreferenceRepository
import com.robin729.aqi.utils.Util
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first

class AqiRepositoryImpl(
    context: Context,
    private val preferenceRepository: PreferenceRepository,
    private val aqiService: AqiService,
    private val mapsAqiService: MapsAqiService,
    private val weatherService: WeatherService
) : AqiRepository {

    private val mapsKey = context.getString(R.string.MAPS_AQI_KEY)
    private val weatherKey = context.getString(R.string.WEATHER_KEY)

    override suspend fun getAirQualityInfo(
        lat: Double,
        long: Double
    ): Resource<Info> = getDataFromService(
        aqiService.getAqiDataResponse(
            lat,
            long,
            preferenceRepository.getApiKey().first() ?: "",
            Constants.FEATURES
        )
    )

    override suspend fun getWeather(lat: Double, long: Double): Resource<WeatherData> =
        getDataFromService(
            weatherService.getWeatherData(
                lat,
                long,
                weatherKey,
                "metric"
            )
        )

    override suspend fun getMapsAqiData(latLngNE: LatLng, latLngSW: LatLng): Resource<MapsAqiData> =
        getDataFromService(mapsAqiService.getMapsData("?token=$mapsKey&latlng=${latLngNE.latitude},${latLngNE.longitude},${latLngSW.latitude},${latLngSW.longitude}"))

    override suspend fun getAqiPrediction(lat: Double, long: Double): Resource<Result> =
        getDataFromService(
            aqiService.getAqiForecastData(
                lat,
                long,
                preferenceRepository.getApiKey().first() ?: "",
                12,
                Constants.FAVOURITES_FEATURES
            )
        )


    override suspend fun getFavouritesListData(): Resource<ArrayList<com.robin729.aqi.data.model.favouritesAqi.Data>> =
        withContext(Dispatchers.IO) {
            delay(50)
            val favLatLngList = preferenceRepository.getFavLatLngList().first()
            val calls: ArrayList<Deferred<Response>> = ArrayList()
            val favouritesData: ArrayList<com.robin729.aqi.data.model.favouritesAqi.Data> =
                ArrayList()
            val locationNames: ArrayList<String> = ArrayList()
            val apiKey = preferenceRepository.getApiKey().first() ?: ""
            return@withContext try {
                favLatLngList.forEach {
                    calls.add(async {
                        aqiService.getAqiData(
                            it.latitude,
                            it.longitude,
                            apiKey,
                            Constants.FAVOURITES_FEATURES
                        )
                    })
                    locationNames.add(Util.getLocationString(it))
                }

                calls.forEach {
                    favouritesData.add(it.await().data)
                    it.await().data.locName = locationNames[calls.indexOf(it)]
                    it.await().data.latLng = favLatLngList.elementAt(calls.indexOf(it))
                }

                Resource.Success(favouritesData)
            } catch (exception: Exception) {
                Resource.Error("Something went wrong ${exception.message}", null)
            }
        }


    private suspend inline fun <reified T> getDataFromService(result: retrofit2.Response<T>): Resource<T> =
        withContext(Dispatchers.IO) {
            return@withContext try {
                if (result.isSuccessful) {
                    Resource.Success(result.body()!!)
                } else {
                    Resource.Error(
                        "Something went wrong ${result.message()}",
                        null
                    )
                }
            } catch (exception: Exception) {
                Resource.Error("Something went wrong ${exception.message}", null)
            }
        }
}