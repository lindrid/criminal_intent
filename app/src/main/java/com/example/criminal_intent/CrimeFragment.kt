package com.example.criminal_intent

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.text.Editable
import android.text.TextWatcher
import android.text.format.DateFormat
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

private const val ARG_CRIME_ID = "crime_id"
private const val TAG = "CrimeFragment"
private const val DIALOG_DATE = "DialogDate"
private const val REQUEST_DATE_PICKER = 0
private const val REQUEST_CONTACT_PICKER = 1
private const val DATE_FORMAT = "EEE, MM, dd"

class CrimeFragment: Fragment(), DatePickerFragment.Callbacks {
  private lateinit var crime: Crime
  private lateinit var titleField: EditText
  private lateinit var dateButton: Button
  private lateinit var solvedCheckBox: CheckBox
  private lateinit var suspectButton: Button
  private lateinit var reportButton: Button

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
    suspectButton   = view.findViewById(R.id.suspect_button) as Button
    reportButton    = view.findViewById(R.id.send_report_button) as Button

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

    reportButton.setOnClickListener {
      val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, getCrimeReport())
        putExtra(Intent.EXTRA_SUBJECT, getString(R.string.crime_report_subject))
      }

      val chooserIntent = Intent.createChooser(intent, getString(R.string.send_report))
      startActivity(chooserIntent)
    }

    val pickContactIntent = Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI)
    suspectButton.setOnClickListener {
      startActivityForResult(pickContactIntent, REQUEST_CONTACT_PICKER)
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
    if (crime.suspect.isNotEmpty()) {
      suspectButton.text = crime.suspect
    }
  }

  private fun getCrimeReport(): String {
    val solvedString = if (crime.isSolved) {
      getString(R.string.crime_report_solved)
    } else {
      getString(R.string.crime_report_unsolved)
    }

    val dateString = DateFormat.format(DATE_FORMAT, crime.date).toString()
    var suspect = if (crime.suspect.isBlank()) {
      getString(R.string.crime_report_no_suspect)
    } else {
      getString(R.string.crime_report_suspect, crime.suspect)
    }

    return getString(R.string.crime_report, crime.title, dateString, solvedString, suspect)
  }

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    when {
      resultCode != Activity.RESULT_OK -> return

      requestCode == REQUEST_CONTACT_PICKER && data != null -> {
        val contactUri: Uri? = data.data
        // какие поля должны быть у запрошенных строк данных из базы контактов
        val queryFields = arrayOf(ContactsContract.Contacts.DISPLAY_NAME)

        contactUri?.let { uri ->
          val cursor = requireActivity().contentResolver
            .query(uri, queryFields, null, null, null)

          cursor?.use {
            if (it.count == 0) {
              return
            }

            it.moveToFirst()
            val suspect = it.getString(0)
            crime.suspect = suspect
            crimeViewModel.saveCrime(crime)
          }
        }
      }
    }
  }

}