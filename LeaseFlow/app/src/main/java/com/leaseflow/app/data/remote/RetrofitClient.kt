package com.leaseflow.app.data.remote

import com.leaseflow.app.data.remote.api.*
import com.leaseflow.app.data.remote.dto.UserServiceErrorResponse
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import com.google.gson.JsonSyntaxException
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.lang.reflect.Type
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.TimeUnit

/**
 * Cliente Retrofit para comunicacion con microservicios de LeaseFlow
 */
object RetrofitClient {

    // ==================== CONFIGURACION DE URLs ====================
    private const val BASE_URL_USER_SERVICE =
        "https://userservice.calmbeach-1addaf50.brazilsouth.azurecontainerapps.io/"
    private const val BASE_URL_PROPERTY_SERVICE =
        "https://propertyservice.calmbeach-1addaf50.brazilsouth.azurecontainerapps.io/"
    private const val BASE_URL_DOCUMENT_SERVICE =
        "https://documentservice.calmbeach-1addaf50.brazilsouth.azurecontainerapps.io/"
    private const val BASE_URL_APPLICATION_SERVICE =
        "https://applicationservice.calmbeach-1addaf50.brazilsouth.azurecontainerapps.io/"
    private const val BASE_URL_CONTACT_SERVICE =
        "https://contactservice.calmbeach-1addaf50.brazilsouth.azurecontainerapps.io/"
    private const val BASE_URL_REVIEW_SERVICE =
        "https://reviewservice.calmbeach-1addaf50.brazilsouth.azurecontainerapps.io/"

    // ==================== CLAVE DE SEGURIDAD (Capa 1) ====================
    // Misma clave que usan los 6 microservicios y el frontend web.
    // Bloquea acceso directo por URL — el ApiKeyInterceptor del backend
    // exige este header en todos los endpoints /api/**
    private const val APP_CLIENT_KEY = "rentify-leaseflow-dev-key-2026"
    private const val APP_CLIENT_HEADER = "X-App-Client"
    private const val AUTHORIZATION_HEADER = "Authorization"

    @Volatile
    private var authToken: String? = null

    // ==================== INTERCEPTOR X-App-Client ====================

    /**
     * Interceptor OkHttp que agrega X-App-Client a TODAS las requests.
     * Equivalente al getAuthHeaders() del frontend web (apiConfig.ts).
     * No necesita userId porque es la capa 1 (identidad de cliente, no de usuario).
     */
    private val appClientInterceptor = Interceptor { chain ->
        val requestBuilder = chain.request()
            .newBuilder()
            .header(APP_CLIENT_HEADER, APP_CLIENT_KEY)

        authToken?.takeIf { it.isNotBlank() }?.let { token ->
            requestBuilder.header(AUTHORIZATION_HEADER, "Bearer $token")
        }

        val request = requestBuilder.build()
        chain.proceed(request)
    }

    // ==================== CONFIGURACION DE OKHTTP ====================

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(appClientInterceptor)   // <- Capa 1: X-App-Client en todas las requests
        .addInterceptor(loggingInterceptor)
        .connectTimeout(360, TimeUnit.SECONDS)
        .readTimeout(360, TimeUnit.SECONDS)
        .writeTimeout(360, TimeUnit.SECONDS)
        .build()

    // ==================== DESERIALIZADOR DE FECHAS ====================

    private class DateDeserializer : JsonDeserializer<Date>, JsonSerializer<Date> {

