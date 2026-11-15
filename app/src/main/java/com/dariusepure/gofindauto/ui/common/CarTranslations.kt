package com.dariusepure.gofindauto.ui.common

import android.content.Context
import com.dariusepure.gofindauto.R

object CarTranslations {
    fun getFuelTypeLabel(context: Context, type: String): String = when (type) {
        "Petrol" -> context.getString(R.string.fuel_petrol)
        "Diesel" -> context.getString(R.string.fuel_diesel)
        "Electric" -> context.getString(R.string.fuel_electric)
        "Hybrid" -> context.getString(R.string.fuel_hybrid)
        "LPG" -> context.getString(R.string.fuel_lpg)
        "CNG" -> context.getString(R.string.fuel_cng)
        "Hydrogen" -> context.getString(R.string.fuel_hydrogen)
        else -> type
    }

    fun getColorLabel(context: Context, color: String): String = when (color.lowercase()) {
        "white" -> context.getString(R.string.color_white)
        "black" -> context.getString(R.string.color_black)
        "silver" -> context.getString(R.string.color_silver)
        "gray", "grey" -> context.getString(R.string.color_gray)
        "blue" -> context.getString(R.string.color_blue)
        "red" -> context.getString(R.string.color_red)
        "brown" -> context.getString(R.string.color_brown)
        "green" -> context.getString(R.string.color_green)
        "yellow" -> context.getString(R.string.color_yellow)
        "orange" -> context.getString(R.string.color_orange)
        else -> color
    }
}
