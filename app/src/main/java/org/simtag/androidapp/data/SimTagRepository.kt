package org.simtag.androidapp.data

import kotlinx.coroutines.flow.Flow

class SimTagRepository(private val simTagDao: SimTagDao) {

    val allSimTags: Flow<List<SimTag>> = simTagDao.getAllSimTags()

    fun getSimTagById(id: Int): Flow<SimTag?> {
        return simTagDao.getSimTagById(id)
    }

    suspend fun insert(tag: SimTag) {
        simTagDao.insertSimTag(tag)
    }

    suspend fun update(tag: SimTag) {
        simTagDao.updateSimTag(tag)
    }

    suspend fun delete(tag: SimTag) {
        simTagDao.deleteSimTag(tag)
    }

    fun getDefaultPersonalTag(): Flow<SimTag?> = simTagDao.getDefaultPersonalTag()

    fun getDefaultWorkTag(): Flow<SimTag?> = simTagDao.getDefaultWorkTag()
}