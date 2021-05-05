package com.example.criminal_intent

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.Observer
import java.util.*
import java.util.concurrent.Executors

private const val ARG_CRIME_ID = "crime_id"
private const val TAG = "CrimeFragment"

class CrimeFragment: Fragment() {
  private lateinit var crime: Crime
  private lateinit var titleField: EditText
  private lateinit var dateButton: Button
  private lateinit var solvedCheckBox: CheckBox
  private val crimeViewModel: CrimeViewModel by lazy {
    ViewModelProviders.of(this).get(CrimeViewModel::class.java)
  }

  companion object {
    fun newInstance(crimeId: UUID): CrimeFragment {
      val args = Bundle().apply {
        putSerializable(ARG_CRIME_ID, crimeId)
      }
      return CrimeFragment().apply {
        arguments = args
      }
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    crime = Crime()
    val crimeId = arguments?.getSerializable(ARG_CRIME_ID) as UUID
    crimeViewModel.loadCrime(crimeId)
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                            savedInstanceState: Bundle?): View? {
    val view = inflater.inflate(R.layout.fragment_crime, container, false)

    titleField      = view.findViewById(R.id.crime_title) as EditText
    dateButton      = view.findViewById(R.id.crime_date) as Button
    solvedCheckBox  = view.findViewById(R.id.crime_solved) as CheckBox

    dateButton.apply {
      text = crime.title.toString()
      isEnabled = false
    }

    return view
  }

  override fun onStart() {
    super.onStart()

    val titleWatcher = object : TextWatcher {
      override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
      }

      override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        crime.title = s.toString()
      }

      override fun afterTextChanged(s: Editable?) {
      }
    }

    titleField.addTextChangedListener(titleWatcher)

    solvedCheckBox.apply {
      setOnCheckedChangeListener { _, isChecked ->
        crime.isSolved = isChecked
      }
    }

    arguments?.apply {
      val crimeId = getSerializable(ARG_CRIME_ID) as UUID
      Log.d(TAG, "Crime_id = $crimeId")
      //crimeViewModel.loadCrime(crimeId)
    }
  }

  override fun onStop() {
    super.onStop()
    crimeViewModel.saveCrime(crime)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    crimeViewModel.crimeLiveData.observe(viewLifecycleOwner, Observer { crime ->
      crime?.let {
        this.crime = crime
        updateUI()
      }
    })
  }

  private fun updateUI() {
    titleField.setText(crime.title)
    dateButton.text = crime.date.toString()
    solvedCheckBox.apply {
      isChecked = crime.isSolved
      jumpDrawablesToCurrentState()
    }
  }
}