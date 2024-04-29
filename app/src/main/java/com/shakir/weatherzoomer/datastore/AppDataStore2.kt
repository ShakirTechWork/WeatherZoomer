package com.shakir.weatherzoomer.datastore

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.shakir.weatherzoomer.utils.Utils
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.single

class AppDataStore2(context: Context) {

    private val Context.dataStore by preferencesDataStore("user_preferences")
    private val dataStore = context.dataStore

    companion object {
        val USER_EMAIL_KEY = stringPreferencesKey("USER_EMAIL")
        val USER_NAME_KEY = stringPreferencesKey("USER_NAME")
        val USER_TOKEN_KEY = stringPreferencesKey("USER_TOKEN")
        val USER_PRIMARY_LOCATION = stringPreferencesKey("USER_PRIMARY_LOCATION")
    }

    suspend fun storeUser(name: String, email: String, token: String) {
        dataStore.edit {
            it[USER_NAME_KEY] = name
            it[USER_EMAIL_KEY] = email
            it[USER_TOKEN_KEY] = token
        }
    }

    suspend fun savePrimaryLocation(primaryLocation: String) {
        dataStore.edit {
            it[USER_PRIMARY_LOCATION] = primaryLocation
        }
    }

    suspend fun getUserAge(): String {
        return dataStore.data.map {
            it[USER_EMAIL_KEY] ?: ""
        }.single()
    }

    suspend fun getUserName(): String {
        return dataStore.data.map {
            it[USER_NAME_KEY] ?: ""
        }.single()
    }

    suspend fun getUserToken(): String {
        return dataStore.data.map {
            it[USER_TOKEN_KEY] ?: ""
        }.single()
    }

    suspend fun getUserPrimaryLocation(): String {
//        return try {
//            Utils.printDebugLog("jhgjhc")
//            dataStore.data.map {
//                it[USER_PRIMARY_LOCATION] ?: ""
//            }.single("default_value")
//        } catch (e: Exception) {
//            Utils.printErrorLog("error_happened ${e}")
//            ""
//        }

        return try {
            dataStore.data.map {
                it[USER_PRIMARY_LOCATION] ?: ""
            }.single()  // Corrected: Remove the default value argument
        } catch (e: NoSuchElementException) {
            // Handle the case where the flow completes without emitting a value
            Utils.printErrorLog("Flow_completed_without_emitting_a_value.")
            ""
        } catch (e: Exception) {
            Utils.printErrorLog("Error_occurred: $e")
            ""
        }
    }
}
