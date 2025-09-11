package ru.dsaime.npchat.di.koin

import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.sse.SSE
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import retrofit2.Retrofit
import ru.dsaime.npchat.data.AuthService
import ru.dsaime.npchat.data.AuthServiceBase
import ru.dsaime.npchat.data.EventsFlowProvider
import ru.dsaime.npchat.data.EventsFlowProviderKtorSSE
import ru.dsaime.npchat.data.HostService
import ru.dsaime.npchat.data.HostServiceBase
import ru.dsaime.npchat.data.NPChatApi
import ru.dsaime.npchat.data.SessionsService
import ru.dsaime.npchat.data.SessionsServiceBase
import ru.dsaime.npchat.network.BaseUrlProvider
import ru.dsaime.npchat.network.BearerTokenProvider
import ru.dsaime.npchat.network.retrofit
import ru.dsaime.npchat.screens.login.LoginViewModel
import ru.dsaime.npchat.screens.splash.SplashViewModel


val appModule = module {
    // Url провайдер, при отсутствии хоста вернет пустую строку
    single {
        BaseUrlProvider {
            get<HostService>().currentHost().orEmpty()
        }
    }

    // Токен провайдер, при отсутствии токена, вернет пустую строку
    single {
        BearerTokenProvider {
            get<SessionsService>()
                .currentSession()?.accessToken.orEmpty()
        }
    }

    // Ktor - http client
    single {
        HttpClient(OkHttp) {
            install(SSE)
        }
    }

    // Retrofit - http client
    single {
        retrofit(get(), get())
    }

    // NPChatApi -
    single<NPChatApi> { get<Retrofit>().create(NPChatApi::class.java) }


    // Зависимости
    single<AuthService> { AuthServiceBase(get()) }
    single<HostService> { HostServiceBase(get()) }
    single<SessionsService> { SessionsServiceBase(get(), get()) }
    single<EventsFlowProvider> { EventsFlowProviderKtorSSE(get(), get()) }

    // ViewModels
    viewModelOf(::SplashViewModel)
    viewModelOf(::LoginViewModel)
//    viewModelOf(::ChatsViewModel)
}