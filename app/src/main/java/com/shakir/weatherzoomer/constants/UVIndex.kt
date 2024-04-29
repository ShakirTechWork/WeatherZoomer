package com.shakir.weatherzoomer.constants

sealed class UVIndex(val description: String) {
    object Low : UVIndex("low")
    object Moderate : UVIndex("moderate")
    object High : UVIndex("high")

    companion object {
        fun getUVValue(index: Int): UVIndex {
            return when (index) {
                1, 2 -> Low
                3, 4, 5 -> Moderate
                6, 7 -> High
                else -> throw IllegalArgumentException("Invalid API response index: $index")
            }
        }
    }
}

