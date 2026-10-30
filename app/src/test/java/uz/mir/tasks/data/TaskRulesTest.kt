package uz.mir.tasks.data

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.nio.file.Files
import java.time.LocalDate

class TaskRulesTest {

    private val today = LocalDate.of(2026, 10, 30)
    private val day = today.toEpochDay()

    private val tasks = listOf(
        Task(1, "no date", createdAt = 1),
        Task(2, "overdue", dueEpochDay = day - 2, createdAt = 2),
        Task(3, "today low", dueEpochDay = day, priority = Priority.LOW, createdAt = 3),
        Task(4, "today high", dueEpochDay = day, priority = Priority.HIGH, createdAt = 4),
        Task(5, "tomorrow", dueEpochDay = day + 1, createdAt = 5),
        Task(6, "done old", done = true, completedAt = 10),
        Task(7, "done new", done = true, completedAt = 20),
    )

    private fun ids(filter: TaskFilter) = TaskRules.apply(tasks, filter, today).map { it.id }

    @Test
    fun allShowsOpenTasksByDateThenPriority() = assertEquals(listOf(2L, 4, 3, 5, 1), ids(TaskFilter.ALL))

    @Test
    fun todayIncludesOverdue() = assertEquals(listOf(2L, 4, 3), ids(TaskFilter.TODAY))

    @Test
    fun upcomingIsFutureOnly() = assertEquals(listOf(5L), ids(TaskFilter.UPCOMING))

    @Test
    fun doneIsNewestFirst() = assertEquals(listOf(7L, 6), ids(TaskFilter.DONE))

    @Test
    fun overdueDetection() {
        assertTrue(TaskRules.isOverdue(tasks[1], today))
        assertFalse(TaskRules.isOverdue(tasks[2], today))
        assertFalse(TaskRules.isOverdue(tasks[0], today))
    }

    @Test
    fun repositoryPersistsAcrossInstances() = runBlocking {
        val file = File(Files.createTempDirectory("tasks").toFile(), "tasks.json")
        val repo = TaskRepository(file)
        repo.upsert(Task(repo.nextId(), "Buy milk", priority = Priority.HIGH))
        repo.upsert(Task(repo.nextId(), "Call mom"))
        repo.setDone(1, true, now = 99)
        repo.delete(2)

        val reloaded = TaskRepository(file).tasks.value
        assertEquals(1, reloaded.size)
        assertTrue(reloaded[0].done)
        assertEquals(99L, reloaded[0].completedAt)
    }
}
