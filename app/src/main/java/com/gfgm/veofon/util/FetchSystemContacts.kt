package com.gfgm.veofon.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.provider.ContactsContract
import androidx.core.content.ContextCompat
import com.gfgm.veofon.model.DirectContact

fun fetchSystemContacts(context: Context): List<DirectContact> {
    val contactsList = mutableListOf<DirectContact>()

    // Verifiquem si tenim permís abans de consultar
    if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS)
        != PackageManager.PERMISSION_GRANTED
    ) {
        return contactsList
    }

    val contentResolver = context.contentResolver
    val cursor = contentResolver.query(
        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
        arrayOf(
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.NUMBER
        ),
        null,
        null,
        "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} ASC" // Ordenat alfabèticament
    )

    cursor?.use {
        val nameIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
        val numberIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)

        while (it.moveToNext()) {
            val name = it.getString(nameIndex) ?: "Sense nom"
            val number = it.getString(numberIndex) ?: ""

            if (number.isNotEmpty()) {
                contactsList.add(
                    DirectContact(
                        name = name,
                        number = number,
                        role = "Contacte" // O etiqueta per defecte
                    )
                )
            }
        }
    }

    return contactsList
}