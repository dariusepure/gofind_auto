package com.dariusepure.autoadvisor.ui.common

import android.content.Context
import com.dariusepure.autoadvisor.R
import com.dariusepure.autoadvisor.domain.Car
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

object CarFormatters {
    private val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

    fun formatPower(context: Context, car: Car): String {
        val hpValue: Int
        val kwValue: Int
        
        if (car.powerUnit.lowercase() == "kw") {
            kwValue = car.power
            hpValue = (car.power * 1.35962).roundToInt()
        } else {
            hpValue = car.power
            kwValue = (car.power / 1.35962).roundToInt()
        }
        
        return context.getString(R.string.formatter_power_dual, hpValue, kwValue)
    }

    fun getCarSummary(context: Context, car: Car): String {
        val details = mutableListOf<String>()
        if (car.year != 0) details.add(car.year.toString())
        if (car.fuelType.isNotBlank()) details.add(CarTranslations.getFuelTypeLabel(context, car.fuelType))
        if (car.power != 0) {
            val hp = if (car.powerUnit.lowercase() == "hp") car.power else (car.power * 1.35962).roundToInt()
            details.add(context.getString(R.string.formatter_power_hp, hp))
        }
        if (car.engineSize.isNotBlank()) details.add(context.getString(R.string.formatter_engine_size, car.engineSize))
        
        return details.joinToString(" \u00B7 ")
    }

    fun formatDate(date: Date): String = dateFormat.format(date)

    fun formatTime(timestamp: Long): String = timeFormat.format(Date(timestamp))

    fun getBrandLogoResource(make: String): Int? {
        if (make.isBlank()) return null
        
        val normalizedMake = make.lowercase()
            .replace(" ", "-")
            .replace("\u00EB", "e") // ë -> e (Citroën)
        
        return when (normalizedMake) {
            "abarth" -> R.drawable.abarth
            "acura" -> R.drawable.acura
            "alfa-romeo" -> R.drawable.alfa_romeo
            "alpine" -> R.drawable.alpine
            "aston-martin" -> R.drawable.aston_martin
            "audi" -> R.drawable.audi
            "bentley" -> R.drawable.bentley
            "bmw" -> R.drawable.bmw
            "bugatti" -> R.drawable.bugatti
            "buick" -> R.drawable.buick
            "byd" -> R.drawable.byd
            "cadillac" -> R.drawable.cadillac
            "caterham" -> R.drawable.caterham
            "chevrolet" -> R.drawable.chevrolet
            "chrysler" -> R.drawable.chrysler
            "citroen" -> R.drawable.citroen
            "cupra" -> R.drawable.cupra
            "dacia" -> R.drawable.dacia
            "daewoo" -> R.drawable.daewoo
            "daihatsu" -> R.drawable.daihatsu
            "dodge" -> R.drawable.dodge
            "ds" -> R.drawable.ds
            "ferrari" -> R.drawable.ferrari
            "fiat" -> R.drawable.fiat
            "ford" -> R.drawable.ford
            "genesis" -> R.drawable.genesis
            "gmc" -> R.drawable.gmc
            "honda" -> R.drawable.honda
            "hummer" -> R.drawable.hummer
            "hyundai" -> R.drawable.hyundai
            "infiniti" -> R.drawable.infiniti
            "isuzu" -> R.drawable.isuzu
            "jaguar" -> R.drawable.jaguar
            "jeep" -> R.drawable.jeep
            "kia" -> R.drawable.kia
            "koenigsegg" -> R.drawable.koenigsegg
            "lada" -> R.drawable.lada
            "lamborghini" -> R.drawable.lamborghini
            "lancia" -> R.drawable.lancia
            "land-rover" -> R.drawable.land_rover
            "lexus" -> R.drawable.lexus
            "lincoln" -> R.drawable.lincoln
            "lotus" -> R.drawable.lotus
            "lucid" -> R.drawable.lucid
            "maserati" -> R.drawable.maserati
            "maybach" -> R.drawable.maybach
            "mazda" -> R.drawable.mazda
            "mclaren" -> R.drawable.mclaren
            "mercedes-benz" -> R.drawable.mercedes_benz
            "mg" -> R.drawable.mg
            "mini" -> R.drawable.mini
            "mitsubishi" -> R.drawable.mitsubishi
            "morgan" -> R.drawable.morgan
            "nissan" -> R.drawable.nissan
            "opel" -> R.drawable.opel
            "pagani" -> R.drawable.pagani
            "peugeot" -> R.drawable.peugeot
            "polestar" -> R.drawable.polestar
            "porsche" -> R.drawable.porsche
            "ram" -> R.drawable.ram
            "renault" -> R.drawable.renault
            "rimac" -> R.drawable.rimac
            "rolls-royce" -> R.drawable.rolls_royce
            "rover" -> R.drawable.rover
            "saab" -> R.drawable.saab
            "seat" -> R.drawable.seat
            "skoda" -> R.drawable.skoda
            "smart" -> R.drawable.smart
            "ssangyong" -> R.drawable.ssangyong
            "subaru" -> R.drawable.subaru
            "suzuki" -> R.drawable.suzuki
            "tesla" -> R.drawable.tesla
            "toyota" -> R.drawable.toyota
            "triumph" -> R.drawable.triumph
            "tvr" -> R.drawable.tvr
            "vauxhall" -> R.drawable.vauxhall
            "volkswagen" -> R.drawable.volkswagen
            "volvo" -> R.drawable.volvo
            "wartburg" -> R.drawable.wartburg
            else -> null
        }
    }
}
