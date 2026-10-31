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

val carBrands = listOf(
    "Abarth", "Acura", "Alfa Romeo", "Alpine", "Aston Martin", "Audi", "Bentley", "Bmw", "Bugatti", "Buick",
    "Byd", "Cadillac", "Caterham", "Chevrolet", "Chrysler", "Citroën", "Cupra", "Dacia", "Daewoo", "Daihatsu",
    "Dodge", "Ds", "Ferrari", "Fiat", "Ford", "Genesis", "Gmc", "Honda", "Hummer", "Hyundai", "Infiniti",
    "Isuzu", "Jaguar", "Jeep", "Kia", "Koenigsegg", "Lada", "Lamborghini", "Lancia", "Land Rover", "Lexus", 
    "Lincoln", "Lotus", "Lucid", "Maserati", "Maybach", "Mazda", "Mclaren", "Mercedes-Benz", "Mg", "Mini", 
    "Mitsubishi", "Morgan", "Moskvitch", "Nissan", "Opel", "Pagani", "Peugeot", "Polestar", "Porsche", "Ram", 
    "Renault", "Rimac", "Rolls-Royce", "Rover", "Saab", "Seat", "Skoda", "Smart", "Ssangyong", "Subaru", 
    "Suzuki", "Tesla", "Toyota", "Trabant", "Triumph", "Tvr", "Vauxhall", "Volkswagen", "Volvo", "Wartburg"
).sorted() + "Other"

val tireBrands = listOf(
    "Michelin", "Continental", "Bridgestone", "Goodyear", "Pirelli", "Dunlop", "Hankook", "Yokohama",
    "Nokian", "Kumho", "Toyo", "Vredestein", "Falken", "Bfgoodrich", "Cooper", "General Tire", "Uniroyal",
    "Kleber", "Fulda", "Debica", "Sava", "Tigar", "Matador", "Viking", "Barum", "Laufenn", "Firestone",
    "Nexen", "Apollo", "Gt Radial"
).sorted() + "Other"
