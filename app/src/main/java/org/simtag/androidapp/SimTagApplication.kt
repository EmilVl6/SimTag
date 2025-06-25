// In a new Kotlin file, e.g., SimTagApplication.kt
package org.simtag.androidapp // Your base package

import android.app.Application
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import org.simtag.androidapp.data.AppDatabase
import org.simtag.androidapp.data.SimTagRepository

class SimTagApplication : Application() {
    // No need to cancel this scope as it'll be torn down with the process
    private val applicationScope = CoroutineScope(SupervisorJob())

    // Using by lazy so the database and repository are only created when they're needed
    // rather than when the application starts
    val database by lazy { AppDatabase.getDatabase(this, applicationScope) }
    val repository by lazy { SimTagRepository(database.simTagDao()) }
}