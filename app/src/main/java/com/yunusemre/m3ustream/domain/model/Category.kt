package com.yunusemre.m3ustream.domain.model

data class Category(
    val name: String,
    val contents: List<Content> = emptyList()
)
