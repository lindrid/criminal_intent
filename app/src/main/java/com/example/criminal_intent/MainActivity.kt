package com.example.criminal_intent

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import java.util.*

private const val TAG = "MainActivity"

class MainActivity : AppCompatActivity(), CrimeListFragment.Callbacks {
  override fun onCreate (savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    val currentFragment = supportFragmentManager.findFragmentById(R.id.fragment_container)

    if (currentFragment == null) {
      val fragment = CrimeListFragment.newInstance()
      supportFragmentManager
        .beginTransaction()
        .add(R.id.fragment_container, fragment)
        .commit()
    }
  }

  override fun onCrimeSelected (crimeId: UUID) {
    val crimeFragment = CrimeFragment.newInstance(crimeId)
    supportFragmentManager
      .beginTransaction()
      .replace(R.id.fragment_container, crimeFragment)
      .addToBackStack(null)
      .commit()
  }

  fun popupPictureFragment (pictureFragment: PictureFragment) {
    supportFragmentManager
      .beginTransaction()
      .replace(R.id.fragment_container, pictureFragment)
      .addToBackStack(null)
      .commit()
  }
}