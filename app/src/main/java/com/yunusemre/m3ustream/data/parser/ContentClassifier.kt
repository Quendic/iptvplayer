package com.yunusemre.m3ustream.data.parser

import com.yunusemre.m3ustream.data.local.entity.ContentEntity
import com.yunusemre.m3ustream.data.local.entity.SeriesGroupEntity
import java.security.MessageDigest
import javax.inject.Inject

class ContentClassifier @Inject constructor() {

    fun classify(items: List<M3uItem>): Pair<List<ContentEntity>, List<SeriesGroupEntity>> {
        val contentList = mutableListOf<ContentEntity>()
        val seriesGroupsMap = mutableMapOf<String, SeriesGroupEntity>()

        // 1) S01E01 / S1 E1 / S01.E01 / S1-E1 / [S01E01]
        val pattern1 = Regex("""^(.*?)(?:[\s._-]+|[\[(])S(\d{1,2})[\s._-]*E(\d{1,3})""", RegexOption.IGNORE_CASE)
        // 2) 1. Sezon 2. Bölüm / 1.Sezon 2.Bolum
        val pattern2 = Regex("""^(.*?)\s+(\d{1,2})\.\s*Sezon\s+(\d{1,3})\.\s*(?:Bölüm|Bolum)""", RegexOption.IGNORE_CASE)
        // 3) 1x01 / 01x01
        val pattern3 = Regex("""^(.*?)(?:[\s._-]+|[\[(])(\d{1,2})x(\d{1,3})""", RegexOption.IGNORE_CASE)

        val currentTime = System.currentTimeMillis()

        for (item in items) {
            val name = item.name.trim()
            val url = item.url.trim()
            if (url.isEmpty()) continue

            val id = md5(url)

            var seriesName: String? = null
            var seasonNumber: Int? = null
            var episodeNumber: Int? = null

            val match1 = pattern1.find(name)
            val match2 = pattern2.find(name)
            val match3 = pattern3.find(name)

            if (match1 != null) {
                seriesName = match1.groupValues[1].cleanSeriesName()
                seasonNumber = match1.groupValues[2].toIntOrNull()
                episodeNumber = match1.groupValues[3].toIntOrNull()
            } else if (match2 != null) {
                seriesName = match2.groupValues[1].cleanSeriesName()
                seasonNumber = match2.groupValues[2].toIntOrNull()
                episodeNumber = match2.groupValues[3].toIntOrNull()
            } else if (match3 != null) {
                seriesName = match3.groupValues[1].cleanSeriesName()
                seasonNumber = match3.groupValues[2].toIntOrNull()
                episodeNumber = match3.groupValues[3].toIntOrNull()
            }

            val isSeries = (seriesName != null && seasonNumber != null) ||
                    url.contains("/series/") ||
                    item.groupTitle.contains("dizi", ignoreCase = true) ||
                    item.groupTitle.contains("series", ignoreCase = true) ||
                    item.groupTitle.contains("sezon", ignoreCase = true)

            val type = if (isSeries) "series" else "movie"

            var category = ""
            val groups = item.groupTitle.split(";").map { it.trim() }
            if (groups.isNotEmpty() && groups[0].isNotBlank()) {
                category = groups[0]
            } else {
                category = if (isSeries) "Diziler" else "Filmler"
            }

            if (seriesName == null && isSeries) {
                if (groups.size >= 2 && groups[1].isNotBlank()) {
                    seriesName = groups[1].cleanSeriesName()
                } else {
                    seriesName = name
                }
            }

            if (isSeries && seriesName != null) {
                val s = seasonNumber ?: 1
                val existing = seriesGroupsMap[seriesName]
                val maxS = maxOf(existing?.totalSeasons ?: 1, s)
                val totalEps = (existing?.totalEpisodes ?: 0) + 1
                val poster = existing?.posterUrl ?: item.tvgLogo.ifEmpty { null }

                seriesGroupsMap[seriesName] = SeriesGroupEntity(
                    seriesName = seriesName,
                    category = category,
                    posterUrl = poster,
                    tmdbPosterUrl = existing?.tmdbPosterUrl,
                    totalSeasons = maxS,
                    totalEpisodes = totalEps
                )
            }

            contentList.add(
                ContentEntity(
                    id = id,
                    title = name,
                    originalName = item.tvgName.ifBlank { name },
                    type = type,
                    category = category,
                    seriesName = seriesName,
                    seasonNumber = seasonNumber,
                    episodeNumber = episodeNumber,
                    streamUrl = url,
                    posterUrl = item.tvgLogo.ifEmpty { null },
                    tmdbPosterUrl = null,
                    groupTitle = item.groupTitle,
                    parsedAt = currentTime
                )
            )
        }
        return Pair(contentList, seriesGroupsMap.values.toList())
    }

    private fun String.cleanSeriesName(): String {
        return this.replace(Regex("""[-._:\[\]()]+$"""), "").trim()
    }

    private fun md5(input: String): String {
        val md = MessageDigest.getInstance("MD5")
        return md.digest(input.toByteArray()).joinToString("") { "%02x".format(it) }
    }
}
