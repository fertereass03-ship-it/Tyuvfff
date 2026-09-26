package com.example.data.api

import com.example.data.api.models.ScheduleItem
import com.example.data.api.models.ShikimoriAnimeDetailDto
import com.example.data.api.models.ShikimoriAnimeDto
import com.example.data.repository.AnimeScheduleData
import com.example.data.settings.AppSettingsManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap

data class AnimeEpisodeInfo(
    val airedEpisodes: Int?,
    val totalEpisodes: Int?,
    val isOngoing: Boolean,
    val isAnons: Boolean,
    val formattedText: String
)

object AnimeEpisodeHelper {

    // Known IDs of anime that already aired or released in 2026 or earlier (MUST NEVER BE SHOWN IN ANONS!)
    val KNOWN_RELEASED_OR_AIRED_IDS: Set<Long> = setOf(
        59978L, // Провожающая в последний путь Фрирен 2 (10 сер., released 2026)
        57658L, // Магическая битва: Смертельная миграция (12 сер., released 2026)
        51553L, // Ателье колдовских колпаков (13 сер., released 2026)
        60071L, // Ателье колдовских колпаков (alt id)
        55825L, // Адский рай 2 (12 сер., released 2026)
        56009L, // Приговорённый быть героем (12 сер., released 2026)
        61316L, // Re:Zero. Жизнь с нуля в альтернативном мире 4 (18 сер., ongoing 2026)
        61317L, // Re:Zero 4 (alt id)
        59193L, // Реинкарнация безработного 3 (14 сер., ongoing 2026)
        60058L, // Ребёнок идола 3 (10 сер., released 2026)
        58505L, // Ребёнок идола 3 (alt id)
        58123L, // Дни Сакамото (11 сер., ongoing 2026)
        58564L, // Поднятие уровня в одиночку 2 (12 сер., released 2026)
        58567L, // Поднятие уровня в одиночку 2 (alt id)
        57334L, // Дандадан 2 (12 сер., released 2026)
        58494L, // Синяя тюрьма: Блю Лок 2 (14 сер., released 2026)
        54857L, // Re:Zero 3 (16 сер., released 2026)
        58514L, // Рубеж Шангри-Ла 2 (25 сер., ongoing 2026)
        57989L, // Ранма 1/2 (2026) (12 сер., released 2026)
        58778L, // Ветролом 2 (12 сер., ongoing 2026)
        57887L, // Кайдзю № 8 (2 сезон) (12 сер., ongoing 2026)
        59970L, // О моём перерождении в слизь 4 (24 сер., released 2026)
        60601L, // Перерождение в аристократа 3 (12 сер., released 2026)
        59787L, // Военная хроника Ромелии (12 сер., released 2026)
        49233L, // Военная хроника маленькой девочки 2 (12 сер., released 2026)
        59708L, // Класс превосходства 4: Второй год (16 сер., released 2026)
        60371L, // Ты и я — полные противоположности (12 сер., released 2026)
        62001L, // Цугаи загробного мира (24 сер., released 2026)
        62076L, // История о перекуре за супермаркетом (12 сер., released 2026)
        61469L, // Невероятное приключение ДжоДжо: Гонка «Стальной шар» (ongoing 2026)
        58483L, // Доктор Стоун: Будущее науки (12 сер., released 2026)
        56788L, // Блич: Тысячелетняя кровавая война — Конфликт (13 сер., released 2026)
        60444L, // О моём перерождении в слизь: Фильм 2 (released)
        57181L, // Голубая шкатулка (25 сер., released 2026)
        55194L  // Медалистка (13 сер., released 2026)
    )

