package com.yunusemre.m3ustream.data.parser

data class M3uItem(
    val name: String,
    val url: String,
    val tvgId: String,
    val tvgName: String,
    val tvgLogo: String,
    val groupTitle: String,
    val duration: String
)
