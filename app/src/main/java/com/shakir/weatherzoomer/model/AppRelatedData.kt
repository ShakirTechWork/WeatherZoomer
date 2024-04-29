package com.shakir.weatherzoomer.model

import com.google.firebase.database.PropertyName

data class AppRelatedData(
    @get:PropertyName("app_play_store_link")
    @set:PropertyName("app_play_store_link")
    var app_play_store_link: String,
    @get:PropertyName("is_app_accessible")
    @set:PropertyName("is_app_accessible")
    var is_app_accessible: Boolean,
    @get:PropertyName("is_new_user_allowed")
    @set:PropertyName("is_new_user_allowed")
    var is_new_user_allowed: Boolean,
    @get:PropertyName("is_app_update_required")
    @set:PropertyName("is_app_update_required")
    var is_app_update_required: Boolean,
    @get:PropertyName("app_latest_version")
    @set:PropertyName("app_latest_version")
    var app_latest_version: String,
    @get:PropertyName("is_gemini_ai_accessible")
    @set:PropertyName("is_gemini_ai_accessible")
    var is_gemini_ai_accessible: Boolean,
    @get:PropertyName("is_weather_api_accessible")
    @set:PropertyName("is_weather_api_accessible")
    var is_weather_api_accessible: Boolean,
    @get:PropertyName("is_any_action_allowed")
    @set:PropertyName("is_any_action_allowed")
    var is_any_action_allowed: Boolean
) {
    // Default no-argument constructor required by Firebase
    constructor() : this("",false, false, false, "", false, false, false)
}