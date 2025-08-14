package ru.dsaime.npchat.di

import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.core.scope.Scope
import org.koin.dsl.module
import retrofit2.Retrofit
import ru.dsaime.npchat.data.NPChatApi
import ru.dsaime.npchat.data.NPChatLocalPrefs
import ru.dsaime.npchat.data.NPChatRepository
import ru.dsaime.npchat.network.retrofit
import ru.dsaime.npchat.screens.login.LoginViewModel
import ru.dsaime.npchat.screens.login.NPChatClient
import ru.dsaime.npchat.screens.splash.SplashViewModel


val appModule = module {
    // Preferences
    singleOf(::NPChatLocalPrefs)

    // Api
    single { retrofit(get()) }
    fun <T> Scope.retroApi(service: Class<T>) = get<Retrofit>().create(service)
    single { retroApi(NPChatApi::class.java) }

    // Repositories
    singleOf(::NPChatRepository)

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