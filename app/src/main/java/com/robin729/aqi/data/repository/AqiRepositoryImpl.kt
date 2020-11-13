package com.robin729.aqi.data.repository

import com.mapbox.mapboxsdk.geometry.LatLng
import com.robin729.aqi.data.model.Resource
import com.robin729.aqi.data.model.aqi.Info
import com.robin729.aqi.data.model.favouritesAqi.Response
import com.robin729.aqi.data.model.favouritesAqi.Result
import com.robin729.aqi.data.model.mapsAqi.MapsAqiData
import com.robin729.aqi.data.model.weather.WeatherData
import com.robin729.aqi.network.retrofit.AqiApi
import com.robin729.aqi.network.retrofit.MapsAqiApi
import com.robin729.aqi.network.retrofit.WeathersApi
import com.robin729.aqi.utils.Constants
import com.robin729.aqi.utils.StoreSession
import com.robin729.aqi.utils.Util
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext

class AqiRepositoryImpl : AqiRepository {

    private val favouritesLatLngList: HashSet<LatLng> by lazy {
        StoreSession.readFavouritesLatLng(Constants.FAVOURITES_LIST)
    }

    private val apiKey = StoreSession.readString(Constants.API_KEY)

    override suspend fun getAirQualityInfo(
        lat: Double,
        long: Double
    ): Resource<Info> = getDataFromService(
        AqiApi.retrofitService.getAqiDataResponse(
            lat,
            long,
            apiKey,
            Constants.FEATURES
        )
    )

    override suspend fun getWeather(lat: Double, long: Double): Resource<WeatherData> =
        getDataFromService(
            WeathersApi.retrofitService.getWeatherData(
                lat,
                long,
                Constants.WEATHER_KEY,
                "metric"
            )
        )

    override suspend fun getMapsAqiData(latLngNE: LatLng, latLngSW: LatLng): Resource<MapsAqiData> =
        getDataFromService(MapsAqiApi.retrofitService.getMapsData("?token=${Constants.MAPS_AQI_KEY}&latlng=${latLngNE.latitude},${latLngNE.longitude},${latLngSW.latitude},${latLngSW.longitude}"))

    override suspend fun getAqiPrediction(lat: Double, long: Double): Resource<Result> =
        getDataFromService(
            AqiApi.retrofitService.getAqiForecastData(
                lat,
                long,
                apiKey,
                4,
                Constants.FAVOURITES_FEATURES
            )
        )


    override suspend fun getFavouritesListData(): Resource<ArrayList<com.robin729.aqi.data.model.favouritesAqi.Data>> =
        withContext(Dispatchers.IO) {
            return@withContext try {
                val calls: ArrayList<Deferred<Response>> = ArrayList()
                val favouritesData: ArrayList<com.robin729.aqi.data.model.favouritesAqi.Data> =
                    ArrayList()
                val locationNames: ArrayList<String> = ArrayList()

                favouritesLatLngList.forEach {
                    calls.add(async {
                        AqiApi.retrofitService.getAqiData(
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