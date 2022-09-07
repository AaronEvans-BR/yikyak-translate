package com.yikyaktranslate

import android.app.Application
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.yikyaktranslate.presentation.domain.TranslateLogic
import com.yikyaktranslate.presentation.viewmodel.TranslateViewModel
import com.yikyaktranslate.service.face.TranslationApi
import com.yikyaktranslate.service.face.TranslationService
import okhttp3.OkHttpClient
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import timber.log.Timber
import java.util.concurrent.TimeUnit

class YikYakTranslateApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        Timber.i("Aaron: launch application")
        /**
         * Original implementation initialized several classes involved in the creation of the service each time it was called.
         * Dependency injection isn't really required to solve that problem but its' still invaluable to practice.
         *
         * It Simplifies future object initialization for multiple view models and encourages modularization of our code for testing.
         */
        Timber.plant(Timber.DebugTree())
        startKoin {
            Timber.i("Aaron: start koin")
            androidLogger()
            androidContext(this@YikYakTranslateApplication)
            modules(modules = listOf(module {
                single {
                    Moshi.Builder()
                        .add(KotlinJsonAdapterFactory())
                        .build()
                }
                single {
                    OkHttpClient()
                        .newBuilder()
                        .callTimeout(20, TimeUnit.SECONDS)
                        .build()
                }
                single<Retrofit> {
                    Retrofit.Builder()
                        .baseUrl(BuildConfig.BASE_URL)
                        .client(get())
                        .addConverterFactory(MoshiConverterFactory.create(get()))
                        .build()

                }
                single<TranslationApi> {
                    get<Retrofit>().create(TranslationApi::class.java)
                }
                single {
                    TranslationService(get())
                }
                single {
                    TranslateViewModel(get(), get(), get())
                }
                single {
                    TranslateLogic()
                }
            }))
        }
    }
}
