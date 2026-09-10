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

val brandModels = mapOf(
    "Volkswagen" to listOf("Golf", "Passat", "Tiguan", "Polo", "Touareg", "Arteon", "T-Roc", "ID.3", "ID.4"),
    "BMW" to listOf("1 Series", "3 Series", "5 Series", "7 Series", "X1", "X3", "X5", "X6", "M3", "M5"),
    "Mercedes-Benz" to listOf("A-Class", "C-Class", "E-Class", "S-Class", "GLA", "GLC", "GLE", "GLS", "EQE", "EQS"),
    "Audi" to listOf("A1", "A3", "A4", "A6", "A8", "Q3", "Q5", "Q7", "Q8", "e-tron"),
    "Porsche" to listOf("911", "Cayenne", "Macan", "Panamera", "Taycan", "718 Boxster"),
    "Opel" to listOf("Corsa", "Astra", "Insignia", "Mokka", "Grandland"),
    "Renault" to listOf("Clio", "Megane", "Captur", "Kadjar", "Zoe", "Austral"),
    "Peugeot" to listOf("208", "308", "508", "2008", "3008", "5008"),
    "Citroën" to listOf("C3", "C4", "C5 X", "C5 Aircross"),
    "Dacia" to listOf("Logan", "Sandero", "Duster", "Jogger", "Spring"),
    "Volvo" to listOf("XC40", "XC60", "XC90", "V60", "V90", "S60", "S90"),
    "Skoda" to listOf("Fabia", "Octavia", "Superb", "Kamiq", "Karoq", "Kodiaq"),
    "Seat" to listOf("Ibiza", "Leon", "Arona", "Ateca", "Tarraco"),
    "Cupra" to listOf("Formentor", "Leon", "Ateca", "Born"),
    "Toyota" to listOf("Corolla", "Yaris", "RAV4", "Camry", "C-HR", "Land Cruiser", "Hilux", "Supra"),
    "Honda" to listOf("Civic", "CR-V", "HR-V", "Jazz", "Accord"),
    "Nissan" to listOf("Qashqai", "Juke", "X-Trail", "Leaf", "Ariya"),
    "Mazda" to listOf("Mazda2", "Mazda3", "Mazda6", "CX-30", "CX-5", "CX-60"),
    "Hyundai" to listOf("i10", "i20", "i30", "Tucson", "Santa Fe", "Ioniq 5", "Ioniq 6"),
    "Kia" to listOf("Ceed", "Sportage", "Sorento", "Niro", "EV6", "Stinger"),
    "Ford" to listOf("Fiesta", "Focus", "Mondeo", "Kuga", "Puma", "Mustang", "Ranger"),
    "Chevrolet" to listOf("Camaro", "Corvette", "Cruze", "Equinox"),
    "Tesla" to listOf("Model 3", "Model S", "Model X", "Model Y")
)
