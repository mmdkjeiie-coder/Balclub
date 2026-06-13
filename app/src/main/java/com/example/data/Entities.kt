package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val role: Role,
    val isActive: Boolean = true
)

enum class Role {
    Admin, Adhyakshya, Upadhyakshya, Sachiv, Sahasachiv, Koshadhyakshya, Sadasya, Pradhanadhyapak, Samrakshyak
}

fun Role.toNepaliDisplay(): String {
    return when (this) {
        Role.Adhyakshya -> "अध्यक्ष"
        Role.Upadhyakshya -> "उपाध्यक्ष"
        Role.Sachiv -> "सचिव"
        Role.Sahasachiv -> "सहसचिव"
        Role.Koshadhyakshya -> "कोषाध्यक्ष"
        Role.Sadasya -> "सदस्य"
        Role.Pradhanadhyapak -> "प्रधानाध्यापक"
        Role.Samrakshyak -> "संरक्षक"
        Role.Admin -> "Admin"
    }
}

@Entity(tableName = "minutes")
data class Minute(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val template: MinuteTemplate,
    val title: String,
    val date: Long,
    val chairperson: String,
    val attendees: String,
    val agenda: String,
    val discussion: String,
    val decisions: String,
    val summary: String
)

enum class MinuteTemplate {
    Standard, EventPlanning, Emergency, DailyActivity, Custom
}

@Entity(tableName = "events")
data class Event(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val date: Long,
    val status: EventStatus,
    val assignedTo: String
)

enum class EventStatus {
    Pending, Ongoing, Completed, Cancelled
}

@Entity(tableName = "notices")
data class Notice(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val content: String,
    val date: Long,
    val isPinned: Boolean
)

data class Suggestion(
    val id: String = "",
    val text: String = "",
    val author: String = "",
    val date: Long = 0L,
    val upvotes: Int = 0
)

data class PhotoAlbumUrl(
    val id: String = "",
    val url: String = "",
    val caption: String = "",
    val uploadedBy: String = "",
    val date: Long = 0L
)
