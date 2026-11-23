package com.safarismart.app.domain.util

/** One nullable message per field. null = that field is valid. */
data class TripFormErrors(
    val origin: String? = null,
    val destination: String? = null,
    val sacco: String? = null,
    val fare: String? = null,
    val delay: String? = null
) {
    val hasErrors: Boolean
        get() = listOf(origin, destination, sacco, fare, delay).any { it != null }
}

object Validators {
    fun validateStage(text: String, label: String): String? = when {
        text.isBlank() -> "$label cannot be empty"
        text.trim().length < 2 -> "$label is too short"
        else -> null
    }

    fun validateDifferent(origin: String, destination: String): String? =
        if (origin.trim().equals(destination.trim(), ignoreCase = true))
            "Origin and destination must be different" else null

    fun validateFare(text: String): String? {
        val fare = text.trim().toDoubleOrNull() ?: return "Enter a valid fare"
        return when {
            fare <= 0.0 -> "Fare must be more than 0"
            fare > 5000.0 -> "Fare looks too high (max 5000)"
            else -> null
        }
    }

    /** Delay is optional: blank means "on time". */
    fun validateDelay(text: String): String? {
        if (text.isBlank()) return null
        val minutes = text.trim().toIntOrNull() ?: return "Enter whole minutes"
        return when {
            minutes < 0 -> "Delay cannot be negative"
            minutes > 240 -> "Delay cannot exceed 240 minutes"
            else -> null
        }
    }
}