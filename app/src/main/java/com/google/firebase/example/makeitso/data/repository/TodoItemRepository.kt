package com.google.firebase.example.makeitso.data.repository

import com.google.firebase.example.makeitso.data.datasource.TodoItemRemoteDataSource
import com.google.firebase.example.makeitso.data.model.TodoItem
import javax.inject.Inject

class TodoItemRepository @Inject constructor(
    private val todoItemRemoteDataSource: TodoItemRemoteDataSource
) {
    suspend fun getTodoItems(): List<TodoItem> {
        return todoItemRemoteDataSource.getTodoItems()
    }

    suspend fun getTodoItem(itemId: String): TodoItem? {
        return todoItemRemoteDataSource.getTodoItem(itemId)
    }

    suspend fun create(todoItem: TodoItem): String {
        return todoItemRemoteDataSource.create(todoItem)
    }

    suspend fun update(todoItem: TodoItem) {
        todoItemRemoteDataSource.update(todoItem)
    }

    suspend fun delete(itemId: String) {
        todoItemRemoteDataSource.delete(itemId)
    }
}
