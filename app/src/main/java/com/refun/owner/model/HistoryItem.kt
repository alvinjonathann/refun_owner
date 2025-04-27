package com.refun.owner.model

data class HistoryItem(
    val id: String,
    val timestamp: String,
    val status: Boolean,
    val points: Int
) 