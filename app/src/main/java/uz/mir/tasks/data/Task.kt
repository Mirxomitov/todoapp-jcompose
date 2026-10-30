package uz.mir.tasks.data

import kotlinx.serialization.Serializable
import java.time.LocalDate

@Serializable
enum class Priority { NONE, LOW, MEDIUM, HIGH }

@Serializable
data class Task(
    val id: Long,
    val title: String,
    val note: String = "",
    /** Epoch day of the due date, or null when the task has no date. */
    val dueEpochDay: Long? = null,
    val priority: Priority = Priority.NONE,
    val done: Boolean = false,
    val createdAt: Long = 0,
    val completedAt: Long? = null,
) {
    val dueDate: LocalDate? get() = dueEpochDay?.let(LocalDate::ofEpochDay)
}

enum class TaskFilter { ALL, TODAY, UPCOMING, DONE }

/** Filtering and ordering rules, kept free of Android types so they are unit-testable. */
object TaskRules {

    fun apply(tasks: List<Task>, filter: TaskFilter, today: LocalDate): List<Task> {
        val todayDay = today.toEpochDay()
        val filtered = when (filter) {
            TaskFilter.ALL -> tasks.filter { !it.done }
            TaskFilter.TODAY -> tasks.filter { !it.done && it.dueEpochDay != null && it.dueEpochDay <= todayDay }
            TaskFilter.UPCOMING -> tasks.filter { !it.done && it.dueEpochDay != null && it.dueEpochDay > todayDay }
            TaskFilter.DONE -> return tasks.filter { it.done }.sortedByDescending { it.completedAt ?: 0 }
        }
        return filtered.sortedWith(
            compareBy<Task> { it.dueEpochDay ?: Long.MAX_VALUE }
                .thenByDescending { it.priority.ordinal }
                .thenBy { it.createdAt },
        )
    }

    fun isOverdue(task: Task, today: LocalDate): Boolean =
        !task.done && task.dueEpochDay != null && task.dueEpochDay < today.toEpochDay()

    fun counts(tasks: List<Task>, today: LocalDate): Map<TaskFilter, Int> =
        TaskFilter.entries.associateWith { apply(tasks, it, today).size }
}
