package com.robin729.aqi.di

import android.content.Context
import com.robin729.aqi.data.repository.AqiRepository
import com.robin729.aqi.data.repository.AqiRepositoryImpl
import com.robin729.aqi.network.AqiService
import com.robin729.aqi.network.MapsAqiService
import com.robin729.aqi.network.WeatherService
import com.robin729.aqi.utils.Constants.AQI_BASE_URL
import com.robin729.aqi.utils.Constants.MAPS_BASE_URL
import com.robin729.aqi.utils.Constants.WEATHER_BASE_URL
import com.robin729.aqi.utils.PreferenceRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@Module
@InstallIn(SingletonComponent::class)
class AppModule {

    private val loggingInterceptor by lazy {
        HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY)
    }

    private val okHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .build()
    }

    private fun createRetrofitInstance(baseURL: String): Retrofit = Retrofit.Builder()
        .baseUrl(baseURL)
        .addConverterFactory(GsonConverterFactory.create())
        .client(okHttpClient)
        .build()


    @Provides
    fun provideAqiService(): AqiService =
        createRetrofitInstance(AQI_BASE_URL).create(AqiService::class.java)

    @Provides
    fun provideMapsService(): MapsAqiService =
        createRetrofitInstance(MAPS_BASE_URL).create(MapsAqiService::class.java)

    @Provides
    fun provideWeatherService(): WeatherService =
        createRetrofitInstance(WEATHER_BASE_URL).create(WeatherService::class.java)

    @Provides
    fun preferenceRepository(@ApplicationContext context: Context) = PreferenceRepository(context)

    @Provides
    fun providesRepository(
        @ApplicationContext context: Context,
        preferenceRepository: PreferenceRepository,
        aqiService: AqiService,
        mapsAqiService: MapsAqiService,
        weatherService: WeatherService
    ): AqiRepository = AqiRepositoryImpl(context, preferenceRepository, aqiService, mapsAqiService, weatherService)
}