package com.dariusepure.gofindauto.ui.common

import android.content.Context
import com.dariusepure.gofindauto.R

object CarTranslations {
    fun getFuelTypeLabel(context: Context, type: String): String = when (type.lowercase().trim()) {
        "petrol", "benzina", "benzină" -> context.getString(R.string.fuel_petrol)
        "diesel", "motorina", "motorină" -> context.getString(R.string.fuel_diesel)
        "electric" -> context.getString(R.string.fuel_electric)
        "hybrid", "hibrid" -> context.getString(R.string.fuel_hybrid)
        "lpg", "gpl" -> context.getString(R.string.fuel_lpg)
        "cng", "gnc" -> context.getString(R.string.fuel_cng)
        "hydrogen", "hidrogen" -> context.getString(R.string.fuel_hydrogen)
        else -> type
    }

    fun getColorLabel(context: Context, color: String): String = when (color.lowercase().trim()) {
        "white", "alb" -> context.getString(R.string.color_white)
        "black", "negru" -> context.getString(R.string.color_black)
        "silver", "argintiu" -> context.getString(R.string.color_silver)
        "gray", "grey", "gri" -> context.getString(R.string.color_gray)
        "blue", "albastru" -> context.getString(R.string.color_blue)
        "red", "rosu", "roșu" -> context.getString(R.string.color_red)
        "brown", "maro" -> context.getString(R.string.color_brown)
        "green", "verde" -> context.getString(R.string.color_green)
        "yellow", "galben" -> context.getString(R.string.color_yellow)
        "orange", "portocaliu" -> context.getString(R.string.color_orange)
        else -> color
    }

    fun getVehicleTypeLabel(context: Context, type: String): String = when (type.lowercase().trim()) {
        "sedan", "limuzina", "limuzină" -> "Sedan"
        "hatchback" -> "Hatchback"
        "suv" -> "SUV"
        "wagon", "estate", "combi", "break" -> "Combi / Wagon"
        "coupe", "cupé" -> "Coupe"
        "convertible", "cabriolet", "cabrio" -> "Cabrio"
        else -> type
    }
}
