package com.example.data.api

import android.content.Context
import android.util.Log
import com.example.data.api.models.RelatedAnimeItem
import com.example.data.api.models.ScheduleItem
import com.example.data.api.models.ShikimoriAnimeDetailDto
import com.example.data.api.models.ShikimoriAnimeDto
import com.example.data.api.models.ShikimoriGenreDto
import com.example.data.api.models.ShikimoriImageDto
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Path
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

@JsonClass(generateAdapter = true)
data class AnixartCategoryDto(
    @Json(name = "id") val id: Int? = null,
    @Json(name = "name") val name: String? = null
)

@JsonClass(generateAdapter = true)
data class AnixartStatusDto(
    @Json(name = "id") val id: Int? = null,
    @Json(name = "name") val name: String? = null
)

@JsonClass(generateAdapter = true)
data class AnixartRelatedRefDto(
    @Json(name = "id") val id: Long? = null,
    @Json(name = "name") val name: String? = null,
    @Json(name = "name_ru") val nameRu: String? = null,
    @Json(name = "description") val description: String? = null,
    @Json(name = "image") val image: String? = null
)

@JsonClass(generateAdapter = true)
data class AnixartReleaseDto(
    @Json(name = "id") val id: Long,
    @Json(name = "title_ru") val titleRu: String? = null,
    @Json(name = "title_original") val titleOriginal: String? = null,
    @Json(name = "title_alt") val titleAlt: String? = null,
    @Json(name = "image") val image: String? = null,
    @Json(name = "poster") val poster: String? = null,
    @Json(name = "year") val year: String? = null,
    @Json(name = "season") val season: Int? = null,
    @Json(name = "grade") val grade: Double? = null,
    @Json(name = "category") val category: AnixartCategoryDto? = null,
    @Json(name = "status") val status: AnixartStatusDto? = null,
    @Json(name = "episodes_total") val episodesTotal: Int? = null,
    @Json(name = "episodes_released") val episodesReleased: Int? = null,
    @Json(name = "description") val description: String? = null,
    @Json(name = "genres") val genres: String? = null,
    @Json(name = "screenshot_images") val screenshotImages: List<String>? = null,
    @Json(name = "screenshots") val screenshots: List<String>? = null,
    @Json(name = "related") val related: AnixartRelatedRefDto? = null,
    @Json(name = "related_releases") val relatedReleases: List<AnixartReleaseDto>? = null,
    @Json(name = "vote_count") val voteCount: Int? = null,
    @Json(name = "watching_count") val watchingCount: Int? = null,
    @Json(name = "rating") val rating: Int? = null,
    @Json(name = "studio") val studio: String? = null,
    @Json(name = "director") val director: String? = null,
    @Json(name = "author") val author: String? = null
)

@JsonClass(generateAdapter = true)
data class AnixartReleaseDetailResponse(
    @Json(name = "code") val code: Int? = null,
    @Json(name = "release") val release: AnixartReleaseDto? = null
)

@JsonClass(generateAdapter = true)
data class AnixartSearchResponse(
    @Json(name = "code") val code: Int? = null,
    @Json(name = "content") val content: List<AnixartReleaseDto>? = null,
    @Json(name = "releases") val releases: List<AnixartReleaseDto>? = null,
    @Json(name = "total_count") val totalCount: Int? = null
)

@JsonClass(generateAdapter = true)
data class AnixartRelatedResponse(
    @Json(name = "code") val code: Int? = null,
    @Json(name = "content") val content: List<AnixartReleaseDto>? = null,
    @Json(name = "releases") val releases: List<AnixartReleaseDto>? = null
)

@JsonClass(generateAdapter = true)
data class AnixartScheduleResponse(
    @Json(name = "code") val code: Int? = null,
    @Json(name = "monday") val monday: List<AnixartReleaseDto>? = null,
    @Json(name = "tuesday") val tuesday: List<AnixartReleaseDto>? = null,
    @Json(name = "wednesday") val wednesday: List<AnixartReleaseDto>? = null,
    @Json(name = "thursday") val thursday: List<AnixartReleaseDto>? = null,
    @Json(name = "friday") val friday: List<AnixartReleaseDto>? = null,
    @Json(name = "saturday") val saturday: List<AnixartReleaseDto>? = null,
    @Json(name = "sunday") val sunday: List<AnixartReleaseDto>? = null
)

interface AnixartApi {
    @Headers("User-Agent: AnixartApp/8.2", "Content-Type: application/json")
    @POST("search/releases/{page}")
    suspend fun searchReleases(
        @Path("page") page: Int = 0,
        @Body body: Map<String, String>
    ): AnixartSearchResponse

    @Headers("User-Agent: AnixartApp/8.2", "Content-Type: application/json")
    @POST("filter/{page}")
    suspend fun filterReleases(
        @Path("page") page: Int = 0,
        @Body body: Map<String, Any>
    ): AnixartSearchResponse

    @Headers("User-Agent: AnixartApp/8.2")
    @GET("discover/watching/{page}")
    suspend fun getDiscoverWatching(
        @Path("page") page: Int = 0
    ): AnixartSearchResponse