        private val dateFormats = listOf(
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.getDefault()),
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.getDefault()),
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()),
            SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()),
            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        ).onEach {
            it.isLenient = true
            it.timeZone = TimeZone.getDefault()
        }

        override fun deserialize(
            json: JsonElement?,
            typeOfT: Type?,
            context: JsonDeserializationContext?
        ): Date? {
            if (json == null || json.isJsonNull) return null
            val dateString = json.asString
            if (dateString.isNullOrBlank()) return null
            for (format in dateFormats) {
                try { return format.parse(dateString) } catch (e: Exception) { }
            }
            return try { Date(dateString.toLong()) } catch (e: Exception) {
                android.util.Log.w("DateDeserializer", "No se pudo parsear fecha: $dateString")
                null
            }
        }

        override fun serialize(
            src: Date?,
            typeOfSrc: Type?,
            context: JsonSerializationContext?
        ): JsonElement {
            if (src == null) return JsonPrimitive("")
            val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            return JsonPrimitive(format.format(src))
        }
    }

    // ==================== CONFIGURACION DE GSON ====================

    private val gson = GsonBuilder()
        .setLenient()
        .registerTypeAdapter(Date::class.java, DateDeserializer())
        .setDateFormat("yyyy-MM-dd'T'HH:mm:ss")
        .create()

    // ==================== FUNCION HELPER PARA CREAR RETROFIT ====================

    private fun createRetrofit(baseUrl: String): Retrofit {
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    fun setAuthToken(token: String?) {
        authToken = token?.takeIf { it.isNotBlank() }
    }

    // ==================== INSTANCIAS DE APIS ====================

    val userServiceApi: UserServiceApi by lazy {
        createRetrofit(BASE_URL_USER_SERVICE).create(UserServiceApi::class.java)
    }

    val propertyServiceApi: PropertyServiceApi by lazy {
        createRetrofit(BASE_URL_PROPERTY_SERVICE).create(PropertyServiceApi::class.java)
    }

    val documentServiceApi: DocumentServiceApi by lazy {
        createRetrofit(BASE_URL_DOCUMENT_SERVICE).create(DocumentServiceApi::class.java)
    }

    val applicationServiceApi: ApplicationServiceApi by lazy {
        createRetrofit(BASE_URL_APPLICATION_SERVICE).create(ApplicationServiceApi::class.java)
    }

    val contactServiceApi: ContactServiceApi by lazy {
        createRetrofit(BASE_URL_CONTACT_SERVICE).create(ContactServiceApi::class.java)
    }

    val reviewServiceApi: ReviewServiceApi by lazy {
        createRetrofit(BASE_URL_REVIEW_SERVICE).create(ReviewServiceApi::class.java)
    }
}

// ==================== SEALED CLASS ApiResult ====================

sealed class ApiResult<out T> {
    data class Success<T>(val data: T) : ApiResult<T>()
    data class Error(
        val message: String,
        val code: Int? = null,
        val errorResponse: UserServiceErrorResponse? = null
    ) : ApiResult<Nothing>()
    object Loading : ApiResult<Nothing>()
}

// ==================== EXTENSION safeApiCall ====================

suspend fun <T> safeApiCall(apiCall: suspend () -> Response<T>): ApiResult<T> {
    return try {
        val response = apiCall()
        if (response.isSuccessful) {
            response.body()?.let {
                ApiResult.Success(it)
            } ?: ApiResult.Error(
                message = "Respuesta vacia del servidor",
                code = response.code()
            )
        } else {
            val errorBody = response.errorBody()?.string()
            val errorResponse = parseErrorResponse(errorBody)
            val message = errorResponse?.getUserFriendlyMessage()
                ?: errorBody
                ?: "Error ${response.code()}: ${response.message()}"
            ApiResult.Error(message = message, code = response.code(), errorResponse = errorResponse)
        }
    } catch (e: Exception) {
        ApiResult.Error(message = getExceptionMessage(e))
    }
}

private fun parseErrorResponse(errorBody: String?): UserServiceErrorResponse? {
    if (errorBody.isNullOrBlank()) return null
    return try {
        GsonBuilder().create().fromJson(errorBody, UserServiceErrorResponse::class.java)
    } catch (e: JsonSyntaxException) {
        android.util.Log.w("RetrofitClient", "No se pudo parsear ErrorResponse: ${e.message}")
        null
    }
}

private fun getExceptionMessage(e: Exception): String {
    return when {
        e is java.net.UnknownHostException -> "Sin conexion a internet. Verifica tu conexion."
        e is java.net.SocketTimeoutException -> "Tiempo de espera agotado. El servidor no responde."
        e is java.net.ConnectException -> "No se pudo conectar al servidor. Verifica que este en ejecucion."
        e is javax.net.ssl.SSLException -> "Error de seguridad en la conexion."
        else -> e.message ?: "Error de conexion desconocido"
    }
}
