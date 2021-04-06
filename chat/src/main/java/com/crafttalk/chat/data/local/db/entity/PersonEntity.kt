package com.crafttalk.chat.data.local.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity

@Entity(tableName = "persons", primaryKeys = ["person_id"])
data class PersonEntity(
    @ColumnInfo(name = "person_id")
    val personId: String,
    @ColumnInfo(name = "person_preview")
    val personPreview: String
)