package ru.dsaime.npchat.data

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import ru.dsaime.npchat.common.functions.tickerFlow
import ru.dsaime.npchat.data.room.AppDatabase
import ru.dsaime.npchat.data.room.SavedHost
import ru.dsaime.npchat.model.Host
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap
import kotlin.time.Duration.Companion.seconds

class HostServiceBase(
    private val api: NPChatApi,
    private val db: AppDatabase,
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob()),
) : HostService {
    // Кэш flow со статусом хоста
    private val statusFlowCache = ConcurrentHashMap<String, StateFlow<Host.Status>>()

    // Выбранный хост (mutable)
    private val currentSavedHostMutFlow = MutableStateFlow<SavedHost?>(null)

    // Выбранный хост
    @OptIn(ExperimentalCoroutinesApi::class)
    private val currentSavedHostFlow =
        currentSavedHostMutFlow
            .map { it?.toModel() }
            .flatMapLatest { host ->
                if (host == null) {
                    // Установить значение, даже если равно null
                    flowOf(null)
                } else {
                    // Проверять доступность хоста и обновлять значение
                    statusFlow(host.url)
                        .map { host.copy(status = it) }
                }
            }.stateIn(
                scope = coroutineScope,
                started = SharingStarted.WhileSubscribed(2_000),
                initialValue = currentSavedHostMutFlow.value?.toModel(),
            )

    @OptIn(ExperimentalCoroutinesApi::class)
    private val hostsFlow =
        db
            .hostDao()
            .getAllFlow()
            .map { it.sortedByDescending { it.lastUsedAt } }
            .map { it.map { it.toModel() } }
            .flatMapLatest { hosts ->
                // Установить значение, даже если хостов нет
                if (hosts.isEmpty()) {
                    return@flatMapLatest flowOf(emptyList())
                }
                // written with qwen
                // Проверять доступность всех хостов и обновлять значение
                tickerFlow(1.seconds).flatMapLatest {
                    val statusFlows =
                        hosts.map { host ->
                            statusFlow(host.url)
                                .map { status -> host.copy(status = status) }
                        }
                    // Объединяем все результаты в один список
                    combine(statusFlows) { results -> results.toList() }
                }
            }.stateIn(
                scope = coroutineScope,
                started = SharingStarted.WhileSubscribed(2_000),
                initialValue = emptyList(),
            )

    init {
        // Инициализация flow с выбранным хостом
        coroutineScope.launch {
            currentSavedHostMutFlow.emit(preferredHost())
        }
    }

    // Возвращает flow с выбранным хостом
    override fun currentHostFlow() = currentSavedHostFlow

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun hostsFlow() = hostsFlow

    // Изменяет выбранный хост
    override suspend fun changeHost(host: Host) {
        if (host.url.isBlank()) error("Host url cannot be empty")
        // Сохраняем в room
        val savedHost = SavedHost(host, Instant.now().epochSecond)
        db.hostDao().upsert(savedHost)
        // Обновляем текущий хост
        currentSavedHostMutFlow.emit(savedHost)
    }

    // Удаляет хост
    override suspend fun deleteHostByUrl(url: String) {
        db.hostDao().delete(url)
    }

    // Проверяет доступность хоста
    override suspend fun status(baseUrl: String) = api.ping(baseUrl).toHostStatus()

    // Written with gigacode
    // Возвращает flow со статусом хоста
    override fun statusFlow(baseUrl: String): StateFlow<Host.Status> =
        statusFlowCache.getOrPut(baseUrl) {
            tickerFlow(1.seconds)
                .map { api.ping(baseUrl).toHostStatus() }
                .stateIn(
                    scope = coroutineScope,
                    started = SharingStarted.WhileSubscribed(2_000),
                    initialValue = Host.Status.UNKNOWN,
                )
        }

    // Добавляет хост в сохраненные
    override suspend fun add(host: Host) =
        db.hostDao().upsert(
            SavedHost(
                baseUrl = host.url,
                lastUsedAt = 0,
                status = Host.Status.UNKNOWN.name,
            ),
        )

    // Возвращает сохраненные хосты
    private suspend fun savedHosts(): List<SavedHost> =
        db
            .hostDao()
            .getAll()
            .sortedByDescending { it.lastUsedAt }

    // Возвращает хост по специальному алгоритму
    private suspend fun preferredHost(): SavedHost? =
        with(Dispatchers.IO) {
            // Получаем сохраненные хосты, если их нет, то возвращаем null
            val hosts = savedHosts().ifEmpty { return null }
            // Выбираем последний использованный
            val currentHost = hosts.maxByOrNull { it.lastUsedAt }
            // Если текущий хост не задан, то возвращаем первый из списка
            return currentHost ?: hosts.first()
        }

    // Возвращает baseUrl выбранного хоста
    override suspend fun currentBaseUrl() = preferredHost()?.baseUrl

    // Преобразует Result в Host.Status
    private fun Result<Unit>.toHostStatus() = if (isSuccess) Host.Status.ONLINE else Host.Status.OFFLINE
}
