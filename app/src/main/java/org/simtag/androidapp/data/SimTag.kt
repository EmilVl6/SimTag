package org.simtag.androidapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sim_tags")
data class SimTag(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    var tagName: String,
    var fullName: String? = null,
    var phoneNumber: String? = null,
    var email: String? = null,
    var linkedInProfileUrl: String? = null,
    var twitterHandle: String? = null,
    var websiteUrl: String? = null,
    var customNotes: String? = null,
    var isDefaultPersonal: Boolean = false,
    var isDefaultWork: Boolean = false,
    var color: Int = 0xFF2196F3.toInt()
)