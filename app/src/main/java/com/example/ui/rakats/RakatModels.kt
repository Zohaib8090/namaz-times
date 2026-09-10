package com.example.ui.rakats

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GoldLight
import com.example.ui.theme.GreenSuccess
import com.example.ui.theme.TextMuted

enum class RakatType(
    val displayName: String,
    val arabicName: String,
    val primaryColor: Color,
    val description: String,
    val isMandatory: Boolean
) {
    FARZ(
        displayName = "Farz",
        arabicName = "فَرْض",
        primaryColor = GoldAccent,
        description = "Mandatory / Obligatory prayer. Missing it intentionally is a major sin.",
        isMandatory = true
    ),
    SUNNAH_MUAKKADAH(
        displayName = "Sunnah Mu'akkadah",
        arabicName = "سُنَّة مُؤَكَّدَة",
        primaryColor = EmeraldLight,
        description = "Emphasized Sunnah constantly practiced by Prophet Muhammad ﷺ. Omitting regularly is blameworthy.",
        isMandatory = false
    ),
    SUNNAH_GHAIR_MUAKKADAH(
        displayName = "Sunnah Ghair Mu'akkadah",
        arabicName = "سُنَّة غَيْر مُؤَكَّدَة",
        primaryColor = Color(0xFF38BDF8), // Light Sky Blue
        description = "Recommended Sunnah rewarded when performed, no sin if omitted.",
        isMandatory = false
    ),
    WITR_WAJIB(
        displayName = "Witr (Wajib)",
        arabicName = "وِتْر وَاجِب",
        primaryColor = Color(0xFFF59E0B), // Amber
        description = "Necessary / Wajib prayer in Hanafi Fiqh. Prayed with 3 Rakats and Dua Qunoot.",
        isMandatory = true
    ),
    NAFL(
        displayName = "Nafl",
        arabicName = "نَفْل",
        primaryColor = Color(0xFFA7F3D0), // Soft Mint
        description = "Voluntary prayer bringing extra closeness and high spiritual reward.",
        isMandatory = false
    )
}

data class RakatSegment(
    val type: RakatType,
    val count: Int,
    val timing: String, // e.g. "Before Farz", "After Farz"
    val notes: String
)

data class PrayerRakat(
    val id: String,
    val name: String,
    val arabicName: String,
    val totalRakats: Int,
    val timingDescription: String,
    val sequence: List<RakatSegment>,
    val virtues: String,
    val tips: List<String> = emptyList()
)

object RakatRepository {

