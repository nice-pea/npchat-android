package ru.dsaime.npchat.di

import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import retrofit2.Retrofit
import ru.dsaime.npchat.data.AuthService
import ru.dsaime.npchat.data.AuthServiceBase
import ru.dsaime.npchat.data.EventsFlowProvider
import ru.dsaime.npchat.data.EventsFlowProviderBase
import ru.dsaime.npchat.data.HostService
import ru.dsaime.npchat.data.HostServiceBase
import ru.dsaime.npchat.data.NPChatApi
import ru.dsaime.npchat.data.SessionsService
import ru.dsaime.npchat.data.SessionsServiceBase
import ru.dsaime.npchat.network.retrofit
import ru.dsaime.npchat.screens.login.LoginViewModel
import ru.dsaime.npchat.screens.splash.SplashViewModel


val appModule = module {
    // Retrofit - http client
    single {
        retrofit(
            // Токен провайдер, при отсутствии токена, вернет пустую строку
            bearerTokenProvider = get<SessionsService>()
                .currentSession()
                ?.accessToken::orEmpty,
            // Url провайдер, при отсутствии хоста вернет пустую строку
            baseUrlProvider = get<HostService>()
                .currentHost()::orEmpty
        )
    }

    // NPChatApi -
    single { get<Retrofit>().create(NPChatApi::class.java) }

    // Зависимости
    single<AuthService> { AuthServiceBase(get()) }
    single<HostService> { HostServiceBase(get()) }
    single<SessionsService> { SessionsServiceBase(get(), get()) }
    single<EventsFlowProvider> { EventsFlowProviderBase(get()) }

    // ViewModels
    viewModelOf(::SplashViewModel)
    viewModelOf(::LoginViewModel)
//    viewModelOf(::ChatsViewModel)
}