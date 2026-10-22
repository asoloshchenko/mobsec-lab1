package com.google.firebase.example.makeitso.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TodoItem(
    val id: String = "",
    val title: String = "",
    val priority: Int = Priority.NONE.value,
    val completed: Boolean = false,
    val flagged: Boolean = false,
    @SerialName("owner_id") val ownerId: String = ""
)

val TodoItem.isHighPriority: Boolean
    get() = this.priority == Priority.HIGH.value

val TodoItem.isMediumPriority: Boolean
    get() = this.priority == Priority.MEDIUM.value

val TodoItem.isLowPriority: Boolean
    get() = this.priority == Priority.LOW.value

val TodoItem.isNonePriority: Boolean
    get() = this.priority == Priority.NONE.value