    val dailyPrayers: List<PrayerRakat> = listOf(
        PrayerRakat(
            id = "fajr",
            name = "Fajr",
            arabicName = "صَلَاة الفَجْر",
            totalRakats = 4,
            timingDescription = "From true dawn until just before sunrise",
            sequence = listOf(
                RakatSegment(
                    type = RakatType.SUNNAH_MUAKKADAH,
                    count = 2,
                    timing = "Before Farz",
                    notes = "The Prophet ﷺ said: 'The two rakats of Fajr are better than the world and all that is in it.' (Muslim)"
                ),
                RakatSegment(
                    type = RakatType.FARZ,
                    count = 2,
                    timing = "Obligatory",
                    notes = "Recited aloud (Jahri) in congregation by the Imam. Qirat is recited in both rakats."
                )
            ),
            virtues = "Guarantees Allah's protection throughout the day and protects from hypocrisy.",
            tips = listOf(
                "Never omit the 2 Sunnah of Fajr; they are the most strongly emphasized Sunnah.",
                "Surah Al-Kafirun in 1st Rakat and Surah Al-Ikhlas in 2nd Rakat of Sunnah is Sunnah."
            )
        ),
        PrayerRakat(
            id = "dhuhr",
            name = "Dhuhr",
            arabicName = "صَلَاة الظُّهْر",
            totalRakats = 12,
            timingDescription = "From sun passing zenith until Asr begins",
            sequence = listOf(
                RakatSegment(
                    type = RakatType.SUNNAH_MUAKKADAH,
                    count = 4,
                    timing = "Before Farz",
                    notes = "4 Rakats with one Salam. Surah recited after Fatiha in all 4 rakats."
                ),
                RakatSegment(
                    type = RakatType.FARZ,
                    count = 4,
                    timing = "Obligatory",
                    notes = "Silent recitation (Sirri). Surah recited after Fatiha in the first 2 rakats only."
                ),
                RakatSegment(
                    type = RakatType.SUNNAH_MUAKKADAH,
                    count = 2,
                    timing = "After Farz",
                    notes = "Prayed immediately following the 4 Farz."
                ),
                RakatSegment(
                    type = RakatType.NAFL,
                    count = 2,
                    timing = "After Sunnah",
                    notes = "Voluntary prayer with great reward for continuous remembrance."
                )
            ),
            virtues = "Whoever prays 12 rakats daily (including Dhuhr Sunnah), Allah builds a house for him in Paradise (Muslim).",
            tips = listOf(
                "In 4 Farz: recite Surah after Al-Fatiha only in Rakats 1 and 2.",
                "In 4 Sunnah Mu'akkadah: recite Surah after Al-Fatiha in all 4 rakats."
            )
        ),
        PrayerRakat(
            id = "asr",
            name = "Asr",
            arabicName = "صَلَاة العَصْر",
            totalRakats = 8,
            timingDescription = "From mid-afternoon until sunset",
            sequence = listOf(
                RakatSegment(
                    type = RakatType.SUNNAH_GHAIR_MUAKKADAH,
                    count = 4,
                    timing = "Before Farz",
                    notes = "The Prophet ﷺ said: 'May Allah have mercy upon one who prays four rakats before Asr.' (Tirmidhi)"
                ),
                RakatSegment(
                    type = RakatType.FARZ,
                    count = 4,
                    timing = "Obligatory",
                    notes = "Silent recitation (Sirri). Surah after Fatiha in first 2 rakats only."
                )
            ),
            virtues = "Guarantees protection from Hellfire. Prophet ﷺ said: 'Whoever prays the two cool prayers (Fajr & Asr) will enter Paradise.' (Bukhari)",
            tips = listOf(
                "No Nafl prayers are permitted after the Farz of Asr until Maghrib.",
                "The 4 Sunnah before Asr are optional but bring profound mercy and blessings."
            )
        ),
        PrayerRakat(
            id = "maghrib",
            name = "Maghrib",
            arabicName = "صَلَاة المَغْرِب",
            totalRakats = 7,
            timingDescription = "From immediately after sunset until red twilight disappears",
            sequence = listOf(
                RakatSegment(
                    type = RakatType.FARZ,
                    count = 3,
                    timing = "Obligatory",
                    notes = "Aloud in first 2 rakats. First sitting (Qa'da Ula) after 2 rakats with Tashahhud only. Surah only in first 2 rakats."
                ),
                RakatSegment(
                    type = RakatType.SUNNAH_MUAKKADAH,
                    count = 2,
                    timing = "After Farz",
                    notes = "Emphasized Sunnah immediately following Farz."
                ),
                RakatSegment(
                    type = RakatType.NAFL,
                    count = 2,
                    timing = "After Sunnah",
                    notes = "Often prayed as part of Awwabin prayer (2 to 6 rakats of Nafl after Maghrib)."
                )
            ),
            virtues = "Praying Maghrib promptly at sunset is a continuous sign of goodness in the Ummah.",
            tips = listOf(
                "Maghrib time is shorter than other prayers, so hasten to offer it upon the Azan.",
                "In 3 Farz, recite Al-Fatiha alone in the 3rd rakat."
            )
        ),
        PrayerRakat(
            id = "isha",
            name = "Isha",
            arabicName = "صَلَاة العِشَاء",
            totalRakats = 17,
            timingDescription = "From disappearance of twilight until midnight / dawn",
            sequence = listOf(
                RakatSegment(
                    type = RakatType.SUNNAH_GHAIR_MUAKKADAH,
                    count = 4,
                    timing = "Before Farz",
                    notes = "Optional Sunnah recommended before Farz."
                ),
                RakatSegment(
                    type = RakatType.FARZ,
                    count = 4,
                    timing = "Obligatory",
                    notes = "Recited aloud (Jahri) in congregation by the Imam for first 2 rakats."
                ),
                RakatSegment(
                    type = RakatType.SUNNAH_MUAKKADAH,
                    count = 2,
                    timing = "After Farz",
                    notes = "Emphasized Sunnah after Farz."
                ),
                RakatSegment(
                    type = RakatType.NAFL,
                    count = 2,
                    timing = "After Sunnah",
                    notes = "Voluntary prayer."
                ),
                RakatSegment(
                    type = RakatType.WITR_WAJIB,
                    count = 3,
                    timing = "Wajib Prayer",
                    notes = "3 Rakats prayed continuously. In 3rd rakat, recite Fatiha + Surah, raise hands to ears saying Allahu Akbar, fold hands, and recite Dua-e-Qunoot before Ruku."
                ),
                RakatSegment(
                    type = RakatType.NAFL,
                    count = 2,
                    timing = "After Witr",
                    notes = "Sunnah of the Prophet ﷺ to offer 2 light rakats sitting or standing after Witr."
                )
            ),
            virtues = "Prophet ﷺ said: 'Whoever attends Isha prayer in congregation, it is as if he prayed half of the night.' (Muslim)",
            tips = listOf(
                "Salat-ul-Witr is Wajib (essential) and cannot be missed intentionally.",
                "Dua-e-Qunoot is recited standing in the 3rd Rakat of Witr after Takbir."
            )
        )
    )

