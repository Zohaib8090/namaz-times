package com.example.ui.tasbih

data class DhikrItem(
    val id: String,
    val arabic: String,
    val transliteration: String,
    val translation: String,
    val defaultTarget: Int,
    val virtue: String
)

object AdhkarList {
    val predefinedDhikr: List<DhikrItem> = listOf(
        DhikrItem(
            id = "subhanallah",
            arabic = "سُبْحَانَ اللَّهِ",
            transliteration = "SubhanAllah",
            translation = "Glory be to Allah",
            defaultTarget = 33,
            virtue = "Recited 33 times after every obligatory prayer as taught by Prophet Muhammad ﷺ."
        ),
        DhikrItem(
            id = "alhamdulillah",
            arabic = "الْحَمْدُ لِلَّهِ",
            transliteration = "Alhamdulillah",
            translation = "All praise is due to Allah",
            defaultTarget = 33,
            virtue = "Recited 33 times after every prayer; fills the scales of good deeds."
        ),
        DhikrItem(
            id = "allahu_akbar",
            arabic = "اللَّهُ أَكْبَرُ",
            transliteration = "Allahu Akbar",
            translation = "Allah is the Greatest",
            defaultTarget = 34,
            virtue = "Recited 34 times after prayer to complete the 100 of Tasbih-e-Fatima."
        ),
        DhikrItem(
            id = "astaghfirullah",
            arabic = "أَسْتَغْفِرُ اللَّهَ",
            transliteration = "Astaghfirullah",
            translation = "I seek forgiveness from Allah",
            defaultTarget = 100,
            virtue = "The Prophet ﷺ sought forgiveness over 70 to 100 times daily. Relieves anxiety and opens sustenance."
        ),
        DhikrItem(
            id = "kalimah",
            arabic = "لَا إِلٰهَ إِلَّا اللَّهُ",
            transliteration = "La Ilaha Illallah",
            translation = "There is no deity worthy of worship except Allah",
            defaultTarget = 100,
            virtue = "The best dhikr is 'La ilaha illallah'. Renewal of faith and key to Jannah."
        ),
        DhikrItem(
            id = "salawat",
            arabic = "اللَّهُمَّ صَلِّ عَلَىٰ مُحَمَّدٍ",
            transliteration = "Allahumma Salli 'Ala Muhammad",
            translation = "O Allah, bestow peace and blessings upon Muhammad",
            defaultTarget = 100,
            virtue = "Whoever sends blessings upon the Prophet ﷺ once, Allah sends blessings upon him tenfold."
        ),
        DhikrItem(
            id = "subhanallah_bihamdihi",
            arabic = "سُبْحَانَ اللَّهِ وَبِحَمْدِهِ",
            transliteration = "SubhanAllahi wa biHamdihi",
            translation = "Glory be to Allah and His is the praise",
            defaultTarget = 100,
            virtue = "Whoever says this 100 times in a day, his sins will be forgiven even if they are like the foam of the sea."
        ),
        DhikrItem(
            id = "la_hawla",
            arabic = "لَا حَوْلَ وَلَا قُوَّةَ إِلَّا بِاللَّهِ",
            transliteration = "La Hawla wa la Quwwata illa Billah",
            translation = "There is no power nor might except with Allah",
            defaultTarget = 100,
            virtue = "A treasure from the treasures of Paradise."
        ),
        DhikrItem(
            id = "hasbunallah",
            arabic = "حَسْبُنَا اللَّهُ وَنِعْمَ الْوَكِيلُ",
            transliteration = "HasbunAllahu wa Ni'mal Wakeel",
            translation = "Sufficient for us is Allah, and He is the best Disposer of affairs",
            defaultTarget = 33,
            virtue = "Said by Prophet Ibrahim (AS) when thrown in fire and by Prophet Muhammad ﷺ."
        ),
        DhikrItem(
            id = "custom",
            arabic = "ذِكْرٌ مُطْلَق",
            transliteration = "Free Dhikr / Custom",
            translation = "Count any dhikr, istighfar, or remembrance freely",
            defaultTarget = 100,
            virtue = "Keep your tongue continuously moist with the remembrance of Allah."
        )
    )

    fun getById(id: String): DhikrItem {
        return predefinedDhikr.find { it.id == id } ?: predefinedDhikr.first()
    }
}
