package com.refun.owner.model

data class HistoryItem (
    var id: String,
    var timestamp: String,
    var status: Boolean,
    var points: Int,
//    var bottles: ArrayList<ScannedBottle>
) 