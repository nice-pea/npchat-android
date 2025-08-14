package ru.dsaime.npchat.network

import com.google.gson.FieldNamingPolicy
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.skydoves.retrofit.adapters.result.ResultCallAdapterFactory
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import ru.dsaime.npchat.data.NPChatLocalPrefs
import java.lang.reflect.Type
import java.net.SocketTimeoutException
import java.net.URL
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration

// Создает новый ретрофит экземпляр
fun retrofit(
    localPrefs: NPChatLocalPrefs,
): Retrofit {
    val logging = HttpLoggingInterceptor()
        .setLevel(HttpLoggingInterceptor.Level.BODY)

    val client = OkHttpClient.Builder()
        // Добавить
        .addInterceptor(AuthorizationInterceptor(localPrefs))
        .addInterceptor(ReplaceNpcUrlPlaceholderInterceptor(localPrefs))
        .addInterceptor(logging)
        .addInterceptor(RetryInterceptor(3))
        .callTimeout(10.seconds.toJavaDuration())
        .build()

    val gson = GsonBuilder()
        .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
        .registerTypeAdapter(OffsetDateTime::class.java, OffsetDateTimeAdapter)
        .create()

    return Retrofit.Builder()
        .baseUrl(NpcUrlPlaceholder)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .addCallAdapterFactory(ResultCallAdapterFactory.create())
        .build()
}

private object OffsetDateTimeAdapter : JsonDeserializer<OffsetDateTime> {
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): OffsetDateTime? {
        return OffsetDateTime.parse(json?.asString, DateTimeFormatter.ISO_OFFSET_DATE_TIME)
    }
}

private class RetryInterceptor(private val retryAttempts: Int) : Interceptor {
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

const val NpcUrlPlaceholder = "http://<npc_host>:7511"

fun npcBaseUrl(store: NPChatLocalPrefs, default: String = ""): String {
    return if (store.baseUrl != "") {
        store.baseUrl
    } else if (default != "") {
        default
    } else {
        NpcUrlPlaceholder
    }
}

private class ReplaceNpcUrlPlaceholderInterceptor(
    private val store: NPChatLocalPrefs,
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val urlString = chain.request().url.toString()
        if (urlString.startsWith(NpcUrlPlaceholder) && store.baseUrl != "") {
            val newUrl = URL(store.baseUrl + urlString.removePrefix(NpcUrlPlaceholder))
            val request = chain.request().newBuilder()
                .url(newUrl)
                .build()
            return chain.proceed(request)
        }

        return chain.proceed(chain.request())
    }
}

const val AuthorizationHeader = "Authorization"
const val AuthorizationType = "NpcUserToken"

fun authzHeaderValue(token: String): String {
    return "$AuthorizationType $token"
}

// Перехватчик, добавляющий токен из prefs в заголовок Authorization
private class AuthorizationInterceptor(
    private val localPrefs: NPChatLocalPrefs,
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        if (localPrefs.token != "" && chain.request().header(AuthorizationHeader) == null) {
            return chain.request().newBuilder()
                .addHeader(AuthorizationHeader, authzHeaderValue(localPrefs.token))
                .build()
                .run(chain::proceed)
        }

        return chain.proceed(chain.request())
    }
}