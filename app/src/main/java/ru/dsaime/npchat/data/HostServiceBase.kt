package ru.dsaime.npchat.data

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.transformLatest
import kotlinx.coroutines.launch
import ru.dsaime.npchat.common.functions.runSuspend
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
    init {
        // Инициализация flow с выбранным хостом
        coroutineScope.launch {
            currentSavedHostFlow.emit(preferredHost())
        }
    }

    // Возвращает flow с выбранным хостом
    @OptIn(ExperimentalCoroutinesApi::class)
    override fun currentHostFlow() =
        currentSavedHostFlow
            .map { it?.toModel() }
            .transformLatest { host ->
                // Установить значение, даже если равно null
                emit(host)
                // Проверять доступность хоста и обновлять значение
                if (host != null) {
                    statusFlow(host.url).collect { status ->
                        emit(host.copy(status = status))
                    }
                }
            }.stateIn(
                scope = coroutineScope,
                started = SharingStarted.Eagerly,
                initialValue = currentSavedHostFlow.value?.toModel(),
            )

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun hostsFlow() =
        db
            .hostDao()
            .getAllFlow()
            .map { it.sortedByDescending { it.lastUsedAt } }
            .map { it.map { it.toModel() } }
            .transformLatest { hosts ->
                // Установить значение, даже если хостов нет
                emit(hosts)
                if (hosts.isEmpty()) return@transformLatest
                // Проверять доступность всех хостов и обновлять значение
                tickerFlow(1.seconds).collect {
                    coroutineScope
                        .async {
                            hosts
                                // Асинхронно получить статусы всех хостов
                                .map {
                                    async { it.copy(status = status(it.url)) }
                                }.awaitAll()
                                // Ждать, пока все хосты будут обработаны и обновить значение
                                .runSuspend(::emit)
                        }.await()
                }
            }.stateIn(
                scope = coroutineScope,
                started = SharingStarted.Eagerly,
                initialValue = emptyList(),
            )

    // Изменяет выбранный хост
    override suspend fun changeHost(host: Host) {
        if (host.url.isBlank()) error("Host url cannot be empty")
        // Сохраняем в room
        val savedHost = SavedHost(host, Instant.now().epochSecond)
        db.hostDao().upsert(savedHost)
        // Обновляем текущий хост
        currentSavedHostFlow.emit(savedHost)
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
                    started = SharingStarted.WhileSubscribed(5_000),
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

    // Кэш flow со статусом хоста
    private val statusFlowCache = ConcurrentHashMap<String, StateFlow<Host.Status>>()

    // Выбранный хост
    private val currentSavedHostFlow = MutableStateFlow<SavedHost?>(null)

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