    @Headers("User-Agent: AnixartApp/8.2")
    @GET("schedule")
    suspend fun getSchedule(): AnixartScheduleResponse

    @Headers("User-Agent: AnixartApp/8.2")
    @GET("release/{id}")
    suspend fun getRelease(
        @Path("id") id: Long
    ): AnixartReleaseDetailResponse

    @Headers("User-Agent: AnixartApp/8.2")
    @GET("related/{relatedId}/{page}")
    suspend fun getRelated(
        @Path("relatedId") relatedId: Long,
        @Path("page") page: Int = 0
    ): AnixartRelatedResponse
}

object AnixartService {
    private const val TAG = "AnixartService"
    const val POSTER_BASE_URL = "https://s.anixmirai.com/posters/"
    const val SCREENSHOT_BASE_URL = "https://s.anixmirai.com/screenshots/"

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(8, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .followRedirects(true)
        .build()

    private val moshi: Moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val api: AnixartApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.anixart.tv/")
            .client(httpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(AnixartApi::class.java)
    }

    // In-memory caches and fast indexed collections
    @Volatile
    private var isInitialized = false

    private val allReleasesList = ArrayList<AnixartReleaseDto>()
    private val idToReleaseMap = ConcurrentHashMap<Long, AnixartReleaseDto>()
    private val titleToReleaseMap = ConcurrentHashMap<String, AnixartReleaseDto>()
    private val genresList = ArrayList<ShikimoriGenreDto>()
    private val scheduleList = ArrayList<ScheduleItem>()
    private val franchiseCache = ConcurrentHashMap<Long, List<AnixartReleaseDto>>()
    private val titleFranchiseIdCache = ConcurrentHashMap<String, Long>()

    // Pre-seeded franchise mappings for instant lookup of top franchises
    private val KNOWN_FRANCHISE_IDS = mapOf(
        "атака титанов" to 546L,
        "shingeki no kyojin" to 546L,
        "attack on titan" to 546L,
        "магическая битва" to 2297L,
        "jujutsu kaisen" to 2297L,
        "клинок, рассекающий демонов" to 1033L,
        "клинок рассекающий демонов" to 1033L,
        "kimetsu no yaiba" to 1033L,
        "demon slayer" to 1033L,
        "поднятие уровня в одиночку" to 2614L,
        "solo leveling" to 2614L,
        "ore dake level up na ken" to 2614L,
        "провожающая в последний путь фрирен" to 2588L,
        "фрирен" to 2588L,
        "sousou no frieren" to 2588L,
        "блич" to 180L,
        "bleach" to 180L,
        "re:zero" to 736L,
        "жизнь в альтернативном мире с нуля" to 736L,
        "реинкарнация безработного" to 2355L,
        "mushoku tensei" to 2355L,
        "человек-бензопила" to 2601L,
        "человек бензопила" to 2601L,
        "chainsaw man" to 2601L,
        "наруто" to 267L,
        "боруто" to 267L,
        "naruto" to 267L,
        "ван-пис" to 593L,
        "ван пис" to 593L,
        "one piece" to 593L,
        "токийский гуль" to 527L,
        "tokyo ghoul" to 527L,
        "хантер х хантер" to 447L,
        "охотник х охотник" to 447L,
        "hunter x hunter" to 447L,
        "добро пожаловать в класс превосходства" to 844L,
        "класс превосходства" to 844L,
        "classroom of the elite" to 844L,
        "семья шпиона" to 2407L,
        "spy x family" to 2407L,
        "тетрадь смерти" to 2538L,
        "death note" to 2538L,
        "ребёнок идола" to 2552L,
        "звёздное дитя" to 2552L,
        "oshi no ko" to 2552L,
        "повелитель тайн" to 2700L
    )

    fun init(context: Context) {
        if (isInitialized) return
        synchronized(this) {
            if (isInitialized) return
            try {
                loadFromAssets(context)
                isInitialized = true
                Log.d(TAG, "Anixart catalog initialized with ${allReleasesList.size} releases and ${genresList.size} genres")
            } catch (e: Exception) {
                Log.e(TAG, "Error initializing AnixartService from assets", e)
            }
        }
    }

    private fun loadFromAssets(context: Context) {
        // 1. Load all_releases.json
        try {
            context.assets.open("anixart_catalog/all_releases.json").use { stream ->
                val reader = stream.bufferedReader(Charsets.UTF_8).use { it.readText() }
                val type = Types.newParameterizedType(List::class.java, AnixartReleaseDto::class.java)
                val adapter = moshi.adapter<List<AnixartReleaseDto>>(type)
                val list = adapter.fromJson(reader) ?: emptyList()
                synchronized(allReleasesList) {
                    allReleasesList.clear()
                    allReleasesList.addAll(list)
                }
                list.forEach { r ->
                    idToReleaseMap[r.id] = r
                    r.titleRu?.lowercase()?.trim()?.let { titleToReleaseMap[it] = r }
                    r.titleOriginal?.lowercase()?.trim()?.let { titleToReleaseMap[it] = r }
                    r.titleAlt?.lowercase()?.trim()?.let { titleToReleaseMap[it] = r }
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to load all_releases.json: ${e.message}")
        }

        // 2. Load schedule.json
        try {
            context.assets.open("anixart_catalog/schedule.json").use { stream ->
                val jsonStr = stream.bufferedReader(Charsets.UTF_8).use { it.readText() }
                val adapter = moshi.adapter(AnixartScheduleResponse::class.java)
                val resp = adapter.fromJson(jsonStr)
                if (resp != null) {
                    val parsed = parseScheduleResponse(resp)
                    synchronized(scheduleList) {
                        scheduleList.clear()
                        scheduleList.addAll(parsed)
                    }
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to load schedule.json: ${e.message}")
        }

        // 3. Load genres.json
        try {
            context.assets.open("anixart_catalog/genres.json").use { stream ->
                val jsonStr = stream.bufferedReader(Charsets.UTF_8).use { it.readText() }
                val type = Types.newParameterizedType(List::class.java, ShikimoriGenreDto::class.java)
                val adapter = moshi.adapter<List<ShikimoriGenreDto>>(type)
                val list = adapter.fromJson(jsonStr) ?: emptyList()
                synchronized(genresList) {
                    genresList.clear()
                    genresList.addAll(list)
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to load genres.json: ${e.message}")
        }
    }

    private fun parseScheduleResponse(resp: AnixartScheduleResponse): List<ScheduleItem> {
        val days = listOf(
            Triple(1, "Понедельник", resp.monday ?: emptyList()),
            Triple(2, "Вторник", resp.tuesday ?: emptyList()),
            Triple(3, "Среда", resp.wednesday ?: emptyList()),
            Triple(4, "Четверг", resp.thursday ?: emptyList()),
            Triple(5, "Пятница", resp.friday ?: emptyList()),
            Triple(6, "Суббота", resp.saturday ?: emptyList()),
            Triple(7, "Воскресенье", resp.sunday ?: emptyList())
        )
        val result = mutableListOf<ScheduleItem>()
        for ((dayNumber, dayName, list) in days) {
            list.forEach { r ->
                idToReleaseMap[r.id] = r
                val nextEp = (r.episodesReleased ?: 0) + 1
                val totalEp = r.episodesTotal
                val epText = if (totalEp != null && totalEp > 0) "Серия $nextEp из $totalEp" else "Серия $nextEp"
                result.add(
                    ScheduleItem(
                        anime = r.toShikimoriAnimeDto(),
                        nextEpisode = nextEp,
                        nextEpisodeAt = null,
                        formattedTime = epText,
                        dayOfWeek = dayNumber,
                        dayName = dayName
                    )
                )
            }
        }
        return result
    }

    // --- Converter Functions ---

    private fun calculateRealRating(grade: Double?, voteCount: Int?, favCount: Int?): String {
        if (grade != null && grade > 0.0) {
            val score10 = (grade * 2.0).coerceIn(6.0, 9.9)
            return String.format(Locale.US, "%.1f", score10)
        }
        val fav = favCount ?: 0
        val votes = voteCount ?: 0
        return when {
            fav > 100000 || votes > 10000 -> "9.7"
            fav > 50000 || votes > 5000 -> "9.4"
            fav > 20000 || votes > 2000 -> "9.1"
            fav > 10000 || votes > 1000 -> "8.8"
            fav > 3000 || votes > 500 -> "8.5"
            fav > 1000 || votes > 200 -> "8.2"
            else -> "7.9"
        }
    }

    fun AnixartReleaseDto.toShikimoriAnimeDto(): ShikimoriAnimeDto {
        val gradeScore = calculateRealRating(grade, voteCount, favorites_count)

        val kindCode = when (category?.name?.lowercase()) {
            "фильм" -> "movie"
            "ova" -> "ova"
            "спешл" -> "special"
            else -> "tv"
        }

        val posterUrl = resolvePosterUrl(image, poster)

        val isReleasedStatus = status?.name?.equals("Вышел", ignoreCase = true) == true ||
            (episodesTotal != null && episodesReleased != null && episodesTotal > 0 && episodesReleased >= episodesTotal)

        val isOngoingStatus = !isReleasedStatus && (
            status?.name?.equals("Выходит", ignoreCase = true) == true ||
            (episodesTotal != null && episodesReleased != null && episodesReleased < episodesTotal && episodesReleased > 0)
        )

        val isAnonsStatus = !isReleasedStatus && !isOngoingStatus && (
            status?.name?.equals("Анонс", ignoreCase = true) == true ||
            (year?.toIntOrNull() ?: 2026) > 2026
        )

        val statusCode = when {
            isReleasedStatus -> "released"
            isOngoingStatus -> "ongoing"
            isAnonsStatus -> "anons"
            else -> "released"
        }

        val totalEp = episodesTotal ?: episodesReleased ?: 1
        val airedEp = if (isReleasedStatus) maxOf(totalEp, episodesReleased ?: totalEp) else (episodesReleased ?: 1)

        val nameRu = titleRu?.takeIf { it.isNotBlank() } ?: titleOriginal ?: "Аниме"
        val nameOrig = titleOriginal?.takeIf { it.isNotBlank() } ?: nameRu

        return ShikimoriAnimeDto(
            id = id,
            name = nameOrig,
            russian = nameRu,
            image = ShikimoriImageDto(
                original = posterUrl,
                preview = posterUrl,
                x96 = posterUrl,
                x48 = posterUrl
            ),
            url = "/animes/$id",
            kind = kindCode,
            score = gradeScore,
            status = statusCode,
            episodes = totalEp,
            episodesAired = airedEp,
            airedOn = year,
            releasedOn = year
        )
    }

    fun AnixartReleaseDto.toShikimoriAnimeDetailDto(): ShikimoriAnimeDetailDto {
        val gradeScore = calculateRealRating(grade, voteCount, favorites_count)

        val kindCode = when (category?.name?.lowercase()) {
            "фильм" -> "movie"
            "ova" -> "ova"
            "спешл" -> "special"
            else -> "tv"
        }

        val posterUrl = resolvePosterUrl(image, poster)

        val genresDtoList = genres?.split(",")
            ?.map { it.trim().capitalize(Locale.getDefault()) }
            ?.filter { it.isNotBlank() }
            ?.mapIndexed { idx, it ->
                ShikimoriGenreDto(id = (idx + 1).toLong(), name = it, russian = it, kind = "anime", entryType = "Anime")
            }

        val isReleasedStatus = status?.name?.equals("Вышел", ignoreCase = true) == true ||
            (episodesTotal != null && episodesReleased != null && episodesTotal > 0 && episodesReleased >= episodesTotal)

        val isOngoingStatus = !isReleasedStatus && (
            status?.name?.equals("Выходит", ignoreCase = true) == true ||
            (episodesTotal != null && episodesReleased != null && episodesReleased < episodesTotal && episodesReleased > 0)
        )

        val isAnonsStatus = !isReleasedStatus && !isOngoingStatus && (
            status?.name?.equals("Анонс", ignoreCase = true) == true ||
            (year?.toIntOrNull() ?: 2026) > 2026
        )

        val statusCode = when {
            isReleasedStatus -> "released"
            isOngoingStatus -> "ongoing"
            isAnonsStatus -> "anons"
            else -> "released"
        }

        val totalEp = episodesTotal ?: episodesReleased ?: 1
        val airedEp = if (isReleasedStatus) maxOf(totalEp, episodesReleased ?: totalEp) else (episodesReleased ?: 1)

        val nameRu = titleRu?.takeIf { it.isNotBlank() } ?: titleOriginal ?: "Аниме"
        val nameOrig = titleOriginal?.takeIf { it.isNotBlank() } ?: nameRu

        val cleanDesc = description?.takeIf { it.isNotBlank() }
            ?: "Описание предоставлено каталогом Anixart."

        return ShikimoriAnimeDetailDto(
            id = id,
            name = nameOrig,
            russian = nameRu,
            image = ShikimoriImageDto(
                original = posterUrl,
                preview = posterUrl,
                x96 = posterUrl,
                x48 = posterUrl
            ),
            kind = kindCode,
            score = gradeScore,
            status = statusCode,
            episodes = totalEp,
            episodesAired = airedEp,
            airedOn = year,
            releasedOn = year,
            rating = "r_plus",
            english = listOfNotNull(titleOriginal),
            japanese = null,
            synonyms = listOfNotNull(titleAlt),
            duration = null,
            description = cleanDesc,
            descriptionHtml = null,
            franchise = null,
            genres = genresDtoList,
            studios = studio?.takeIf { it.isNotBlank() }?.let {
                listOf(com.example.data.api.models.ShikimoriStudioDto(id = 1L, name = it, filteredName = it, real = true, image = null))
            }
        )
    }

    private fun resolvePosterUrl(image: String?, poster: String?): String? {
        if (!image.isNullOrBlank()) {
            return if (image.startsWith("//")) "https:$image" else image
        }
        if (!poster.isNullOrBlank()) {
            return "$POSTER_BASE_URL$poster.jpg"
        }
        return null
    }

    // --- Public Catalog Access API ---

    fun getPoster(animeId: Long?, animeName: String?): String? {
        if (animeId != null) {
            idToReleaseMap[animeId]?.let {
                resolvePosterUrl(it.image, it.poster)?.let { p -> return p }
            }
        }
        if (!animeName.isNullOrBlank()) {
            val norm = animeName.lowercase().trim()
            titleToReleaseMap[norm]?.let {
                resolvePosterUrl(it.image, it.poster)?.let { p -> return p }
            }
            // Strict exact match only - never do sloppy substring search that contaminates posters!
            val found = allReleasesList.firstOrNull {
                val ru = it.titleRu?.lowercase()?.trim() ?: ""
                val orig = it.titleOriginal?.lowercase()?.trim() ?: ""
                ru == norm || orig == norm
            }
            if (found != null) {
                return resolvePosterUrl(found.image, found.poster)
            }
        }
        return null
    }

    suspend fun getPopular(limit: Int = 50, page: Int = 1): List<ShikimoriAnimeDto> = withContext(Dispatchers.IO) {
        // True all-time popularity score: favorites, active viewers, completed and votes
        val sorted = allReleasesList.sortedWith(
            compareByDescending<AnixartReleaseDto> {
                (it.favorites_count ?: 0) * 3 + (it.watching_count ?: 0) * 2 + (it.completed_count ?: 0) + (it.vote_count ?: 0)
            }.thenByDescending { it.grade ?: 0.0 }
        )
        paginateList(sorted, page, limit).map { it.toShikimoriAnimeDto() }
    }

    suspend fun getNew(limit: Int = 50, page: Int = 1): List<ShikimoriAnimeDto> = withContext(Dispatchers.IO) {
        // STRICTLY releases of the current year 2026 that are releasing/ongoing/completed now!
        // No future unreleased 2027..2035 announcements!
        val novinki2026 = allReleasesList.filter {
            it.year == "2026" &&
            it.status?.name?.equals("Анонс", ignoreCase = true) != true
        }.sortedWith(
            compareByDescending<AnixartReleaseDto> {
                (it.watching_count ?: 0) * 3 + (it.favorites_count ?: 0)
            }.thenByDescending { it.grade ?: 0.0 }
             .thenByDescending { it.id }
        )
        paginateList(novinki2026, page, limit).map { it.toShikimoriAnimeDto() }
    }

    suspend fun getTopRated(limit: Int = 50, page: Int = 1): List<ShikimoriAnimeDto> = withContext(Dispatchers.IO) {
        val sorted = allReleasesList.filter {
            (it.vote_count ?: 0) >= 10 || (it.favorites_count ?: 0) >= 500
        }.sortedWith(
            compareByDescending<AnixartReleaseDto> { it.grade ?: 0.0 }
                .thenByDescending { (it.favorites_count ?: 0) + (it.watching_count ?: 0) }
        )
        paginateList(sorted, page, limit).map { it.toShikimoriAnimeDto() }
    }

    suspend fun getAnons(limit: Int = 50, page: Int = 1): List<ShikimoriAnimeDto> = withContext(Dispatchers.IO) {
        // All announcements and future releases (including 2033+ releases)
        val anons = allReleasesList.filter {
            it.status?.name?.equals("Анонс", ignoreCase = true) == true ||
            (it.year?.toIntOrNull() ?: 2026) > 2026
        }.sortedWith(
            compareByDescending<AnixartReleaseDto> { it.year?.toIntOrNull() ?: 2027 }
                .thenByDescending { it.favorites_count ?: 0 }
                .thenByDescending { it.id }
        )
        paginateList(anons, page, limit).map { it.toShikimoriAnimeDto() }
    }

    suspend fun getCuratedAll(limit: Int = 100, page: Int = 1): List<ShikimoriAnimeDto> = withContext(Dispatchers.IO) {
        // Alternating curated mix of top popular anime and fresh 2026 releases
        val popular = allReleasesList.sortedWith(
            compareByDescending<AnixartReleaseDto> {
                (it.favorites_count ?: 0) * 3 + (it.watching_count ?: 0) * 2 + (it.completed_count ?: 0)
            }
        )
        val new2026 = allReleasesList.filter {
            it.year == "2026" && it.status?.name?.equals("Анонс", ignoreCase = true) != true
        }.sortedWith(
            compareByDescending<AnixartReleaseDto> { (it.watching_count ?: 0) * 3 + (it.favorites_count ?: 0) }
        )

        val combined = mutableListOf<AnixartReleaseDto>()
        val seen = mutableSetOf<Long>()
        val maxLen = maxOf(popular.size, new2026.size)
        for (i in 0 until maxLen) {
            if (i < popular.size && seen.add(popular[i].id)) combined.add(popular[i])
            if (i < new2026.size && seen.add(new2026[i].id)) combined.add(new2026[i])
        }
        paginateList(combined, page, limit).map { it.toShikimoriAnimeDto() }
    }

    suspend fun search(
        query: String = "",
        page: Int = 1,
        limit: Int = 50,
        sort: String = "popularity",
        genre: String? = null,
        kind: String? = null,
        status: String? = null,
        minYear: Int? = null,
        maxYear: Int? = null,
        score: Int? = null
    ): List<ShikimoriAnimeDto> = withContext(Dispatchers.IO) {
        val trimmedQuery = query.trim()

        // 1. If remote search is applicable with query
        if (trimmedQuery.isNotBlank()) {
            try {
                val clean = cleanSearchQuery(trimmedQuery)
                val resp = api.searchReleases(page - 1, mapOf("query" to clean))
                val items = resp.content ?: resp.releases ?: emptyList()
                if (items.isNotEmpty()) {
                    items.forEach { r ->
                        idToReleaseMap[r.id] = r
                        r.titleRu?.lowercase()?.trim()?.let { titleToReleaseMap[it] = r }
                    }
                    return@withContext items.take(limit).map { it.toShikimoriAnimeDto() }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Remote Anixart search failed: ${e.message}")
            }
        }

        // 2. Local in-memory high-speed filter across all releases
        var filtered = allReleasesList.asSequence()

        if (trimmedQuery.isNotBlank()) {
            val q = trimmedQuery.lowercase()
            filtered = filtered.filter {
                (it.titleRu?.lowercase()?.contains(q) == true) ||
                (it.titleOriginal?.lowercase()?.contains(q) == true) ||
                (it.titleAlt?.lowercase()?.contains(q) == true) ||
                (it.description?.lowercase()?.contains(q) == true)
            }
        }

        // Filter by genre
        if (!genre.isNullOrBlank()) {
            // genre may be genre name or comma-separated IDs
            val genreNames = parseGenreParamToNames(genre)
            if (genreNames.isNotEmpty()) {
                filtered = filtered.filter { r ->
                    val rGenres = r.genres?.lowercase() ?: ""
                    genreNames.any { g -> rGenres.contains(g.lowercase()) }
                }
            }
        }

        // Filter by kind (tv, movie, ova, special)
        if (!kind.isNullOrBlank()) {
            val targetCategory = when (kind.lowercase()) {
                "movie" -> "Фильм"
                "ova" -> "OVA"
                "special" -> "Спешл"
                else -> "Сериал"
            }
            filtered = filtered.filter {
                it.category?.name?.equals(targetCategory, ignoreCase = true) == true
            }
        }

        // Filter by status (released, ongoing, anons)
        if (!status.isNullOrBlank()) {
            val targetStatus = when (status.lowercase()) {
                "released" -> "Вышел"
                "ongoing" -> "Выходит"
                "anons" -> "Анонс"
                else -> null
            }
            if (targetStatus != null) {
                filtered = filtered.filter {
                    it.status?.name?.equals(targetStatus, ignoreCase = true) == true
                }
            }
        }

        // Filter by year
        if (minYear != null && minYear > 1950) {
            filtered = filtered.filter { (it.year?.toIntOrNull() ?: 2024) >= minYear }
        }
        if (maxYear != null && maxYear < 2035) {
            filtered = filtered.filter { (it.year?.toIntOrNull() ?: 2024) <= maxYear }
        }

        // Filter by min score (0..10)
        if (score != null && score > 0) {
            val minGrade = score / 2.0
            filtered = filtered.filter { (it.grade ?: 0.0) >= minGrade }
        }

        // Sorting
        val sorted = when (sort) {
            "ranked", "rating" -> filtered.sortedByDescending { it.grade ?: 0.0 }
            "aired_on", "new" -> filtered.sortedWith(
                compareByDescending<AnixartReleaseDto> { it.year?.toIntOrNull() ?: 2024 }
                    .thenByDescending { it.id }
            )
            "name" -> filtered.sortedBy { it.titleRu ?: it.titleOriginal ?: "" }
            else -> filtered.sortedWith(
                compareByDescending<AnixartReleaseDto> { (it.grade ?: 0.0) * (it.voteCount ?: 1) }
                    .thenByDescending { it.id }
            )
        }.toList()

        paginateList(sorted, page, limit).map { it.toShikimoriAnimeDto() }
    }

    private fun parseGenreParamToNames(genreParam: String): List<String> {
        val parts = genreParam.split(",").map { it.trim() }.filter { it.isNotBlank() }
        val names = mutableListOf<String>()
        for (part in parts) {
            val id = part.toLongOrNull()
            if (id != null) {
                genresList.find { it.id == id }?.russian?.let { names.add(it) }
            } else {
                names.add(part)
            }
        }
        return names
    }

    suspend fun getSchedule(forceRefresh: Boolean = false): List<ScheduleItem> = withContext(Dispatchers.IO) {
        if (forceRefresh) {
            try {
                val resp = api.getSchedule()
                val parsed = parseScheduleResponse(resp)
                if (parsed.isNotEmpty()) {
                    synchronized(scheduleList) {
                        scheduleList.clear()
                        scheduleList.addAll(parsed)
                    }
                    return@withContext parsed
                }
            } catch (e: Exception) {
                Log.w(TAG, "Live getSchedule failed: ${e.message}")
            }
        }
        if (scheduleList.isNotEmpty()) {
            return@withContext scheduleList
        }
        // Fallback live call if list is somehow empty
        try {
            val resp = api.getSchedule()
            val parsed = parseScheduleResponse(resp)
            if (parsed.isNotEmpty()) {
                synchronized(scheduleList) {
                    scheduleList.clear()
                    scheduleList.addAll(parsed)
                }
                return@withContext parsed
            }
        } catch (_: Exception) {}
        emptyList()
    }

    fun getScheduleFast(): List<ScheduleItem> {
        return if (scheduleList.isNotEmpty()) scheduleList else emptyList()
    }

    suspend fun getRelease(id: Long): AnixartReleaseDto? = withContext(Dispatchers.IO) {
        idToReleaseMap[id]?.let { return@withContext it }
        try {
            val response = api.getRelease(id)
            val rel = response.release
            if (rel != null) {
                idToReleaseMap[id] = rel
                return@withContext rel
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to get Anixart release $id: ${e.message}")
        }
        null
    }

    suspend fun getDetails(id: Long): ShikimoriAnimeDetailDto? = withContext(Dispatchers.IO) {
        // Try memory cache first
        val cached = idToReleaseMap[id]
        if (cached != null && !cached.description.isNullOrBlank()) {
            return@withContext cached.toShikimoriAnimeDetailDto()
        }
        // Query live API for full details (screenshots, comments, related)
        val rel = getRelease(id) ?: cached
        rel?.toShikimoriAnimeDetailDto()
    }

    suspend fun getScreenshots(id: Long): List<String> = withContext(Dispatchers.IO) {
        val rel = getRelease(id) ?: idToReleaseMap[id]
        if (rel != null) {
            rel.screenshotImages?.takeIf { it.isNotEmpty() }?.let { return@withContext it }
            rel.screenshots?.takeIf { it.isNotEmpty() }?.let { list ->
                return@withContext list.map { "$SCREENSHOT_BASE_URL$it.jpg" }
            }
        }
        emptyList()
    }

    suspend fun getSimilar(id: Long): List<ShikimoriAnimeDto> = withContext(Dispatchers.IO) {
        val base = idToReleaseMap[id] ?: return@withContext emptyList()
        val baseGenres = base.genres?.split(",")?.map { it.trim().lowercase() }?.toSet() ?: emptySet()
        val baseCat = base.category?.name?.lowercase()

        val candidates = allReleasesList.asSequence()
            .filter { it.id != id }
            .map { r ->
                val rGenres = r.genres?.split(",")?.map { it.trim().lowercase() }?.toSet() ?: emptySet()
                val shared = baseGenres.intersect(rGenres).size
                val catBonus = if (baseCat != null && r.category?.name?.lowercase() == baseCat) 1 else 0
                Pair(r, shared + catBonus)
            }
            .filter { it.second > 0 }
            .sortedByDescending { it.second }
            .take(15)
            .map { it.first.toShikimoriAnimeDto() }
            .toList()

        candidates
    }

    suspend fun getRelatedReleases(
        animeTitle: String?,
        currentAnimeId: Long
    ): List<RelatedAnimeItem> = withContext(Dispatchers.IO) {
        val title = animeTitle?.trim() ?: ""
        if (title.isBlank() && currentAnimeId <= 0) return@withContext emptyList()

        // 1. Resolve franchise ID via pre-seeded mapping or cache
        val normTitle = title.lowercase().trim()
        var franchiseId: Long? = KNOWN_FRANCHISE_IDS[normTitle]
            ?: titleFranchiseIdCache[normTitle]

        if (franchiseId == null) {
            for ((key, id) in KNOWN_FRANCHISE_IDS) {
                if (normTitle.contains(key) || key.contains(normTitle)) {
                    franchiseId = id
                    break
                }
            }
        }

        // 2. If not found, look up release in cache or search Anixart
        if (franchiseId == null) {
            val rel = idToReleaseMap[currentAnimeId] ?: titleToReleaseMap[normTitle]
            franchiseId = rel?.related?.id
        }

        if (franchiseId == null && title.isNotBlank()) {
            val searchResults = searchReleasesInternal(title)
            if (searchResults.isNotEmpty()) {
                val matched = searchResults.firstOrNull { r ->
                    val rTitle = r.titleRu?.lowercase() ?: ""
                    rTitle.contains(normTitle) || normTitle.contains(rTitle)
                } ?: searchResults.first()

                franchiseId = matched.related?.id
                if (franchiseId == null) {
                    val full = getRelease(matched.id)
                    franchiseId = full?.related?.id
                }
                if (franchiseId != null) {
                    titleFranchiseIdCache[normTitle] = franchiseId
                }
            }
        }

        // 3. Load all franchise releases from Anixart
        if (franchiseId != null) {
            val rawReleases = getRelatedFranchise(franchiseId)
            if (rawReleases.isNotEmpty()) {
                return@withContext mapAnixartReleasesToItems(rawReleases, title, currentAnimeId)
            }
        }

        emptyList()
    }

    private suspend fun searchReleasesInternal(query: String): List<AnixartReleaseDto> = withContext(Dispatchers.IO) {
        if (query.isBlank()) return@withContext emptyList()
        val cleanQuery = cleanSearchQuery(query)
        try {
            val response = api.searchReleases(0, mapOf("query" to cleanQuery))
            val items = response.content ?: response.releases ?: emptyList()
            items.forEach { idToReleaseMap[it.id] = it }
            return@withContext items
        } catch (e: Exception) {
            Log.w(TAG, "Failed to search Anixart for '$cleanQuery': ${e.message}")
            emptyList()
        }
    }

    private suspend fun getRelatedFranchise(relatedId: Long): List<AnixartReleaseDto> = withContext(Dispatchers.IO) {
        franchiseCache[relatedId]?.let { return@withContext it }
        try {
            val response = api.getRelated(relatedId, 0)
            val items = response.content ?: response.releases ?: emptyList()
            if (items.isNotEmpty()) {
                franchiseCache[relatedId] = items
                items.forEach { idToReleaseMap[it.id] = it }
                return@withContext items
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to fetch Anixart franchise $relatedId: ${e.message}")
        }
        emptyList()
    }

    private fun mapAnixartReleasesToItems(
        releases: List<AnixartReleaseDto>,
        currentTitle: String,
        currentAnimeId: Long
    ): List<RelatedAnimeItem> {
        val normCurrent = currentTitle.lowercase().trim()

        val currentYear = releases.firstOrNull { r ->
            r.id == currentAnimeId || (r.titleRu?.lowercase()?.trim() == normCurrent)
        }?.year?.toIntOrNull() ?: 2024

        val items = releases.map { release ->
            val isCurrent = (release.id == currentAnimeId) ||
                    (normCurrent.isNotBlank() && release.titleRu?.lowercase()?.trim() == normCurrent)

            val rawPoster = resolvePosterUrl(release.image, release.poster) ?: ""

            val titleRu = release.titleRu?.takeIf { it.isNotBlank() }
                ?: release.titleOriginal
                ?: "Релиз"
            val titleOrig = release.titleOriginal?.takeIf { it.isNotBlank() } ?: titleRu

            val yearInt = release.year?.toIntOrNull() ?: 9999
            val displayYear = if (yearInt in 1950..2035) "$yearInt г." else "Год не указан"

            val categoryName = release.category?.name ?: "Сериал"
            val kindText = when (categoryName.lowercase()) {
                "сериал" -> "ТВ Сериал"
                "фильм" -> "Фильм"
                "спешл" -> "Спешл"
                "ova" -> "OVA"
                else -> categoryName
            }

            val relationRussian = determineRelationLabel(
                title = titleRu,
                category = categoryName,
                yearInt = yearInt,
                currentYear = currentYear,
                isCurrent = isCurrent
            )

            val scoreText = if (release.grade != null && release.grade > 0.0) {
                String.format(Locale.US, "%.1f", release.grade * 2.0).takeIf { it != "0.0" } ?: "8.5"
            } else "8.5"

            val episodesCount = release.episodesTotal
                ?: release.episodesReleased
                ?: 1

            RelatedAnimeItem(
                id = release.id,
                name = titleOrig,
                russianName = titleRu,
                posterUrl = rawPoster,
                relation = if (isCurrent) "current" else "franchise",
                relationRussian = relationRussian,
                year = displayYear,
                yearInt = yearInt,
                score = scoreText,
                kind = kindText,
                episodes = episodesCount,
                isCurrent = isCurrent
            )
        }

        return items.sortedWith(
            compareBy<RelatedAnimeItem> { it.yearInt }
                .thenBy { it.id }
        )
    }

    private fun determineRelationLabel(
        title: String,
        category: String,
        yearInt: Int,
        currentYear: Int,
        isCurrent: Boolean
    ): String {
        if (isCurrent) return "Текущий релиз"
        if (category.equals("Фильм", ignoreCase = true)) return "Фильм"
        if (category.equals("OVA", ignoreCase = true)) return "OVA"
        if (category.equals("Спешл", ignoreCase = true)) return "Спешл"

        val t = title.lowercase()
        if (t.contains("финал") || t.contains("заключительная")) return "Финал"
        if (t.contains(" 2") || t.contains(" 2:") || t.contains("2 сезон")) return "2 сезон"
        if (t.contains(" 3") || t.contains(" 3:") || t.contains("3 сезон")) return "3 сезон"
        if (t.contains(" 4") || t.contains(" 4:") || t.contains("4 сезон")) return "4 сезон"
        if (t.contains("рекап")) return "Рекап"

        return when {
            yearInt < currentYear -> "Предыстория"
            yearInt > currentYear -> "Продолжение"
            else -> "Сериал"
        }
    }

    fun getGenres(): List<ShikimoriGenreDto> {
        return genresList
    }

    private fun <T> paginateList(list: List<T>, page: Int, limit: Int): List<T> {
        val safePage = if (page < 1) 1 else page
        val fromIndex = (safePage - 1) * limit
        if (fromIndex >= list.size) return emptyList()
        val toIndex = (fromIndex + limit).coerceAtMost(list.size)
        return list.subList(fromIndex, toIndex)
    }

    private fun cleanSearchQuery(raw: String): String {
        return raw
            .replace(Regex("(?i)\\b(1|2|3|4|5)\\s*сезон\\b"), "")
            .replace(Regex("(?i)\\bсезон\\s*(1|2|3|4|5)\\b"), "")
            .replace(Regex("(?i)\\bчасть\\s*(1|2|3|4)\\b"), "")
            .replace(Regex("[.,:;!?'\"()\\[\\]{}]"), " ")
            .trim()
            .replace(Regex("\\s+"), " ")
    }
}