    // Curated accurate aired/total voiced episodes baseline for popular ongoings and 2026 releases
    val KNOWN_ONGOING_EPISODES: Map<Long, Pair<Int, Int>> = mapOf(
        59193L to Pair(14, 14), // Реинкарнация безработного 3 (14 из 14 сер.)
        59978L to Pair(10, 10), // Провожающая в последний путь Фрирен 2 (10 сер. в озвучке)
        57658L to Pair(12, 12), // Магическая битва: Смертельная миграция (12 сер.)
        51553L to Pair(13, 13), // Ателье колдовских колпаков (13 сер.)
        60071L to Pair(13, 13), // Ателье колдовских колпаков (13 сер.)
        55825L to Pair(12, 12), // Адский рай 2 (12 сер.)
        56009L to Pair(12, 12), // Приговорённый быть героем (12 сер.)
        61316L to Pair(18, 18), // Re:Zero 4 (18 сер.)
        61317L to Pair(18, 18), // Re:Zero 4 (18 сер.)
        60058L to Pair(10, 10), // Ребёнок идола 3 (10 сер.)
        58505L to Pair(10, 10), // Ребёнок идола 3 (10 сер.)
        58123L to Pair(11, 12), // Дни Сакамото (11 из 12 сер.)
        58564L to Pair(12, 12), // Поднятие уровня в одиночку 2 (12 из 12 сер.)
        58567L to Pair(12, 12), // Поднятие уровня в одиночку 2 (12 сер.)
        57334L to Pair(12, 12), // Дандадан 2 (12 из 12 сер.)
        58494L to Pair(14, 14), // Синяя тюрьма: Блю Лок 2 (14 сер.)
        54857L to Pair(16, 16), // Re:Zero 3 (16 сер.)
        58514L to Pair(22, 25), // Рубеж Шангри-Ла 2 (22 из 25 сер.)
        57989L to Pair(12, 12), // Ранма 1/2 (2026) (12 сер.)
        58778L to Pair(6, 12),  // Ветролом 2 (6 из 12 сер.)
        57887L to Pair(12, 12), // Кайдзю № 8 (2 сезон) (12 сер.)
        59970L to Pair(24, 24), // О моём перерождении в слизь 4 (24 сер.)
        60601L to Pair(12, 12), // Перерождение в аристократа со способностью анализа 3 (12 сер.)
        59787L to Pair(12, 12), // Военная хроника Ромелии (12 сер.)
        49233L to Pair(12, 12), // Военная хроника маленькой девочки 2 (12 сер.)
        59708L to Pair(16, 16), // Класс превосходства 4 (16 сер.)
        60371L to Pair(12, 12), // Ты и я — полные противоположности (12 сер.)
        62001L to Pair(24, 24), // Цугаи загробного мира (24 сер.)
        62076L to Pair(12, 12), // История о перекуре за супермаркетом (12 сер.)
        58483L to Pair(12, 12), // Доктор Стоун: Будущее науки (12 сер.)
        56788L to Pair(13, 13), // Блич: Конфликт (13 сер.)
        57181L to Pair(25, 25), // Голубая шкатулка (25 сер.)
        55194L to Pair(13, 13), // Медалистка (13 сер.)
        60444L to Pair(1, 1),   // Слизь фильм 2
        63098L to Pair(12, 12), // Псайрен (Инкогнито)
        63409L to Pair(12, 12), // Я ведьма, которую возлюбленный попросил создать любовное зелье
        63712L to Pair(12, 12), // Я перевоплотился в гоблина, вопросы есть?
        63240L to Pair(16, 52), // Путешествие к бессмертию 5
        61214L to Pair(12, 12), // Шероховатый мир: Перерождение
        37096L to Pair(248, 250), // Игры и драконы
        21L to Pair(1123, 1123)   // Ван-Пис
    )

