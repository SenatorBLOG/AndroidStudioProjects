package com.breatheonline.breathe.data.api

import com.breatheonline.breathe.BuildConfig
import com.breatheonline.breathe.utils.AuthEvent
import com.breatheonline.breathe.utils.AuthEventBus
import com.breatheonline.breathe.utils.TokenManager
import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

/**
 * Hilt network module — provides [OkHttpClient], [Retrofit], and [ApiService]
 * as app-scoped singletons (mirrors the existing DatabaseModule pattern).
 *
 * The OkHttp interceptor reads the JWT from [TokenManager] on every request,
 * so a token saved after construction is picked up automatically.
 */
@Module
@InstallIn(SingletonComponent::class)
object RetrofitClient {

    private const val BASE_URL        = "https://breathe-api-amut.onrender.com/api/"
    private const val TIMEOUT_SECONDS = 30L

    // ── OkHttpClient ──────────────────────────────────────────────────────────

    @Provides
    @Singleton
    fun provideOkHttpClient(tokenManager: TokenManager): OkHttpClient {
        return OkHttpClient.Builder()
            // Auth interceptor: attaches "Authorization: Bearer <token>" when logged in
            .addInterceptor { chain ->
                val request = tokenManager.getToken()?.let { token ->
                    chain.request().newBuilder()
                        .header("Authorization", "Bearer $token")
                        .build()
                } ?: chain.request()
                chain.proceed(request)
            }
            // 401 interceptor: clear token and signal logout on Unauthorized response
            .addInterceptor { chain ->
                val response = chain.proceed(chain.request())
                if (response.code == 401) {
                    tokenManager.clearToken()
                    AuthEventBus.emit(AuthEvent.Unauthorized)
                }
                response
            }
            // Logging interceptor: full request/response body in debug builds only
            .apply {
                if (BuildConfig.DEBUG) {
                    addInterceptor(
                        HttpLoggingInterceptor().apply {
                            level = HttpLoggingInterceptor.Level.BODY
                        }
                    )
                }
            }
            .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .writeTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .build()
    }

    // ── Retrofit ──────────────────────────────────────────────────────────────

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    // ── ApiService ────────────────────────────────────────────────────────────

    @Provides
    @Singleton
    fun provideApiService(retrofit: Retrofit): ApiService {
        return retrofit.create(ApiService::class.java)
    }

    // ── Shared Gson instance ──────────────────────────────────────────────────

    @Provides
    @Singleton
    fun provideGson(): Gson = Gson()

    // ── Bare OkHttpClient (no auth interceptors) for external APIs ────────────

    @Provides
    @Singleton
    @Named("bare")
    fun provideBareOkHttpClient(): OkHttpClient =
        OkHttpClient.Builder()
            .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .writeTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .build()
}
