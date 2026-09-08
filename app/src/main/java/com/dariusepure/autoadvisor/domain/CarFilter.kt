package com.dariusepure.autoadvisor.domain

data class CarFilter(
    val selectedBrands: Set<String> = emptySet(),
    val selectedFuelTypes: Set<String> = emptySet(),
    val yearFrom: Int? = null,
    val yearTo: Int? = null
) {
    val isActive: Boolean
        get() = selectedBrands.isNotEmpty() || selectedFuelTypes.isNotEmpty() || yearFrom != null || yearTo != null
}