    // ONLY verified real unreleased anime / films / sequels that have NOT yet aired (реальные анонсы)
    val KNOWN_ANNOUNCEMENT_IDS: Set<Long> = setOf(
        61987L, // Kusuriya no Hitorigoto 3rd Season (Монолог фармацевта 3)
        62516L, // Dandadan 3rd Season (Дандадан 3)
        57555L, // Chainsaw Man Movie: Reze-hen (Человек-бензопила: Фильм — Арка Резе)
        63234L, // Chainsaw Man 2nd Season (Человек-бензопила 2)
        52807L, // One Punch Man 3 (Ванпанчмен 3)
        60636L, // Bleach: TYBW - The Calamity / Part 4 (Бедствие)
        61990L, // Cyberpunk: Edgerunners 2 (Киберпанк: Бегущие по краю 2)
        61967L, // Black Clover 2nd Season (Чёрный клевер 2)
        60810L, // Spy x Family Season 3 (Семья шпиона 3)
        56123L, // Haikyuu!! Final Movie 2 (Волейбол!! Финал 2)
        54900L, // Dorohedoro 2nd Season (Дорохедоро 2)
        56784L, // Enen no Shouboutai: San no Shou (Пламенная бригада пожарных 3)
        57584L, // Kage no Jitsuryokusha ni Naritakute! Movie: Zankyou-hen (Восхождение в тени! Фильм)
        59068L, // Dungeon Meshi Season 2 (Подземелье вкусностей 2)
        61006L, // Bocchi the Rock! 2nd Season (Одинокий рокер! 2)
        59873L, // Tokidoki Bosotto Russia-go... Season 2 (Аля иногда кокетничает со мной по-русски 2)
        63816L, // Sousou no Frieren: Ougonkyou-hen (Провожающая в последний путь Фрирен: Золотая земля)
        53913L, // Tensei shitara Ken deshita II (О моём перерождении в меч 2)
        56732L, // Sekai Saikou no Ansatsusha Season 2 (Лучший в мире ассасин 2)
        59139L, // Tsuki ga Michibiku Isekai Douchuu 3rd Season (Лунное путешествие 3)
        58934L, // Mashle Final Exam (Магия и мускулы: Турнир трёх магий)
        63147L, // Gachiakuta 2nd Season (Гатиакута 2)
        60509L, // Lazarus (Лазарь)
        64546L, // Solo Leveling: Beyond the System (За гранью системы)
        51234L, // Guimi Zhi Zhu (Повелитель тайн / Lord of the Mysteries)
        59234L, // Blue Lock 3rd Season (Синяя тюрьма: Блю Лок 3)
        59345L, // Boku no Hero Academia Final Season (Моя геройская академия: Финальный сезон)
        48549L, // Mahou Shoujo Madoka Magica: Walpurgis no Kaiten (Мадока: Выворот Вальпургиевой ночи)
        62111L, // Berserk: Black Swordsman (Берсерк: Черные мечи)
        62222L, // No Game No Life 2nd Season (Нет игры — нет жизни 2)
        60122L, // Grand Blue Season 2 (Необъятный океан 2)
        61555L, // Choujin X (Сверхчеловек X)
        53998L, // Fate/strange Fake TV
        62567L, // Vinland Saga Season 3 (Сага о Винланде 3)
        63012L, // Bungou Stray Dogs 6th Season (Великий из бродячих псов 6)
        61338L, // Shangri-La Frontier 3rd Season (Рубеж Шангри-Ла 3)
        61203L, // Kono Subarashii Sekai ni Shukufuku wo! 4 (Этот замечательный мир! 4 / Коносуба 4)
        61323L, // Ao no Hako Season 2 (Голубая шкатулка 2)
        63794L, // [Oshi no Ko] 4th Season (Ребёнок идола 4)
        54250L, // Made in Abyss: Mezameru Shinpi (Созданный в Бездне: Тайна пробуждения)
        63824L, // Jujutsu Kaisen: Shimetsu Kaiyuu - Kouhen (Магическая битва: Смертельная миграция. Часть 2)
        62844L, // Kusuriya no Hitorigoto Movie: Bouhi no Hihou (Монолог фармацевта: Фильм)
        59088L, // Tokyo Revengers: Santen Sensou-hen (Токийские мстители: Битва трёх небожителей)
        64516L, // Tongari Boushi no Atelier 2nd Season (Ателье колдовских колпаков 2)
        62841L, // Kusuriya no Hitorigoto 3rd Season Part 2 (Монолог фармацевта 3. Часть 2)
        52480L, // Tantei wa Mou, Shindeiru. Season 2 (Детектив уже мёртв 2)
        58967L, // Haikyuu!! Movie: vs. Chiisana Kyojin (Волейбол!! Против Маленького Гиганта)
        63139L, // Sakamoto Days 2nd Season (Дни Сакамото 2)
        63129L  // Tensei shitara Slime Datta Ken 4th Season Part 2 (О моём перерождении в слизь 4. Часть 2)
    )

