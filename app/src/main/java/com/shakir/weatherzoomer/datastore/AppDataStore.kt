package com.shakir.weatherzoomer.datastore

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.shakir.weatherzoomer.model.UserModel
import com.shakir.weatherzoomer.utils.Utils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map


class AppDataStore(context: Context) {

    // Create the dataStore and give it a name same as shared preferences
//    private val dataStore = context.createDataStore(name = "user_prefs")

    private val Context.dataStore by preferencesDataStore("user_preferences")
    private val dataStore = context.dataStore

    // Create some keys we will use them to store and retrieve the data
    companion object {

        @Volatile
        private var instance: AppDataStore? = null

        fun getInstance(context: Context): AppDataStore {
            return instance ?: synchronized(this) {
                instance ?: AppDataStore(context).also { instance = it }
            }
        }

        val IS_APP_OPENED_FIRST_TIME = booleanPreferencesKey("IS_APP_OPENED_FIRST_TIME")
        val USER_EMAIL_KEY = stringPreferencesKey("USER_EMAIL")
        val USER_NAME_KEY =stringPreferencesKey("USER_NAME")
        val USER_TOKEN_KEY = stringPreferencesKey("USER_TOKEN")
        val USER_PRIMARY_LOCATION = stringPreferencesKey("USER_PRIMARY_LOCATION")

    }

    // Store user data
    // refer to the data store and using edit
    // we can store values using the keys
    suspend fun storeUser(name: String, email: String, token: String) {
        dataStore.edit {
            it[USER_NAME_KEY] = name
            it[USER_EMAIL_KEY] = email
            it[USER_TOKEN_KEY] = token

            // here it refers to the preferences we are editing

        }
    }

    suspend fun updateIsAppOpenedFirstTime(value: Boolean) {
        dataStore.edit {
            it[IS_APP_OPENED_FIRST_TIME] = value
        }
    }

    suspend fun savePrimaryLocation(primaryLocation: String) {
        dataStore.edit {
            it[USER_PRIMARY_LOCATION] = primaryLocation
            Utils.printDebugLog("Stored primary location locally.")
        }
    }

    val isAppOpenedFirstTime: Flow<Boolean> = dataStore.data.map {
        it[IS_APP_OPENED_FIRST_TIME] ?: false
    }

    // Create an age flow to retrieve age from the preferences
    // flow comes from the kotlin coroutine
    val userAgeFlow: Flow<String> = dataStore.data.map {
        it[USER_EMAIL_KEY] ?: ""
    }

    // Create a name flow to retrieve name from the preferences
    val userNameFlow: Flow<String> = dataStore.data.map {
        it[USER_NAME_KEY] ?: ""
    }


    val userTokenFlow: Flow<String> = dataStore.data.map {
        it[USER_TOKEN_KEY] ?: ""
    }

    val userPrimaryLocation: Flow<String> = dataStore.data.map {
        it[USER_PRIMARY_LOCATION] ?: ""
    }

    // Function to get all user data
    val userStoredData: Flow<UserModel> = dataStore.data.map {
        val name = it[USER_NAME_KEY] ?: ""
        val email = it[USER_EMAIL_KEY] ?: ""
        val token = it[USER_TOKEN_KEY] ?: ""
        val primaryLocation = it[USER_PRIMARY_LOCATION] ?: ""

        val userModel = UserModel(name, email, token)
        Log.d("WEATHER_ZOOMER_LOG", "Emitting userStoredData: $userModel")
        userModel
    }


    // Function to remove the entire data store
    suspend fun deleteAllUserData() {
        dataStore.edit {
            it.clear()
        }
    }

    // Function to remove an individual preference
    suspend fun clearPrimaryLocation() {
        dataStore.edit {
            it.remove(USER_PRIMARY_LOCATION)
        }
    }

    // Function to remove all individual preferences
    suspend fun clearAllUserPreferences() {
        dataStore.edit {
            it.remove(USER_EMAIL_KEY)
            it.remove(USER_NAME_KEY)
            it.remove(USER_TOKEN_KEY)
            it.remove(USER_PRIMARY_LOCATION)
        }
    }

}