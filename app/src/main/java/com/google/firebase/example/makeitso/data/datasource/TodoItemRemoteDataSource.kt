package com.google.firebase.example.makeitso.data.datasource

import com.google.firebase.example.makeitso.data.model.TodoItem
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import javax.inject.Inject

// Rows are filtered by RLS on the server (owner_id = auth.uid()), so no owner filter is needed here
class TodoItemRemoteDataSource @Inject constructor(
    private val postgrest: Postgrest
) {
    suspend fun getTodoItems(): List<TodoItem> {
        return postgrest.from(TODO_ITEMS_TABLE)
            .select { order(CREATED_AT_COLUMN, Order.DESCENDING) }
            .decodeList()
    }

    suspend fun getTodoItem(itemId: String): TodoItem? {
        return postgrest.from(TODO_ITEMS_TABLE)
            .select { filter { eq(ID_COLUMN, itemId) } }
            .decodeSingleOrNull()
    }

    // id and owner_id are filled in by the database (gen_random_uuid() / auth.uid())
    suspend fun create(todoItem: TodoItem): String {
        return postgrest.from(TODO_ITEMS_TABLE)
            .insert(todoItem.toJson()) { select() }
            .decodeSingle<TodoItem>()
            .id
    }

    suspend fun update(todoItem: TodoItem) {
        postgrest.from(TODO_ITEMS_TABLE).update(todoItem.toJson()) {
            filter { eq(ID_COLUMN, todoItem.id) }
        }
    }

    suspend fun delete(itemId: String) {
        postgrest.from(TODO_ITEMS_TABLE).delete {
            filter { eq(ID_COLUMN, itemId) }
        }
    }

    private fun TodoItem.toJson() = buildJsonObject {
        put("title", title)
        put("priority", priority)
        put("completed", completed)
        put("flagged", flagged)
    }

    companion object {
        private const val TODO_ITEMS_TABLE = "todo_items"
        private const val ID_COLUMN = "id"
        private const val CREATED_AT_COLUMN = "created_at"
    }
}
