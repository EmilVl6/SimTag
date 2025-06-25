package org.simtag.androidapp.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface SimTagDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSimTag(tag: SimTag): Long

    @Update
    suspend fun updateSimTag(tag: SimTag)

    @Delete
    suspend fun deleteSimTag(tag: SimTag)

    @Query("SELECT * FROM sim_tags WHERE id = :tagId")
    fun getSimTagById(tagId: Int): Flow<SimTag?>

    @Query("SELECT * FROM sim_tags ORDER BY tagName ASC")
    fun getAllSimTags(): Flow<List<SimTag>>

    @Query("SELECT * FROM sim_tags WHERE isDefaultPersonal = 1 LIMIT 1")
    fun getDefaultPersonalTag(): Flow<SimTag?>

    @Query("SELECT * FROM sim_tags WHERE isDefaultWork = 1 LIMIT 1")
    fun getDefaultWorkTag(): Flow<SimTag?>

    @Query("DELETE FROM sim_tags")
    suspend fun deleteAllSimTags()
}