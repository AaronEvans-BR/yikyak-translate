package com.yikyaktranslate.service.face

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Translation(
    val translatedText: String
)
