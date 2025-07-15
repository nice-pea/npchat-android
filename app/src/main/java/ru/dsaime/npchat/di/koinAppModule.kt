package ru.dsaime.npchat.di

import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.core.scope.Scope
import org.koin.dsl.module
import retrofit2.Retrofit
import ru.dsaime.npchat.data.api.AuthenticationApi
import ru.dsaime.npchat.data.api.ChatsApi
import ru.dsaime.npchat.data.api.NpcClientApi
import ru.dsaime.npchat.data.repositories.AuthenticationRepository
import ru.dsaime.npchat.data.repositories.ChatsRepository
import ru.dsaime.npchat.data.repositories.NpcClient
import ru.dsaime.npchat.data.store.AuthenticationStore
import ru.dsaime.npchat.data.store.NpcClientStore
import ru.dsaime.npchat.network.retrofit
import ru.dsaime.npchat.screens.app.authentication.AuthenticationViewModel
import ru.dsaime.npchat.screens.chats.ChatsViewModel
import ru.dsaime.npchat.screens.login.LoginViewModel


val appModule = module {
    // LocalStore
    singleOf(::AuthenticationStore)
    singleOf(::NpcClientStore)

    // Api
    single { retrofit(get(), get()) }
    fun <T> Scope.retroApi(service: Class<T>) = get<Retrofit>().create(service)
    single { retroApi(AuthenticationApi::class.java) }
    single { retroApi(NpcClientApi::class.java) }
    single { retroApi(ChatsApi::class.java) }

    // Repositories
    singleOf(::AuthenticationRepository)
    singleOf(::NpcClient)
    singleOf(::ChatsRepository)

    // ViewModels
    viewModelOf(::AuthenticationViewModel)
    viewModelOf(::LoginViewModel)
    viewModelOf(::ChatsViewModel)
}