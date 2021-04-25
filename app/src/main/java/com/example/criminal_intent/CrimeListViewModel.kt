package com.example.criminal_intent

import androidx.lifecycle.ViewModel

class CrimeListViewModel : ViewModel() {
    var crimes = mutableListOf<Crime>()

    init {
        for (i in 0 until 100) {
            var crime = Crime()
            crime.title = "Crime #$i"
            crime.isSolved = i % 2 == 0
            crimes.plusAssign(crime)
        }
    }
}