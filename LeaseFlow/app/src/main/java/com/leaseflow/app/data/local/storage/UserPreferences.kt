package com.leaseflow.app.data.local.storage

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore by preferencesDataStore(name = "leaseflow_user_prefs")

data class UserSessionData(
    val isLoggedIn: Boolean = false,
    val userId: Long = 0L,
    val userEmail: String? = null,
    val userName: String? = null,
    val userRoleName: String? = null,
    val userRole: Int = 3,
    val isDuocVip: Boolean = false,
    val authToken: String? = null
)

class UserPreferences(private val context: Context) {

    // ========== KEYS ==========
    private val isLoggedInKey = booleanPreferencesKey("is_logged_in")
    private val userIdKey = longPreferencesKey("user_id")
    private val userEmailKey = stringPreferencesKey("user_email")
    private val userNameKey = stringPreferencesKey("user_name")
    private val userRoleKey = stringPreferencesKey("user_role")
    private val userRoleIdKey = intPreferencesKey("user_role_id")
    private val isDuocVipKey = booleanPreferencesKey("is_duoc_vip")
    private val authTokenKey = stringPreferencesKey("auth_token")

    // ========== SETTERS ==========
    suspend fun saveUserSession(
        userId: Long,
        email: String,
        name: String,
        role: String,
        roleId: Int,
        isDuocVip: Boolean
    ) {
        context.dataStore.edit { prefs ->
            prefs[isLoggedInKey] = true
            prefs[userIdKey] = userId
            prefs[userEmailKey] = email
            prefs[userNameKey] = name
            prefs[userRoleKey] = role
            prefs[userRoleIdKey] = roleId
            prefs[isDuocVipKey] = isDuocVip
        }
    }

    /**
     * Metodo de compatibilidad para codigo existente
     */
    suspend fun saveUserSession(
        userId: Long,
        email: String,
        name: String,
        role: String,
        isDuocVip: Boolean
    ) {
        // Mapear nombre de rol a ID
        val roleId = when (role.uppercase()) {
            "ADMIN", "ADMINISTRADOR" -> 1
            "PROPIETARIO" -> 2
            "ARRIENDATARIO" -> 3
            else -> 3
        }
        saveUserSession(userId, email, name, role, roleId, isDuocVip)
    }

    suspend fun clearUserSession() {
        context.dataStore.edit { prefs ->
            prefs.clear()
        }
    }

    suspend fun setAuthToken(token: String?) {
        context.dataStore.edit { prefs ->
            if (token.isNullOrBlank()) {
                prefs.remove(authTokenKey)
            } else {
                prefs[authTokenKey] = token
            }
        }
    }

    suspend fun setLoggedIn(value: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[isLoggedInKey] = value
        }
    }

    // ========== GETTERS ==========
    val isLoggedIn: Flow<Boolean> = context.dataStore.data
        .map { prefs -> prefs[isLoggedInKey] ?: false }

    val userId: Flow<Long?> = context.dataStore.data
        .map { prefs -> prefs[userIdKey] }

    val userEmail: Flow<String?> = context.dataStore.data
        .map { prefs -> prefs[userEmailKey] }

    val userName: Flow<String?> = context.dataStore.data
        .map { prefs -> prefs[userNameKey] }

    val userRole: Flow<String?> = context.dataStore.data
        .map { prefs -> prefs[userRoleKey] }

    val userRoleId: Flow<Int?> = context.dataStore.data
        .map { prefs ->
            prefs[userRoleIdKey] ?: prefs[userRoleKey]?.let { role ->
                // Mapeo de fallback si solo existe el nombre
                when (role.uppercase()) {
                    "ADMIN", "ADMINISTRADOR" -> 1
                    "PROPIETARIO" -> 2
                    "ARRIENDATARIO" -> 3
                    else -> 3
                }
            }
        }

    val isDuocVip: Flow<Boolean> = context.dataStore.data
        .map { prefs -> prefs[isDuocVipKey] ?: false }

    val authToken: Flow<String?> = context.dataStore.data
        .map { prefs -> prefs[authTokenKey] }

    val data: Flow<UserSessionData> = context.dataStore.data.map { prefs ->
        val roleName = prefs[userRoleKey]
        val roleId = prefs[userRoleIdKey] ?: roleName?.let { role ->
            when (role.uppercase()) {
                "ADMIN", "ADMINISTRADOR" -> 1
                "PROPIETARIO" -> 2
                "ARRIENDATARIO" -> 3
                else -> 3
            }
        } ?: 3

        UserSessionData(
            isLoggedIn = prefs[isLoggedInKey] ?: false,
            userId = prefs[userIdKey] ?: 0L,
            userEmail = prefs[userEmailKey],
            userName = prefs[userNameKey],
            userRoleName = roleName,
            userRole = roleId,
            isDuocVip = prefs[isDuocVipKey] ?: false,
            authToken = prefs[authTokenKey]
        )
    }
}
