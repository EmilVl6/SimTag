package org.simtag.androidapp.data

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FirestoreTagRepository {
    private val db = FirebaseFirestore.getInstance()
    private val tagsCollection = db.collection("tags")

    suspend fun addOrUpdateTag(tag: FirestoreSimTag): String {
        val docRef = if (tag.id.isBlank()) tagsCollection.document() else tagsCollection.document(tag.id)
        val tagWithId = tag.copy(id = docRef.id)
        docRef.set(tagWithId).await()
        return docRef.id
    }

    suspend fun deleteTag(tagId: String) {
        tagsCollection.document(tagId).delete().await()
    }

    suspend fun getTag(tagId: String): FirestoreSimTag? {
        val doc = tagsCollection.document(tagId).get().await()
        return doc.toObject(FirestoreSimTag::class.java)
    }

    suspend fun getAllTags(): List<FirestoreSimTag> {
        val snapshot = tagsCollection.get().await()
        return snapshot.toObjects(FirestoreSimTag::class.java)
    }
}