package com.gfgm.veofon.model

import androidx.annotation.DrawableRes

// Data model for a contact
data class DirectContact(
    val name: String,
    val number: String,
    val role: String,
    @DrawableRes val photoResId: Int? = null
)