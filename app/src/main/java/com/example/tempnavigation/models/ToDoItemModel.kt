package com.example.tempnavigation.models

data class ToDoItemModel (
    val id: Int,
    val text: String? = null,
    val isEditable: Boolean
    )