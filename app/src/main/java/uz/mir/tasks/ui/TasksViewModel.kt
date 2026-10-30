package uz.mir.tasks.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import uz.mir.tasks.data.Priority
import uz.mir.tasks.data.Task
import uz.mir.tasks.data.TaskFilter
import uz.mir.tasks.data.TaskRepository
import uz.mir.tasks.data.TaskRules
import java.io.File
import java.time.LocalDate

data class TasksUiState(
    val filter: TaskFilter = TaskFilter.ALL,
    val visible: List<Task> = emptyList(),
    val counts: Map<TaskFilter, Int> = emptyMap(),
    val today: LocalDate = LocalDate.now(),
)

class TasksViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = TaskRepository(File(app.filesDir, "tasks.json"))
    private val filter = MutableStateFlow(TaskFilter.ALL)

    val state: StateFlow<TasksUiState> = combine(repo.tasks, filter) { tasks, f ->
        val today = LocalDate.now()
        TasksUiState(f, TaskRules.apply(tasks, f, today), TaskRules.counts(tasks, today), today)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, TasksUiState())

    fun setFilter(f: TaskFilter) {
        filter.value = f
    }

    fun save(existing: Task?, title: String, note: String, due: LocalDate?, priority: Priority) {
        val trimmed = title.trim()
        if (trimmed.isEmpty()) return
        viewModelScope.launch {
            val base = existing ?: Task(id = repo.nextId(), title = trimmed, createdAt = System.currentTimeMillis())
            repo.upsert(
                base.copy(title = trimmed, note = note.trim(), dueEpochDay = due?.toEpochDay(), priority = priority),
            )
        }
    }

    fun toggleDone(task: Task) = viewModelScope.launch {
        repo.setDone(task.id, !task.done, System.currentTimeMillis())
    }

    fun delete(task: Task) = viewModelScope.launch { repo.delete(task.id) }

    fun restore(task: Task) = viewModelScope.launch { repo.upsert(task) }

    fun clearCompleted() = viewModelScope.launch { repo.clearCompleted() }
}
