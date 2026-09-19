package com.gfgm.veofon.model

import androidx.annotation.DrawableRes

// Data model for a contact
data class DirectContact(
    val name: String, // full name or first name, depending the context
    val number: String,
    val role: String? = null, // relation to phone owner: dad, mum, son, daughter, ...
    @DrawableRes val photoResId: Int? = null,
    val photoUri: String? = null
)