package com.example.listacomprasnova.util

import android.util.Patterns

object EmailValidator {
    fun isValid(email: CharSequence?): Boolean {
        return !email.isNullOrEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
}