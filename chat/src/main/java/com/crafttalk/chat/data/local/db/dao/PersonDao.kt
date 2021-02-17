package com.crafttalk.chat.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.crafttalk.chat.data.local.db.entity.Person

@Dao
interface PersonDao {

    @Query("SELECT person_preview FROM persons WHERE person_id = :personId")
    fun getPersonPreview(personId: String): String?

    @Insert
    fun addPersonPreview(person: Person)

}