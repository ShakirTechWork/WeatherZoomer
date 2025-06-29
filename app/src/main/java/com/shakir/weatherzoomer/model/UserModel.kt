package com.shakir.weatherzoomer.model
import com.shakir.weatherzoomer.constants.AppConstants

data class UserModel(
    val userId: String,
    val userPassword: String,
    val userSettings: UserSettingsModel = UserSettingsModel(AppConstants.UserPreferredUnit.METRIC)
) {
    // Default no-argument constructor required by Firebase
    constructor() : this("","")
}