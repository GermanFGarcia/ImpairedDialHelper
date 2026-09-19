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
        "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} ASC"
    )

    cursor?.use {
        val nameIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
        val numberIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)

        while (it.moveToNext()) {
            val name = it.getString(nameIndex) ?: "---"
            val number = it.getString(numberIndex) ?: "---"

            if (number.isNotEmpty()) {
                contactsList.add(
                    DirectContact(
                        name = name,
                        number = number
                    )
                )
            }
        }
    }

    return contactsList
}

fun fetchFavoriteContacts(context: Context): List<DirectContact> {
    val contactsList = mutableListOf<DirectContact>()

    if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS)
        != PackageManager.PERMISSION_GRANTED
    ) {
        return contactsList
    }

    val contentResolver = context.contentResolver

    // SELECT filter: Only contacts marked as STARRED (favorites)
    val selection = "${ContactsContract.CommonDataKinds.Phone.STARRED} = ?"
    val selectionArgs = arrayOf("1")

    val cursor = contentResolver.query(
        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
        arrayOf(
            ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.NUMBER,
            ContactsContract.CommonDataKinds.Phone.PHOTO_URI
        ),
        selection,
        selectionArgs,
        "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} ASC"
    )

    cursor?.use {
        val contactIdIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID)
        val nameIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
        val numberIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
        val photoIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_URI)

        while (it.moveToNext()) {
            val contactId = it.getString(contactIdIndex) ?: ""
            val name = it.getString(nameIndex) ?: "---"
            val number = it.getString(numberIndex) ?: "---"
            // Retrieves photo URI string if available, null otherwise
            val photoUriString = it.getString(photoIndex)

            val nickname = if (contactId.isBlank()) null else fetchSystemNickname(context, contactId)
            val effectiveName = if (nickname.isNullOrBlank()) name.trim().substringBefore(" ") else nickname

            val role = fetchRelationToOwner(context, contactId)

            if (number.isNotEmpty()) {
                contactsList.add(
                    DirectContact(
                        name = effectiveName,
                        number = number,
                        role = role,
                        photoUri = photoUriString
                    )
                )
            }
        }
    }

    return contactsList
}

fun fetchSystemNickname(context: Context, contactId: String): String? {
    val selection = "${ContactsContract.Data.CONTACT_ID} = ? AND ${ContactsContract.Data.MIMETYPE} = ?"
    val selectionArgs = arrayOf(
        contactId,
        ContactsContract.CommonDataKinds.Nickname.CONTENT_ITEM_TYPE
    )

    val cursor = context.contentResolver.query(
        ContactsContract.Data.CONTENT_URI,
        arrayOf(ContactsContract.CommonDataKinds.Nickname.NAME),
        selection,
        selectionArgs,
        null
    )

    var nickname: String? = null
    cursor?.use {
        if (it.moveToFirst()) {
            val index = it.getColumnIndex(ContactsContract.CommonDataKinds.Nickname.NAME)
            nickname = it.getString(index)
        }
    }
    return nickname?.takeIf { it.isNotBlank() }
}

fun fetchRelationToOwner(context: Context, contactId: String): String? {
    val contentResolver = context.contentResolver

    // Step 1: Get the owner's name from the Profile API
    val ownerName = getOwnerName(context) ?: return null

    // Step 2: Query Relations for this contact
    val selection = "${ContactsContract.Data.CONTACT_ID} = ? AND ${ContactsContract.Data.MIMETYPE} = ?"
    val selectionArgs = arrayOf(
        contactId,
        ContactsContract.CommonDataKinds.Relation.CONTENT_ITEM_TYPE
    )

    val cursor = contentResolver.query(
        ContactsContract.Data.CONTENT_URI,
        arrayOf(
            ContactsContract.CommonDataKinds.Relation.NAME, // Person name (e.g., Owner's name)
            ContactsContract.CommonDataKinds.Relation.TYPE, // Int type (TYPE_CHILD, TYPE_SPOUSE, etc.)
            ContactsContract.CommonDataKinds.Relation.LABEL  // Custom string label if TYPE_CUSTOM
        ),
        selection,
        selectionArgs,
        null
    )

    var relationTypeLabel: String? = null

    cursor?.use {
        val personNameIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Relation.NAME)
        val typeIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Relation.TYPE)
        val labelIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Relation.LABEL)

        while (it.moveToNext()) {
            val targetPersonName = it.getString(personNameIndex) ?: continue

            // Match if the relation name contains/matches the owner's name
            if (targetPersonName.contains(ownerName, ignoreCase = true)) {
                val type = it.getInt(typeIndex)
                val customLabel = it.getString(labelIndex)

                // Get string representation of the relation type
                relationTypeLabel = getRelationLabel(context, type, customLabel)
                break
            }
        }
    }

    return relationTypeLabel
}

// Helper: Get phone owner's display name
private fun getOwnerName(context: Context): String? {
    val cursor = context.contentResolver.query(
        ContactsContract.Profile.CONTENT_URI,
        arrayOf(ContactsContract.Profile.DISPLAY_NAME),
        null, null, null
    )
    cursor?.use {
        if (it.moveToFirst()) {
            return it.getString(it.getColumnIndex(ContactsContract.Profile.DISPLAY_NAME))
        }
    }
    return null
}

// Helper: Resolve integer type to label string
private fun getRelationLabel(context: Context, type: Int, customLabel: String?): String {
    val res = context.resources
    val labelRes = ContactsContract.CommonDataKinds.Relation.getTypeLabelResource(type)
    val defaultLabel = res.getString(labelRes).toString()

    return if (type == ContactsContract.CommonDataKinds.Relation.TYPE_CUSTOM && !customLabel.isNullOrBlank()) {
        customLabel
    } else {
        defaultLabel
    }
}