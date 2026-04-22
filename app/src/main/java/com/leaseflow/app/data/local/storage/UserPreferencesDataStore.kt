package com.leaseflow.app.data.local.storage

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "leaseflow_prefs")

class UserPreferencesDataStore(
    private val context: Context,
) {
    private val authTokenKey = stringPreferencesKey("auth_token")

    val authToken: Flow<String?> = context.dataStore.data.map { prefs -> prefs[authTokenKey] }

    suspend fun setAuthToken(token: String?) {
        context.dataStore.edit { prefs ->
            if (token == null) {
                prefs.remove(authTokenKey)
            } else {
                prefs[authTokenKey] = token
            }
        }
    }
}
