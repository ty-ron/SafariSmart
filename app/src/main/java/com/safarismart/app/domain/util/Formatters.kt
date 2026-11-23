package com.safarismart.app.domain.util

import java.util.Locale

/** 1250.0 -> "KES 1,250" */
fun Double.toKes(): String = "KES " + String.format(Locale.US, "%,.0f", this)