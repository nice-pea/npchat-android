package ru.dsaime.npchat.di

import org.koin.core.module.dsl.viewModelOf
import org.koin.core.scope.Scope
import org.koin.dsl.module
import retrofit2.Retrofit
import ru.dsaime.npchat.data.AuthService
import ru.dsaime.npchat.data.AuthServiceBase
import ru.dsaime.npchat.data.EventsFlowProvider
import ru.dsaime.npchat.data.HostService
import ru.dsaime.npchat.data.HostServiceBase
import ru.dsaime.npchat.data.NPChatApi
import ru.dsaime.npchat.data.SessionsService
import ru.dsaime.npchat.data.SessionsServiceBase
import ru.dsaime.npchat.network.retrofit
import ru.dsaime.npchat.screens.login.LoginViewModel
import ru.dsaime.npchat.screens.login.NPChatClient
import ru.dsaime.npchat.screens.splash.SplashViewModel


val appModule = module {
    // ApiDyn
    single {
        retrofit(
            bearerTokenProvider = get<SessionsService>()
                .currentSession()
                ?.accessToken::orEmpty,
            baseUrlProvider = get<HostService>()
                .currentHost()::orEmpty
        )
    }
    fun <T> Scope.retroApi(service: Class<T>) = get<Retrofit>().create(service)
    single { retroApi(NPChatApi::class.java) }

    // Api
    single { retrofit(get()) }
    fun <T> Scope.retroApi(service: Class<T>) = get<Retrofit>().create(service)
    single { retroApi(NPChatApi::class.java) }

    // Зависимости
    single<AuthService> { AuthServiceBase(get()) }
    single<HostService> { HostServiceBase(get()) }
    single<SessionsService> { SessionsServiceBase(get(), get()) }
    single<EventsFlowProvider> { EventsFlowProviderBase() }

    // ViewModels
    viewModelOf(::SplashViewModel)
    single<NPChatClient> {
        object : NPChatClient {
            override fun ping(server: String): Result<Unit> {
                return listOf(
                    Result.success(Unit),
                    Result.failure<Unit>(SecurityException())
                ).random()
            }
        }
    }
    viewModelOf(::LoginViewModel)
//    viewModelOf(::ChatsViewModel)
}