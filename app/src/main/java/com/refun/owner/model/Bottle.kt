package com.refun.owner.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Bottle(
    val barcode: String,
    val brand: String,
    val volumeMl: Int,
    val points: Int
) : Parcelable

@Parcelize
data class ScannedBottle(
    val bottle: Bottle,
    val timestamp: String
) : Parcelable

data class CartSummary(
    val bottles: List<ScannedBottle>,
    val totalPoints: Int
) 