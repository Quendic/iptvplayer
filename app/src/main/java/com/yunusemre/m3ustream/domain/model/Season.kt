package com.yunusemre.m3ustream.domain.model

import androidx.compose.runtime.Immutable

@Immutable
data class Season(
    val number: Int,
    val episodes: List<Episode>
)
