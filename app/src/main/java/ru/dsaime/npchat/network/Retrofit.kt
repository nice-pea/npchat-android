package ru.dsaime.npchat.network

import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import com.skydoves.retrofit.adapters.result.ResultCallAdapterFactory
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import okio.IOException
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.lang.reflect.Type
import java.net.SocketTimeoutException
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration

// Создает новый ретрофит экземпляр
fun retrofit(
    bearerTokenProvider: BearerTokenProvider,
    baseUrlProvider: BaseUrlProvider,
): Retrofit {
    val logging =
        HttpLoggingInterceptor()
            .setLevel(HttpLoggingInterceptor.Level.BODY)

    // Инициализировать http клиент
    val client =
        OkHttpClient
            .Builder()
            // Установить перехватчики на http клиент
            .addInterceptor(AuthorizationInterceptor(bearerTokenProvider))
            .addInterceptor(DynamicBaseUrlInterceptor(baseUrlProvider))
            .addInterceptor(logging)
            .addInterceptor(RetryInterceptor(3))
            .callTimeout(10.seconds.toJavaDuration())
            .build()

    // Инициализировать retrofit (обертка http клиента)
    return Retrofit
        .Builder()
        .baseUrl("http://npchat.placeholder:1")
        .client(client)
        .addConverterFactory(GsonConverterFactory.create(retroGson))
        .addCallAdapterFactory(ResultCallAdapterFactory.create())
        .build()
}

// Правило разбора и сериализации json полей типа OffsetDateTime
object OffsetDateTimeAdapter : JsonSerializer<OffsetDateTime>, JsonDeserializer<OffsetDateTime> {
    override fun serialize(
        src: OffsetDateTime?,
        typeOfSrc: Type?,
        context: JsonSerializationContext?,
    ): JsonElement? = src?.let { JsonPrimitive(it.toString()) }

    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?,
    ): OffsetDateTime? = OffsetDateTime.parse(json?.asString, DateTimeFormatter.ISO_OFFSET_DATE_TIME)
}

// Перехватчик, выполняющий повторный запрос, при таймауте
private class RetryInterceptor(
    private val retryAttempts: Int,
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        repeat(retryAttempts) {
            try {
                return chain.proceed(chain.request())
            } catch (e: SocketTimeoutException) {
                e.printStackTrace()
            }
        }
        throw RuntimeException("failed to compile the request")
    }
}

val retroGson =
    GsonBuilder()
        .registerTypeAdapter(OffsetDateTime::class.java, OffsetDateTimeAdapter)
        .create()

fun interface BaseUrlProvider {
    fun baseUrl(): String
}

// Перехватчик, динамически подменяющий baseUrl на выданный провайдером либо переданный через заголовок
private class DynamicBaseUrlInterceptor(
    private val baseUrlProvider: BaseUrlProvider,
) : Interceptor {
    private val overrideHostHeader = "X-Override-Host"

    override fun intercept(chain: Interceptor.Chain): Response {
        // Получить новый base URL
        val baseUrl =
            chain
                .request()
                .headers[overrideHostHeader]
                // Если заголовок пустой, взять из провайдера
                .orEmpty()
                .ifBlank { baseUrlProvider.baseUrl() }
                .run {
                    // Гарантия на случай если формат url будет неверный
                    try {
                        toHttpUrl()
                    } catch (e: Exception) {
                        // Выбросить IOException, чтобы okio смог обработать
                        throw IOException(e.localizedMessage)
                    }
                }

        // Собрать новый URL
        val newUrl =
            chain
                .request()
                .url
                .newBuilder()
                .scheme(baseUrl.scheme)
                .host(baseUrl.host)
                .port(baseUrl.port)
                .build()

        // Собрать запрос с новым URL
        val newRequest =
            chain
                .request()
                .newBuilder()
                .url(newUrl) // Подставить новый url
                .removeHeader(overrideHostHeader) // Удалить заголовок
                .build()

        // Продолжить выполнение цепочки
        return chain.proceed(newRequest)
    }
}

fun interface BearerTokenProvider {
    fun token(): String
}

// Перехватчик, добавляющий токен из prefs в заголовок Authorization
private class AuthorizationInterceptor(
    private val bearerTokenProvider: BearerTokenProvider,
) : Interceptor {
    val authorizationHeader = "Authorization"

    override fun intercept(chain: Interceptor.Chain): Response {
        // Если заголовок уже заполнен, не продолжать
        if (chain.request().header(authorizationHeader).isNullOrBlank()) {
            return chain.proceed(chain.request())
        }

        // Получить токен из провайдера
        val token =
            bearerTokenProvider
                .token()
                // Если токен пустой, выйти
                .ifEmpty { return chain.proceed(chain.request()) }

        return chain
            .request()
            .newBuilder()
            // Добавить заголовок с токеном
            .addHeader(authorizationHeader, "Bearer $token")
            .build()
            .run(chain::proceed)
    }
}
