package com.refun.owner.data

import com.refun.owner.model.Bottle

object BottleDatabase {
    private val bottles = mapOf(
        "081287622285" to Bottle("081287622285", "AQUA", 600, 20),
        "081287622286" to Bottle("081287622286", "AMIDIS", 350, 10),
        "081287622287" to Bottle("081287622287", "MIZONE", 500, 20)
    )

    fun findByBarcode(barcode: String): Bottle? {
        return bottles[barcode]
    }

    fun getAllBottles(): List<Bottle> {
        return bottles.values.toList()
    }
} 