package com.example.criminal_intent

import android.content.Context
import androidx.room.Room
import com.example.criminal_intent.database.CrimeDatabase
import java.util.*

private val DATABASE_NAME = "crime-database"

class CrimeRepository private constructor(context: Context) {

    private val database: CrimeDatabase = Room.databaseBuilder(
        context.applicationContext,
        CrimeDatabase::class.java,
        DATABASE_NAME
    ).build()

    private val crimeDao = database.crimeDao()

    fun getCrimes(): List<Crime> = crimeDao.getCrimes()
    fun getCrime(id: UUID): Crime? = crimeDao.getCrime(id)

    companion object {
        private var instance: CrimeRepository? = null

        fun initialize(context: Context) {
            if (instance == null) {
                instance = CrimeRepository(context)
            }
        }

        fun get(): CrimeRepository {
            return instance ?:
                throw IllegalStateException("CrimeRepository must be initialized")
        }
    }
}