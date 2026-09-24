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

        val extInfRegex = Regex("""#EXTINF:([^,]*),(.*)""")
        val tvgIdRegex = Regex("""tvg-id="([^"]*)"""")
        val tvgNameRegex = Regex("""tvg-name="([^"]*)"""")
        val tvgLogoRegex = Regex("""tvg-logo="([^"]*)"""")
        val groupTitleRegex = Regex("""group-title="([^"]*)"""")

        inputStream.bufferedReader().useLines { lines ->
            for (line in lines) {
                val trimmed = line.trim()
                if (trimmed.isEmpty()) continue
                
                if (trimmed.startsWith("#EXTINF:")) {
                    val commaIndex = trimmed.lastIndexOf(',')
                    if (commaIndex != -1) {
                        val attrs = trimmed.substring(8, commaIndex)
                        name = trimmed.substring(commaIndex + 1).trim()
                        
                        tvgId = tvgIdRegex.find(attrs)?.groupValues?.get(1) ?: ""
                        tvgName = tvgNameRegex.find(attrs)?.groupValues?.get(1) ?: ""
                        tvgLogo = tvgLogoRegex.find(attrs)?.groupValues?.get(1) ?: ""
                        groupTitle = groupTitleRegex.find(attrs)?.groupValues?.get(1) ?: ""
                        
                        val durMatch = Regex("""^#EXTINF:([-0-9]+)""").find(trimmed)
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
}
