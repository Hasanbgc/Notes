package com.example.tempnavigation.utilities

fun <T> MutableList<T>.addToSecondLastPos(data: T) {

    val lastItem = this.removeAt(this.size - 1)

    if (this.isNotEmpty()) {
        this.add(this.size, data)
        this.add(this.size, lastItem)
    } else {
        this.add(0, data)
        this.add(1, lastItem)
    }
}