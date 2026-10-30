package uz.mir.tasks.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.io.File

/** Stores all tasks on the device in a single JSON file. Nothing leaves the device. */
class TaskRepository(private val file: File) {

    private val json = Json { ignoreUnknownKeys = true }
    private val writeLock = Mutex()
    private val _tasks = MutableStateFlow(load())
    val tasks: StateFlow<List<Task>> = _tasks.asStateFlow()

    suspend fun upsert(task: Task) = mutate { list ->
        if (list.any { it.id == task.id }) list.map { if (it.id == task.id) task else it } else list + task
    }

    suspend fun delete(id: Long) = mutate { list -> list.filterNot { it.id == id } }

    suspend fun setDone(id: Long, done: Boolean, now: Long) = mutate { list ->
        list.map { if (it.id == id) it.copy(done = done, completedAt = if (done) now else null) else it }
    }

    suspend fun clearCompleted() = mutate { list -> list.filterNot { it.done } }

    fun nextId(): Long = (_tasks.value.maxOfOrNull { it.id } ?: 0) + 1

    private suspend fun mutate(change: (List<Task>) -> List<Task>) {
        _tasks.update(change)
        val snapshot = _tasks.value
        withContext(Dispatchers.IO) {
            writeLock.withLock {
                val tmp = File(file.parentFile, "${file.name}.tmp")
                tmp.writeText(json.encodeToString(snapshot))
                tmp.renameTo(file)
            }
        }
    }

    private fun load(): List<Task> = runCatching {
        if (file.exists()) json.decodeFromString<List<Task>>(file.readText()) else emptyList()
    }.getOrDefault(emptyList())
}
