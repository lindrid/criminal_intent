package com.example.criminal_intent

import android.content.res.Resources
import java.text.SimpleDateFormat
import java.util.*

private const val DATE_FORMAT = "EEE, MM, dd"

fun getCurrentLocaleDateString(resources: Resources, date: Date): String {
  val currentLocale = resources.configuration.locales[0]
  val sdf = SimpleDateFormat(DATE_FORMAT, currentLocale)
  return sdf.format(date)
}