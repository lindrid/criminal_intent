package com.example.criminal_intent

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.Picture
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.text.format.DateFormat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

private const val ARG_CRIME_ID = "crime_id"
private const val TAG = "CrimeFragment"
private const val DIALOG_DATE = "DialogDate"
private const val REQUEST_DATE_PICKER = 0
private const val REQUEST_CONTACT_PICKER = 1
private const val REQUEST_PHOTO_CAPTURE = 2


class CrimeFragment: Fragment(), DatePickerFragment.Callbacks {
  private lateinit var crime: Crime
  private lateinit var photoFile: File
  private lateinit var photoUri: Uri
  private lateinit var titleField: EditText
  private lateinit var dateButton: Button
  private lateinit var solvedCheckBox: CheckBox
  private lateinit var suspectButton: Button
  private lateinit var reportButton: Button
  private lateinit var photoButton: ImageButton
  private lateinit var photoView: ImageView

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
    photoButton     = view.findViewById(R.id.crime_camera) as ImageButton
    photoView       = view.findViewById(R.id.crime_photo) as ImageView

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

    if (contactsAppDoesNotExist(pickContactIntent)) {
      suspectButton.isEnabled = false
    }

    photoButton.apply {
      val packageManager = requireActivity().packageManager
      val captureImage = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
      val resolvedActivity: ResolveInfo? = packageManager.resolveActivity(captureImage,
        PackageManager.MATCH_DEFAULT_ONLY)

      if (resolvedActivity == null) {
        isEnabled = false
      }

      setOnClickListener {
        captureImage.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)

        val cameraActivities: List<ResolveInfo> = packageManager.queryIntentActivities(captureImage,
          PackageManager.MATCH_DEFAULT_ONLY)

        for (cameraActivity in cameraActivities) {
          requireActivity().grantUriPermission(cameraActivity.activityInfo.packageName,
            photoUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        }

        startActivityForResult(captureImage, REQUEST_PHOTO_CAPTURE)
      }
    }

    photoView.setOnClickListener {
      val pictureFragment = PictureFragment.newInstance(photoFile, photoUri)
      val mainActivity = requireActivity() as MainActivity
      mainActivity.popupPictureFragment(pictureFragment)
    }
  }

  private fun contactsAppDoesNotExist(pickContactIntent: Intent): Boolean {
    val packageManager: PackageManager = requireActivity().packageManager
    val resolvedActivity: ResolveInfo? = packageManager.resolveActivity(pickContactIntent,
      PackageManager.MATCH_DEFAULT_ONLY)

    return resolvedActivity == null
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
        photoFile = crimeViewModel.getPhotoFile(crime)
        photoUri = FileProvider.getUriForFile(requireActivity(),
          "com.example.criminal_intent.fileprovider", photoFile)
        updateUI()
      }
    })
  }

  private fun updateUI() {
    titleField.setText(crime.title)
    dateButton.text = getCurrentLocaleDateString(resources, crime.date)
    solvedCheckBox.apply {
      isChecked = crime.isSolved
      jumpDrawablesToCurrentState()
    }
    if (crime.suspect.isNotEmpty()) {
      suspectButton.text = crime.suspect
    }

    updatePhotoView()
  }

  private fun updatePhotoView() {
    if (photoFile.exists()) {
      val bitmap = getScaledBitmap(photoFile.path, requireActivity())
      photoView.setImageBitmap(bitmap)
      photoView.rotation = getRotation(requireActivity(), photoUri)
    }
    else {
      photoView.setImageDrawable(null)
    }
  }

  private fun getCrimeReport(): String {
    val solvedString = if (crime.isSolved) {
      getString(R.string.crime_report_solved)
    } else {
      getString(R.string.crime_report_unsolved)
    }

    val dateString = getCurrentLocaleDateString(resources, crime.date)

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
        // ?????????? ???????? ???????????? ???????? ?? ?????????????????????? ?????????? ???????????? ???? ???????? ??????????????????
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

      requestCode == REQUEST_PHOTO_CAPTURE -> {
        requireActivity().revokeUriPermission(photoUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        updatePhotoView()
      }
    }
  }

  override fun onDetach() {
    super.onDetach()
    requireActivity().revokeUriPermission(photoUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
  }
}