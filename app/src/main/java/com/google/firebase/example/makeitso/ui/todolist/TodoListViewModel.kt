package com.google.firebase.example.makeitso.ui.todolist

import com.google.firebase.example.makeitso.MainViewModel
import com.google.firebase.example.makeitso.data.model.TodoItem
import com.google.firebase.example.makeitso.data.repository.TodoItemRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class TodoListViewModel @Inject constructor(
    private val todoItemRepository: TodoItemRepository
) : MainViewModel() {
    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean>
        get() = _isLoading.asStateFlow()

    private val _todoItems = MutableStateFlow<List<TodoItem>>(emptyList())
    val todoItems: StateFlow<List<TodoItem>>
        get() = _todoItems.asStateFlow()

    fun loadItems() {
        launchCatching {
            try {
                _todoItems.value = todoItemRepository.getTodoItems()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateItem(item: TodoItem) {
        _todoItems.value = _todoItems.value.map { if (it.id == item.id) item else it }
        launchCatching {
            todoItemRepository.update(item)
        }
    }
}
