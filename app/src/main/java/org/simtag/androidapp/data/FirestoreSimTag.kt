package org.simtag.androidapp.data

data class FirestoreSimTag(
    val id: String = "",
    val tagName: String = "",
    val fullName: String? = null,
    val email: String? = null,
    val phoneNumber: String? = null,
    val color: Int = 0xFF828148.toInt()
)