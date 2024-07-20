package com.shakir.weatherzoomer.constants

object AppConstants {

    object Other {
        const val WEATHER_API_ATTRIBUTION_URL = "https://www.weatherapi.com"
        const val ACCOUNT_DELETION_REQUEST_FORM_URL = "https://forms.gle/SZFGCDthvWNdX5SF8"
    }
    object Units {
        const val DEGREE_CELSIUS = "°C"
        const val DEGREE_FAHRENHEIT = "°F"
        const val KILOMETERS_PER_HOUR = "km/h"
        const val MILES_PER_HOUR = "miles/h"
        const val MILLIMETERS = "mm"
        const val INCHES = "in"
        const val KILOMETERS = "km"
        const val MILES = "miles"
        const val MILLIBARS = "mb"
        const val INCHES_OF_MERCURY = "inHg"
    }

    object MoonPhases {
        const val NEW_MOON = "New Moon"
        const val WAXING_CRESCENT = "Waxing Crescent"
        const val FIRST_QUARTER = "First Quarter"
        const val WAXING_GIBBOUS = "Waxing Gibbous"
        const val FULL_MOON = "Full Moon"
        const val WANING_GIBBOUS = "Waning Gibbous"
        const val LAST_QUARTER = "Last Quarter"
        const val WANING_CRESCENT = "Waning Crescent"
    }

    object UserPreferredUnit {
        const val METRIC = "metric"
        const val IMPERIAL = "imperial"
    }

}
