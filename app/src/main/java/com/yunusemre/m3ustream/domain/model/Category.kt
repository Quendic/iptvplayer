package com.yunusemre.m3ustream.domain.model

import androidx.compose.runtime.Immutable

@Immutable
data class Category(
    val name: String,
    val contents: List<Content> = emptyList()
)