    private val ANNOUNCEMENT_KEYWORDS: List<String> = listOf(
        "бесконечный замок", "mugenjou", "infinity castle",
        "арка резе", "reze-hen", "reze arc", "человек-бензопила: фильм",
        "ванпанчмен 3", "one punch man 3", "one-punch man 3",
        "бедствие", "calamity", "tybw part 4",
        "дорохедоро 2", "dorohedoro 2",
        "лазарь", "lazarus",
        "семья шпиона 3", "spy x family 3", "spy x family season 3",
        "пламенная бригада пожарных 3", "fire force 3", "enen no shouboutai 3",
        "за гранью системы", "beyond the system",
        "волейбол!! финал 2", "haikyuu!! final 2", "haikyuu final 2",
        "черный клевер 2", "чёрный клевер 2", "black clover 2",
        "синяя тюрьма 3", "blue lock 3",
        "берсерк: черные мечи", "berserk: the black swordsman",
        "моя геройская академия: финал", "my hero academia final",
        "вальпургиевой ночи", "walpurgisnacht", "walpurgis no kaiten",
        "нет игры — нет жизни 2", "no game no life 2",
        "необъятный океан 2", "grand blue 2", "grand blue season 2",
        "сверхчеловек x", "choujin x",
        "странная подделка", "strange fake",
        "киберпанк: бегущие по краю 2", "cyberpunk: edgerunners 2",
        "сага о винланде 3", "vinland saga 3",
        "монолог фармацевта 3", "kusuriya no hitorigoto 3",
        "дандадан 3", "dandadan 3",
        "коносуба 4", "konosuba 4", "kono subarashii sekai ni shukufuku wo! 4",
        "голубая шкатулка 2", "ao no hako 2", "ao no hako season 2",
        "ребёнок идола 4", "oshi no ko 4", "oshi no ko season 4",
        "рубеж шангри-ла 3", "shangri-la frontier 3",
        "сокровище покойной наложницы", "bouhi no hihou",
        "тайна пробуждения", "mezameru shinpi",
        "битва трёх небожителей", "santen sensou-hen",
        "дни сакамото 2", "sakamoto days 2",
    )

    // In-memory cache for dynamic voice/dubbing episode counts discovered in real time from Kodik
    private val dynamicVoiceEpisodesCache = ConcurrentHashMap<Long, Int>()

    // Concurrency control to throttle background queries
    private val lastCheckedTimestamps = ConcurrentHashMap<Long, Long>()

    // Reactive StateFlow broadcasting live episode info updates across all screens
    private val _liveEpisodesFlow = MutableStateFlow<Map<Long, AnimeEpisodeInfo>>(emptyMap())
    val liveEpisodesFlow: StateFlow<Map<Long, AnimeEpisodeInfo>> = _liveEpisodesFlow.asStateFlow()

    fun recordVoiceEpisodes(animeId: Long, maxEpisodes: Int) {
        if (maxEpisodes > 0) {
            val current = dynamicVoiceEpisodesCache[animeId] ?: 0
            if (maxEpisodes > current) {
                dynamicVoiceEpisodesCache[animeId] = maxEpisodes
            }
        }
    }

