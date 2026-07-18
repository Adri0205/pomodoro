package com.example.pomodoro.storage

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(
    name = "pomodoro_preferences"
)

class DataStoreManager(private val context: Context) {

    companion object {
        private val APP_DATA = stringPreferencesKey("app_data")
    }

    private val gson = Gson()

    suspend fun save(appData: AppData) {

        context.dataStore.edit {
            it[APP_DATA] = gson.toJson(appData)
        }
    }

    val appDataFlow: Flow<AppData> =

        context.dataStore.data.map {

            val json = it[APP_DATA]

            if (json.isNullOrEmpty()) {
                AppData()

            } else {
                gson.fromJson(
                    json,
                    AppData::class.java
                )
            }
        }
}