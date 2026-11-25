package com.example.util

data class City(
    val name: String,
    val country: String,
    val latitude: Double,
    val longitude: Double,
    val timezoneId: String
) {
    val displayName: String get() = "$name, $country"
}

/** Built-in list of cities so any of them can be chosen without internet. */
object CityDatabase {

    val cities: List<City> = listOf(
        // Pakistan
        City("Karachi", "Pakistan", 24.8607, 67.0011, "Asia/Karachi"),
        City("Lahore", "Pakistan", 31.5204, 74.3587, "Asia/Karachi"),
        City("Islamabad", "Pakistan", 33.6844, 73.0479, "Asia/Karachi"),
        City("Peshawar", "Pakistan", 34.0151, 71.5249, "Asia/Karachi"),
        City("Quetta", "Pakistan", 30.1798, 66.9750, "Asia/Karachi"),
        City("Faisalabad", "Pakistan", 31.4504, 73.1350, "Asia/Karachi"),
        City("Multan", "Pakistan", 30.1575, 71.5249, "Asia/Karachi"),
        City("Hyderabad", "Pakistan", 25.3960, 68.3578, "Asia/Karachi"),
        City("Sialkot", "Pakistan", 32.4945, 74.5229, "Asia/Karachi"),
        City("Gujranwala", "Pakistan", 32.1877, 74.1945, "Asia/Karachi"),
        // South Asia
        City("Delhi", "India", 28.6139, 77.2090, "Asia/Kolkata"),
        City("Mumbai", "India", 19.0760, 72.8777, "Asia/Kolkata"),
        City("Hyderabad", "India", 17.3850, 78.4867, "Asia/Kolkata"),
        City("Lucknow", "India", 26.8467, 80.9462, "Asia/Kolkata"),
        City("Kolkata", "India", 22.5726, 88.3639, "Asia/Kolkata"),
        City("Bengaluru", "India", 12.9716, 77.5946, "Asia/Kolkata"),
        City("Chennai", "India", 13.0827, 80.2707, "Asia/Kolkata"),
        City("Dhaka", "Bangladesh", 23.8103, 90.4125, "Asia/Dhaka"),
        City("Kabul", "Afghanistan", 34.5553, 69.2075, "Asia/Kabul"),
        // Middle East
        City("Makkah", "Saudi Arabia", 21.3891, 39.8579, "Asia/Riyadh"),
        City("Madinah", "Saudi Arabia", 24.5247, 39.5692, "Asia/Riyadh"),
        City("Riyadh", "Saudi Arabia", 24.7136, 46.6753, "Asia/Riyadh"),
        City("Jeddah", "Saudi Arabia", 21.4858, 39.1925, "Asia/Riyadh"),
        City("Dubai", "UAE", 25.2048, 55.2708, "Asia/Dubai"),
        City("Abu Dhabi", "UAE", 24.4539, 54.3773, "Asia/Dubai"),
        City("Doha", "Qatar", 25.2854, 51.5310, "Asia/Qatar"),
        City("Kuwait City", "Kuwait", 29.3759, 47.9774, "Asia/Kuwait"),
        City("Muscat", "Oman", 23.5880, 58.3829, "Asia/Muscat"),
        City("Manama", "Bahrain", 26.2285, 50.5860, "Asia/Bahrain"),
        City("Tehran", "Iran", 35.6892, 51.3890, "Asia/Tehran"),
        City("Baghdad", "Iraq", 33.3152, 44.3661, "Asia/Baghdad"),
        City("Amman", "Jordan", 31.9454, 35.9284, "Asia/Amman"),
        City("Jerusalem", "Palestine", 31.7683, 35.2137, "Asia/Jerusalem"),
        City("Beirut", "Lebanon", 33.8938, 35.5018, "Asia/Beirut"),
        City("Damascus", "Syria", 33.5138, 36.2765, "Asia/Damascus"),
        City("Istanbul", "Turkey", 41.0082, 28.9784, "Europe/Istanbul"),
        City("Ankara", "Turkey", 39.9334, 32.8597, "Europe/Istanbul"),
        // Africa
        City("Cairo", "Egypt", 30.0444, 31.2357, "Africa/Cairo"),
        City("Casablanca", "Morocco", 33.5731, -7.5898, "Africa/Casablanca"),
        City("Algiers", "Algeria", 36.7538, 3.0588, "Africa/Algiers"),
        City("Tunis", "Tunisia", 36.8065, 10.1815, "Africa/Tunis"),
        City("Lagos", "Nigeria", 6.5244, 3.3792, "Africa/Lagos"),
        City("Nairobi", "Kenya", -1.2921, 36.8219, "Africa/Nairobi"),
        City("Johannesburg", "South Africa", -26.2041, 28.0473, "Africa/Johannesburg"),
        // Asia / Pacific
        City("Jakarta", "Indonesia", -6.2088, 106.8456, "Asia/Jakarta"),
        City("Kuala Lumpur", "Malaysia", 3.1390, 101.6869, "Asia/Kuala_Lumpur"),
        City("Singapore", "Singapore", 1.3521, 103.8198, "Asia/Singapore"),
        City("Tashkent", "Uzbekistan", 41.2995, 69.2401, "Asia/Tashkent"),
        City("Baku", "Azerbaijan", 40.4093, 49.8671, "Asia/Baku"),
        City("Beijing", "China", 39.9042, 116.4074, "Asia/Shanghai"),
        City("Tokyo", "Japan", 35.6762, 139.6503, "Asia/Tokyo"),
        City("Sydney", "Australia", -33.8688, 151.2093, "Australia/Sydney"),
        City("Melbourne", "Australia", -37.8136, 144.9631, "Australia/Melbourne"),
        // Europe
        City("London", "UK", 51.5074, -0.1278, "Europe/London"),
        City("Manchester", "UK", 53.4808, -2.2426, "Europe/London"),
        City("Birmingham", "UK", 52.4862, -1.8904, "Europe/London"),
        City("Paris", "France", 48.8566, 2.3522, "Europe/Paris"),
        City("Berlin", "Germany", 52.5200, 13.4050, "Europe/Berlin"),
        City("Madrid", "Spain", 40.4168, -3.7038, "Europe/Madrid"),
        City("Rome", "Italy", 41.9028, 12.4964, "Europe/Rome"),
        City("Amsterdam", "Netherlands", 52.3676, 4.9041, "Europe/Amsterdam"),
        City("Stockholm", "Sweden", 59.3293, 18.0686, "Europe/Stockholm"),
        City("Moscow", "Russia", 55.7558, 37.6173, "Europe/Moscow"),
        // Americas
        City("New York", "USA", 40.7128, -74.0060, "America/New_York"),
        City("Washington D.C.", "USA", 38.9072, -77.0369, "America/New_York"),
        City("Chicago", "USA", 41.8781, -87.6298, "America/Chicago"),
        City("Houston", "USA", 29.7604, -95.3698, "America/Chicago"),
        City("Los Angeles", "USA", 34.0522, -118.2437, "America/Los_Angeles"),
        City("Toronto", "Canada", 43.6532, -79.3832, "America/Toronto"),
        City("São Paulo", "Brazil", -23.5505, -46.6333, "America/Sao_Paulo"),
        City("Buenos Aires", "Argentina", -34.6037, -58.3816, "America/Argentina/Buenos_Aires")
    )

    fun search(query: String): List<City> {
        val q = query.trim()
        if (q.isEmpty()) return cities
        return cities.filter {
            it.name.contains(q, ignoreCase = true) || it.country.contains(q, ignoreCase = true)
        }
    }
}
