package com.dariusepure.autoadvisor.ui.cars

val europeanBrands = listOf(
    "Volkswagen", "BMW", "Mercedes-Benz", "Audi", "Porsche", "Opel", // Germany
    "Renault", "Peugeot", "Citroën", "Dacia", "DS", "Alpine", // France/Romania
    "Fiat", "Alfa Romeo", "Lancia", "Maserati", "Ferrari", "Lamborghini", "Abarth", // Italy
    "Volvo", "Polestar", // Sweden
    "Skoda", // Czech Republic
    "Seat", "Cupra", // Spain
    "Land Rover", "Jaguar", "Mini", "Aston Martin", "Bentley", "Rolls-Royce", "Lotus", "McLaren", // UK
    "Saab" // Legacy
).sorted()

val globalBrands = listOf(
    "Toyota", "Honda", "Nissan", "Mazda", "Subaru", "Mitsubishi", "Suzuki", "Lexus", "Infiniti", // Japan
    "Hyundai", "Kia", "Genesis", // South Korea
    "Ford", "Chevrolet", "Cadillac", "Jeep", "Dodge", "Tesla", "Ram", // USA
    "BYD", "MG", "Geely" // China/Global
).sorted()

val allCarBrands = (europeanBrands + globalBrands).sorted()