    val specialPrayers: List<PrayerRakat> = listOf(
        PrayerRakat(
            id = "jummah",
            name = "Jumu'ah (Friday)",
            arabicName = "صَلَاة الجُمُعَة",
            totalRakats = 14,
            timingDescription = "Friday at Dhuhr time with Khutbah (sermon)",
            sequence = listOf(
                RakatSegment(
                    type = RakatType.SUNNAH_MUAKKADAH,
                    count = 4,
                    timing = "Before Khutbah",
                    notes = "Offered upon entering the mosque before the Imam sits for Khutbah."
                ),
                RakatSegment(
                    type = RakatType.FARZ,
                    count = 2,
                    timing = "Obligatory with Imam",
                    notes = "Replaces 4 Farz of Dhuhr for those in congregation behind the Imam."
                ),
                RakatSegment(
                    type = RakatType.SUNNAH_MUAKKADAH,
                    count = 4,
                    timing = "After Farz",
                    notes = "First set of 4 Sunnah Mu'akkadah after Friday Farz."
                ),
                RakatSegment(
                    type = RakatType.SUNNAH_MUAKKADAH,
                    count = 2,
                    timing = "After 4 Sunnah",
                    notes = "Second set of 2 Sunnah (according to Imam Abu Yusuf & Imam Muhammad)."
                ),
                RakatSegment(
                    type = RakatType.NAFL,
                    count = 2,
                    timing = "After Sunnah",
                    notes = "Voluntary Nafl to conclude Friday prayers."
                )
            ),
            virtues = "Best day of the week. Wiping away sins from one Friday to the next.",
            tips = listOf(
                "Listen quietly during the Khutbah; talking is forbidden.",
                "Recite Surah Al-Kahf on Friday for light between the two Fridays."
            )
        ),
        PrayerRakat(
            id = "tahajjud",
            name = "Tahajjud (Night Prayer)",
            arabicName = "صَلَاة التَّهَجُّد",
            totalRakats = 8,
            timingDescription = "Last third of the night after sleeping",
            sequence = listOf(
                RakatSegment(
                    type = RakatType.NAFL,
                    count = 8,
                    timing = "Sets of 2 Rakats",
                    notes = "Prayed in sets of 2 rakats each (up to 8, 10, or 12 rakats). Best performed in solitude with long standing and prolonged prostrations."
                )
            ),
            virtues = "Prophet ﷺ said: 'The best prayer after the obligatory prayers is the night prayer.' (Muslim). Dua is answered in the last third of the night.",
            tips = listOf(
                "Even 2 rakats with sincere heart counts as Tahajjud.",
                "If you plan to wake up for Tahajjud, delay Witr until after Tahajjud."
            )
        ),
        PrayerRakat(
            id = "ishraq_duha",
            name = "Ishraq & Chasht (Duha)",
            arabicName = "صَلَاة الإِشْرَاق وَالضُّحَى",
            totalRakats = 6,
            timingDescription = "15-20 min after sunrise (Ishraq) and mid-morning (Chasht/Duha)",
            sequence = listOf(
                RakatSegment(
                    type = RakatType.NAFL,
                    count = 2,
                    timing = "Ishraq (Sunrise + 20m)",
                    notes = "2 Rakats prayed after sun has risen and brightened. Reward equivalent to Hajj and Umrah (Tirmidhi)."
                ),
                RakatSegment(
                    type = RakatType.NAFL,
                    count = 4,
                    timing = "Chasht / Duha (Mid-morning)",
                    notes = "2 to 8 Rakats offered when sun is high. Fulfills daily charity due on 360 joints of the body."
                )
            ),
            virtues = "Acts of thankfulness for the physical health and blessings granted by Allah Almighty.",
            tips = listOf(
                "Sit in Dhikr from Fajr until sunrise, wait 15 minutes, then pray 2 rakats Ishraq for highest reward."
            )
        ),
        PrayerRakat(
            id = "tarawih",
            name = "Tarawih (Ramadan)",
            arabicName = "صَلَاة التَّرَاوِيح",
            totalRakats = 20,
            timingDescription = "Every night of Ramadan immediately after Isha Farz and Sunnah",
            sequence = listOf(
                RakatSegment(
                    type = RakatType.SUNNAH_MUAKKADAH,
                    count = 20,
                    timing = "10 sets of 2 Rakats",
                    notes = "Prayed in 10 sets of 2 rakats, taking a short rest (Tarweeha) after every 4 rakats. Concluded with 3 Rakats of Witr."
                )
            ),
            virtues = "Prophet ﷺ said: 'Whoever prays at night in Ramadan out of faith and seeking reward, his previous sins will be forgiven.' (Bukhari)",
            tips = listOf(
                "Traditionally recited with complete Quran throughout Ramadan."
            )
        )
    )

    val rakatRules: List<Pair<String, String>> = listOf(
        "Farz (Obligatory)" to "Surah Al-Fatiha is followed by an additional Surah in the first two rakats only. In the 3rd and 4th rakats, only Surah Al-Fatiha is recited quietly.",
        "Sunnah Mu'akkadah & Nafl" to "Surah Al-Fatiha MUST be followed by an additional Surah in EVERY rakat (1st, 2nd, 3rd, and 4th). Omitting the additional Surah in 3rd/4th rakat of Sunnah requires Sajdah Sahw.",
        "Salat-ul-Witr" to "3 Rakats prayed continuously. In the 3rd rakat, recite Surah Al-Fatiha and a Surah. Before going to Ruku, raise hands to ears reciting 'Allahu Akbar', refold hands, and recite Dua-e-Qunoot.",
        "Qa'dah (Sitting Position)" to "In prayers of 3 or 4 rakats, sit after 2 rakats (Qa'da Ula) for Tashahhud (Attahiyyat) only, then stand up. In the final sitting (Qa'da Akhirah), recite Tashahhud, Durood Ibrahim, and Dua Masoora before Salam."
    )
}
