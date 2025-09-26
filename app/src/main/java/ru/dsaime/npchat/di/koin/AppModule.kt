package ru.dsaime.npchat.di.koin

import androidx.room.Room
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.logging.DEFAULT
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.sse.SSE
import kotlinx.coroutines.runBlocking
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import retrofit2.Retrofit
import ru.dsaime.npchat.data.BasicAuthService
import ru.dsaime.npchat.data.BasicAuthServiceBase
import ru.dsaime.npchat.data.ChatsService
import ru.dsaime.npchat.data.ChatsServiceBase
import ru.dsaime.npchat.data.EventsFlowProvider
import ru.dsaime.npchat.data.EventsFlowProviderKtorSSE
import ru.dsaime.npchat.data.EventsService
import ru.dsaime.npchat.data.EventsServiceBase
import ru.dsaime.npchat.data.HostService
import ru.dsaime.npchat.data.HostServiceBase
import ru.dsaime.npchat.data.NPChatApi
import ru.dsaime.npchat.data.SessionsService
import ru.dsaime.npchat.data.SessionsServiceBase
import ru.dsaime.npchat.data.room.AppDatabase
import ru.dsaime.npchat.data.room.InitialCallback
import ru.dsaime.npchat.network.BaseUrlProvider
import ru.dsaime.npchat.network.SessionTokenProvider
import ru.dsaime.npchat.network.retrofit
import ru.dsaime.npchat.screens.chat.chats.ChatsViewModel
import ru.dsaime.npchat.screens.chat.create.CreateChatViewModel
import ru.dsaime.npchat.screens.control.main.ControlViewModel
import ru.dsaime.npchat.screens.control.profile.ProfileViewModel
import ru.dsaime.npchat.screens.home.HomeViewModel
import ru.dsaime.npchat.screens.hosts.add.AddHostViewModel
import ru.dsaime.npchat.screens.hosts.select.HostSelectViewModel
import ru.dsaime.npchat.screens.login.LoginViewModel
import ru.dsaime.npchat.screens.registration.RegistrationViewModel
import ru.dsaime.npchat.screens.splash.SplashViewModel

val appModule =
    module {
        single<AppDatabase> {
            Room
                .databaseBuilder(androidContext(), AppDatabase::class.java, "main")
                .addCallback(InitialCallback(this@single))
//                .allowMainThreadQueries()
                .build()
        }

        // Url провайдер, при отсутствии хоста вернет пустую строку
        single<BaseUrlProvider> {
            BaseUrlProvider {
                runBlocking {
                    get<HostService>().currentBaseUrl().orEmpty()
                }
            }
        }

        // Токен провайдер, при отсутствии токена, вернет пустую строку
        single<SessionTokenProvider> {
            SessionTokenProvider {
                runBlocking {
                    get<SessionsService>()
                        .currentSession()
                        ?.accessToken
                        .orEmpty()
                }
            }
        }

        // Ktor - http client
        single<HttpClient> {
            HttpClient(OkHttp) {
                install(Logging) {
                    logger = Logger.DEFAULT
                    level = LogLevel.ALL
                }
                install(SSE)
            }
        }

        // Retrofit - http client
        single<Retrofit> { retrofit(get(), get()) }

        // NPChatApi -
        single<NPChatApi> { get<Retrofit>().create(NPChatApi::class.java) }

        // Зависимости
        single<BasicAuthService> { BasicAuthServiceBase(get()) }
        single<HostService> { HostServiceBase(get(), get()) }
        single<SessionsService> { SessionsServiceBase(get(), get(), get()) }
        single<EventsFlowProvider> { EventsFlowProviderKtorSSE(get(), get()) }
        single<ChatsService> { ChatsServiceBase(get()) }
        single<EventsService> { EventsServiceBase(get(), get()) }

        // Экраны
        viewModelOf(::SplashViewModel)
        viewModelOf(::LoginViewModel)
        viewModelOf(::RegistrationViewModel)
        viewModelOf(::HomeViewModel)
        viewModelOf(::ChatsViewModel)

        // Диалоги
        viewModelOf(::CreateChatViewModel)
        viewModelOf(::ControlViewModel)
        viewModelOf(::HostSelectViewModel)
        viewModelOf(::AddHostViewModel)
        viewModelOf(::ProfileViewModel)
    }