    /**
     * Triggers a fast background update directly querying Kodik voiceovers.
     * Updates automatically and broadcasts real-time counts when new episodes release.
     */
    fun requestLiveUpdate(
        animeId: Long,
        name: String? = null,
        russian: String? = null,
        episodesTotal: Int? = null,
        episodesAired: Int? = null,
        status: String? = null
    ) {
        if (animeId <= 0) return
        val candidateTitle = "${russian ?: ""} ${name ?: ""}"
        if (isAnnouncement(id = animeId, title = candidateTitle, status = status)) return
        val now = System.currentTimeMillis()
        val lastCheck = lastCheckedTimestamps[animeId] ?: 0L
        if (now - lastCheck < 60_000L) {
            // Already checked within last minute
            return
        }
        lastCheckedTimestamps[animeId] = now

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val titleCandidate = russian?.takeIf { it.isNotBlank() } ?: name
                val liveVoiced = KodikService.getLiveVoicedEpisodeCount(animeId, titleCandidate)
                if (liveVoiced != null && liveVoiced > 0) {
                    recordVoiceEpisodes(animeId, liveVoiced)
                    val info = getEpisodeInfo(
                        id = animeId,
                        title = candidateTitle,
                        episodes = episodesTotal,
                        episodesAired = liveVoiced,
                        status = status ?: "ongoing"
                    )
                    val currentMap = _liveEpisodesFlow.value.toMutableMap()
                    currentMap[animeId] = info
                    _liveEpisodesFlow.value = currentMap
                }
            } catch (_: Exception) {}
        }
    }

    /**
     * Extracts upcoming year from date string or description text
     */
    fun extractYear(airedOn: String?, description: String? = null): Int? {
        val y = airedOn?.take(4)?.toIntOrNull()
        if (y != null) return y
        if (!description.isNullOrBlank()) {
            val match = Regex("""\b(202[6-9]|203[0-9])\b""").find(description)
            if (match != null) {
                return match.value.toIntOrNull()
            }
            val shortMatch = Regex("""\b(2[6-9])-м? году\b""", RegexOption.IGNORE_CASE).find(description)
            if (shortMatch != null) {
                val yy = shortMatch.groupValues[1].toIntOrNull()
                if (yy != null) return 2000 + yy
            }
        }
        return null
    }

    /**
     * Checks if an anime is an unreleased announcement (anons) that has NOT yet aired
     */
    fun isAnnouncement(
        id: Long = 0,
        title: String? = null,
        status: String? = null,
        airedOn: String? = null,
        releasedOn: String? = null,
        year: Int? = null,
        description: String? = null,
        episodesAired: Int? = null
    ): Boolean {
        // 1. HARD DISQUALIFICATIONS: If it already has aired episodes, or is released/ongoing, or in known released lists - NOT an announcement!
        if (episodesAired != null && episodesAired > 0) return false
        if (status?.equals("released", ignoreCase = true) == true) return false
        if (status?.equals("ongoing", ignoreCase = true) == true) return false
        if (!releasedOn.isNullOrBlank()) return false
        if (id > 0 && KNOWN_RELEASED_OR_AIRED_IDS.contains(id)) return false
        if (id > 0 && KNOWN_ONGOING_EPISODES.containsKey(id)) return false

        // Kimetsu no Yaiba / Demon Slayer / Клинок is an existing released anime and must NOT be in announcements
        if (id == 59192L || id == 62546L || id == 38000L || id == 40456L || id == 47778L || id == 51019L || id == 55701L) return false
        val candidateTitle = if (title.isNullOrBlank() && id > 0) {
            ShikimoriRussianTitles.resolveRussianTitle(id, null, null)
        } else title
        val titleLower = candidateTitle?.lowercase().orEmpty()
        if (titleLower.contains("клинок") || titleLower.contains("kimetsu no yaiba") || titleLower.contains("demon slayer") || titleLower.contains("винищувач демонів")) {
            return false
        }

        // Check air date: if airedOn is in the past (before today 2026-09-26), it already started airing/released!
        val airYear = year ?: airedOn?.take(4)?.toIntOrNull()
        if (airYear != null && airYear < 2026) {
            return false
        }
        if (!airedOn.isNullOrBlank() && airedOn.length >= 10 && airedOn < "2026-09-26") {
            return false
        }

        // 2. Known verified unreleased announcements
        if (id > 0 && KNOWN_ANNOUNCEMENT_IDS.contains(id)) return true

        // 3. Status explicitly anons / announced / not_yet_aired
        if (status?.equals("anons", ignoreCase = true) == true) return true
        if (status?.contains("announced", ignoreCase = true) == true) return true
        if (status?.contains("not_yet_aired", ignoreCase = true) == true) return true
        if (titleLower.isNotBlank() && ANNOUNCEMENT_KEYWORDS.any { titleLower.contains(it) }) {
            return true
        }

        if (airYear != null && airYear >= 2027) return true
        if (!airedOn.isNullOrBlank() && airedOn >= "2026-10-01") return true

        return false
    }

    fun isAnnouncement(anime: ShikimoriAnimeDto): Boolean =
        isAnnouncement(
            id = anime.id,
            title = "${anime.russian ?: ""} ${anime.name}",
            status = anime.status,
            airedOn = anime.airedOn,
            releasedOn = anime.releasedOn,
            episodesAired = anime.episodesAired
        )

    fun isAnnouncement(anime: ShikimoriAnimeDetailDto): Boolean =
        isAnnouncement(
            id = anime.id,
            title = "${anime.russian ?: ""} ${anime.name}",
            status = anime.status,
            airedOn = anime.airedOn,
            releasedOn = anime.releasedOn,
            description = anime.description,
            episodesAired = anime.episodesAired
        )

    fun isAnnouncement(id: Long, title: String? = null): Boolean =
        isAnnouncement(id = id, title = title, status = null)

    /**
     * Formats announcement release date in user-friendly text
     */
    fun formatAnnouncementDate(
        airedOn: String?,
        isUk: Boolean = AppSettingsManager.isUkrainian(),
        description: String? = null
    ): String {
        val extractedYear = extractYear(airedOn, description)
        val yearStr = extractedYear?.toString() ?: airedOn?.take(4)

        return when {
            !airedOn.isNullOrBlank() && airedOn.length >= 10 -> {
                val parts = airedOn.split("-")
                if (parts.size == 3) {
                    val y = parts[0]
                    val m = parts[1].toIntOrNull() ?: 1
                    val d = parts[2].toIntOrNull() ?: 1
                    val monthName = when (m) {
                        1 -> if (isUk) "січня" else "января"
                        2 -> if (isUk) "лютого" else "февраля"
                        3 -> if (isUk) "березня" else "марта"
                        4 -> if (isUk) "квітня" else "апреля"
                        5 -> if (isUk) "травня" else "мая"
                        6 -> if (isUk) "червня" else "июня"
                        7 -> if (isUk) "липня" else "июля"
                        8 -> if (isUk) "серпня" else "августа"
                        9 -> if (isUk) "вересня" else "сентября"
                        10 -> if (isUk) "жовтня" else "октября"
                        11 -> if (isUk) "листопада" else "ноября"
                        12 -> if (isUk) "грудня" else "декабря"
                        else -> ""
                    }
                    if (isUk) "Вихід: $d $monthName $y р." else "Выход: $d $monthName $y г."
                } else if (!yearStr.isNullOrBlank()) {
                    if (isUk) "Очікується у $yearStr році" else "Ожидается в $yearStr году"
                } else {
                    if (isUk) "Анонс" else "Анонс"
                }
            }
            !yearStr.isNullOrBlank() -> {
                if (isUk) "Очікується у $yearStr році" else "Ожидается в $yearStr году"
            }
            else -> if (isUk) "Дата виходу уточнюється" else "Дата выхода уточняется"
        }
    }

    /**
     * Resolves the best available episode info for a ShikimoriAnimeDto.
     */
    fun getEpisodeInfo(anime: ShikimoriAnimeDto, isUk: Boolean = AppSettingsManager.isUkrainian()): AnimeEpisodeInfo {
        return getEpisodeInfo(
            id = anime.id,
            title = "${anime.russian ?: ""} ${anime.name}",
            episodes = anime.episodes,
            episodesAired = anime.episodesAired,
            status = anime.status,
            airedOn = anime.airedOn,
            isUk = isUk
        )
    }

    /**
     * Resolves the best available episode info for a ShikimoriAnimeDetailDto.
     */
    fun getEpisodeInfo(anime: ShikimoriAnimeDetailDto, isUk: Boolean = AppSettingsManager.isUkrainian()): AnimeEpisodeInfo {
        return getEpisodeInfo(
            id = anime.id,
            title = "${anime.russian ?: ""} ${anime.name}",
            episodes = anime.episodes,
            episodesAired = anime.episodesAired,
            status = anime.status,
            airedOn = anime.airedOn,
            isUk = isUk
        )
    }

    /**
     * Core resolver using primitives.
     */
    fun getEpisodeInfo(
        id: Long,
        title: String? = null,
        episodes: Int? = null,
        episodesAired: Int? = null,
        status: String? = null,
        airedOn: String? = null,
        isUk: Boolean = AppSettingsManager.isUkrainian()
    ): AnimeEpisodeInfo {
        val isAnonsStatus = isAnnouncement(id = id, title = title, status = status, airedOn = airedOn, episodesAired = episodesAired)
        if (isAnonsStatus) {
            return AnimeEpisodeInfo(
                airedEpisodes = 0,
                totalEpisodes = episodes,
                isOngoing = false,
                isAnons = true,
                formattedText = formatAnnouncementDate(airedOn, isUk)
            )
        }
        val isOngoingStatus = status?.contains("ongoing", ignoreCase = true) == true

        // 1. Dynamic dubbing cache from Kodik player
        val cachedVoiceAired = dynamicVoiceEpisodesCache[id]

        // 2. Curated baseline for popular ongoings
        val curated = KNOWN_ONGOING_EPISODES[id]

        // 3. Schedule items
        val scheduleItem: ScheduleItem? = try {
            AnimeScheduleData.getRealShikimoriSchedule().firstOrNull { it.anime.id == id }
        } catch (_: Exception) {
            null
        }

        val scheduleAired: Int? = scheduleItem?.let { item ->
            val next = item.nextEpisode ?: 1
            (next - 1).coerceAtLeast(0)
        }
        val scheduleTotal: Int? = scheduleItem?.anime?.episodes?.takeIf { it > 0 }
            ?: scheduleAired?.let { aired -> if (aired > 12) (if (aired <= 14) 14 else 24) else 12 }

        // Resolve best aired episodes count: ALWAYS prioritize the real live count in voiceover
        val resolvedAired: Int? = when {
            cachedVoiceAired != null && cachedVoiceAired > 0 -> {
                val baseline = maxOf(episodesAired ?: 0, curated?.first ?: 0, scheduleAired ?: 0)
                maxOf(cachedVoiceAired, baseline)
            }
            episodesAired != null && episodesAired > 0 -> episodesAired
            curated != null && curated.first > 0 -> curated.first
            scheduleAired != null && scheduleAired > 0 -> scheduleAired
            else -> null
        }

        // Resolve best total episodes count
        val resolvedTotal: Int? = when {
            curated != null && curated.second > 0 -> {
                if (episodes != null && episodes > 0 && episodes >= (resolvedAired ?: 0)) episodes else curated.second
            }
            episodes != null && episodes > 0 -> episodes
            scheduleTotal != null && scheduleTotal > 0 -> scheduleTotal
            else -> null
        }

        // Determine if ongoing
        val isOngoing = !isAnonsStatus && (
            isOngoingStatus ||
            scheduleItem != null ||
            (resolvedAired != null && resolvedTotal != null && resolvedAired > 0 && resolvedAired < resolvedTotal) ||
            (resolvedAired != null && resolvedAired > 0 && resolvedTotal == null)
        )

        val formattedText = formatEpisodeLabel(
            aired = resolvedAired,
            total = resolvedTotal,
            isOngoing = isOngoing,
            isAnons = isAnonsStatus,
            isUk = isUk
        )

        return AnimeEpisodeInfo(
            airedEpisodes = resolvedAired,
            totalEpisodes = resolvedTotal,
            isOngoing = isOngoing,
            isAnons = isAnonsStatus,
            formattedText = formattedText
        )
    }

    /**
     * Formats the episode text label.
     * Ongoing examples:
     * - "13 из 14 сер." (RU) / "13 з 14 сер." (UK)
     * - "10 из 12 сер." (RU) / "10 з 12 сер." (UK)
     * Released / all out examples:
     * - "12 сер." (RU/UK)
     */
    fun formatEpisodeLabel(
        aired: Int?,
        total: Int?,
        isOngoing: Boolean,
        isAnons: Boolean,
        isUk: Boolean
    ): String {
        return when {
            isAnons -> {
                val base = "Анонс"
                if (total != null && total > 0) "$base ($total сер.)" else base
            }
            isOngoing -> {
                when {
                    aired != null && total != null && total > 0 -> {
                        if (aired < total) {
                            if (isUk) "$aired з $total сер." else "$aired из $total сер."
                        } else {
                            "$total сер."
                        }
                    }
                    aired != null && aired > 0 -> {
                        if (isUk) "Вийшло $aired сер." else "Вышло $aired сер."
                    }
                    total != null && total > 0 -> {
                        if (isUk) "Онґоінґ ($total сер.)" else "Онгоинг ($total сер.)"
                    }
                    else -> {
                        if (isUk) "Онґоінґ" else "Онгоинг"
                    }
                }
            }
            total != null && total > 0 -> {
                "$total сер."
            }
            aired != null && aired > 0 -> {
                "$aired сер."
            }
            else -> ""
        }
    }
}
