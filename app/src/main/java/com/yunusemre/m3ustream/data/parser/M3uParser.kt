package com.yunusemre.m3ustream.data.parser

import java.io.InputStream
import javax.inject.Inject

class M3uParser @Inject constructor() {
    fun parse(inputStream: InputStream): List<M3uItem> {
        val items = mutableListOf<M3uItem>()
        var tvgId = ""
        var tvgName = ""
        var tvgLogo = ""
        var groupTitle = ""
        var name = ""
        var duration = ""



        inputStream.bufferedReader().useLines { lines ->
            for (line in lines) {
                val trimmed = line.trim()
                if (trimmed.isEmpty()) continue
                
                if (trimmed.startsWith("#EXTINF:")) {
                    val commaIndex = trimmed.lastIndexOf(',')
                    if (commaIndex != -1) {
                        val attrs = trimmed.substring(8, commaIndex)
                        name = trimmed.substring(commaIndex + 1).trim()
                        
                        tvgId = TVG_ID_REGEX.find(attrs)?.groupValues?.get(1) ?: ""
                        tvgName = TVG_NAME_REGEX.find(attrs)?.groupValues?.get(1) ?: ""
                        tvgLogo = TVG_LOGO_REGEX.find(attrs)?.groupValues?.get(1) ?: ""
                        groupTitle = GROUP_TITLE_REGEX.find(attrs)?.groupValues?.get(1) ?: ""
                        
                        val durMatch = DUR_REGEX.find(trimmed)
                        duration = durMatch?.groupValues?.get(1) ?: "-1"
                        
                        if (name.isEmpty() && tvgName.isNotEmpty()) {
                            name = tvgName
                        }
                    }
                } else if (!trimmed.startsWith("#")) {
                    if (name.isNotEmpty() && trimmed.isNotEmpty()) {
                        items.add(
                            M3uItem(
                                name = name,
                                url = trimmed,
                                tvgId = tvgId,
                                tvgName = tvgName,
                                tvgLogo = tvgLogo,
                                groupTitle = groupTitle,
                                duration = duration
                            )
                        )
                        // Reset for next
                        name = ""
                        tvgId = ""
                        tvgName = ""
                        tvgLogo = ""
                        groupTitle = ""
                        duration = ""
                    }
                }
            }
        }
        return items
    }

    companion object {
        private val EXT_INF_REGEX = Regex("""#EXTINF:([^,]*),(.*)""")
        private val TVG_ID_REGEX = Regex("""tvg-id="([^"]*)"""")
        private val TVG_NAME_REGEX = Regex("""tvg-name="([^"]*)"""")
        private val TVG_LOGO_REGEX = Regex("""tvg-logo="([^"]*)"""")
        private val GROUP_TITLE_REGEX = Regex("""group-title="([^"]*)"""")
        private val DUR_REGEX = Regex("""^#EXTINF:([-0-9]+)""")
    }
}
