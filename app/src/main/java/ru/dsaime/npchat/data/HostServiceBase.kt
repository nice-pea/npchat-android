package ru.dsaime.npchat.data

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import ru.dsaime.npchat.data.room.AppDatabase
import ru.dsaime.npchat.data.room.Host

class HostServiceBase(
    private val api: NPChatApi,
    private val db: AppDatabase,
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob()),
) : HostService {
    init {
        // Инициализация flow с выбранным хостом
        coroutineScope.launch {
            currentHostFlow.emit(preferredHost())
        }
    }

    // Возвращает flow с baseUrl выбранного хоста
    override fun currentBaseUrlFlow() =
        currentHostFlow
            .map { it?.baseUrl }
            .stateIn(
                scope = coroutineScope,
                started = SharingStarted.Eagerly,
                initialValue = currentHostFlow.value?.baseUrl,
            )

    // Изменяет выбранный хост
    override suspend fun changeBaseUrl(baseUrl: String) {
        if (baseUrl.isBlank()) error("Host cannot be empty")
        val host = Host(baseUrl)
        // Сохраняем в room
        db.hostDao().upsert(host)
        // Обновляем текущий хост
        currentHostFlow.emit(host)
    }

    override suspend fun deleteBaseUrl(baseUrl: String) {
        db.hostDao().delete(Host(baseUrl))
    }

    // Возвращает список сохраненных baseUrls
    override suspend fun savedBaseUrls() = savedHosts().map { it.baseUrl }

    // Проверяет доступность хоста
    override suspend fun ping(baseUrl: String) = api.ping(baseUrl).isSuccess

    // Выбранный хост
    private val currentHostFlow = MutableStateFlow<Host?>(null)

    // Возвращает сохраненные хосты
    private suspend fun savedHosts(): List<Host> = db.hostDao().getAll().sortedByDescending { it.lastUsed }

    // Возвращает хост по специальному алгоритму
    private suspend fun preferredHost(): Host? =
        with(Dispatchers.IO) {
            // Получаем сохраненные хосты, если их нет, то возвращаем null
            val hosts = savedHosts().ifEmpty { return null }
            // Выбираем последний использованный
            val currentHost = hosts.maxByOrNull { it.lastUsed }
            // Если текущий хост не задан, то возвращаем первый из списка
            return currentHost ?: hosts.first()
        }

    // Возвращает baseUrl выбранного хоста
    override suspend fun currentBaseUrl() = preferredHost()?.baseUrl
}
