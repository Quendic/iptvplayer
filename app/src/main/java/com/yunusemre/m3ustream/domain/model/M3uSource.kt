package com.yunusemre.m3ustream.domain.model

import android.net.Uri

sealed interface M3uSource {
    data class Url(val url: String) : M3uSource
    data class File(val uri: Uri) : M3uSource
}
