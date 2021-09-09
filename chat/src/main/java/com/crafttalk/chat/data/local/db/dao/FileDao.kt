package com.crafttalk.chat.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.crafttalk.chat.data.local.db.entity.FileEntity

@Dao
interface FileDao {

    @Query("SELECT file_name FROM files")
    fun getFilesNames(): List<String>

    @Insert
    fun addFile(file: FileEntity)

    @Query("DELETE FROM files WHERE file_name = :fileName")
    fun deleteFile(fileName: String)

}