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
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import java.util.*

private const val ARG_CRIME_ID = "crime_id"
private const val TAG = "CrimeFragment"
private const val DIALOG_DATE = "DialogDate"
private const val DIALOG_TIME = "DialogTime"
private const val REQUEST_DATE_PICKER = 0
private const val REQUEST_TIME_PICKER = 1

class CrimeFragment: Fragment(), DatePickerFragment.Callbacks,
  TimePickerFragment.Callbacks {
  private lateinit var crime: Crime
  private lateinit var titleField: EditText
  private lateinit var dateButton: Button
  private lateinit var solvedCheckBox: CheckBox
  private lateinit var timeButton: Button
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

  override fun setDate(date: Date) {
    crime.date = date
    updateUI()
  }

  override fun setTime(hour: Int, minute: Int) {
    val calendar = Calendar.getInstance()
    calendar.time = crime.date
    calendar[Calendar.MILLISECOND] = 0
    calendar[Calendar.SECOND] = 0
    calendar[Calendar.MINUTE] = minute
    calendar[Calendar.HOUR] = hour
    crime.date = calendar.time
    updateUI()
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
    timeButton      = view.findViewById(R.id.time_button) as Button

    dateButton.text = crime.title.toString()

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

    dateButton.setOnClickListener {
      DatePickerFragment.newInstance(crime.date).apply {
        setTargetFragment(this@CrimeFragment, REQUEST_DATE_PICKER)
        show(this@CrimeFragment.requireFragmentManager(), DIALOG_DATE)
      }
    }

    timeButton.setOnClickListener {
      val calendar = Calendar.getInstance()
      calendar.time = crime.date
      TimePickerFragment.newInstance(calendar.get(Calendar.HOUR_OF_DAY),
        calendar.get(Calendar.MINUTE)).apply {
        setTargetFragment(this@CrimeFragment, REQUEST_TIME_PICKER)
        show(this@CrimeFragment.requireFragmentManager(), DIALOG_TIME)
      }
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