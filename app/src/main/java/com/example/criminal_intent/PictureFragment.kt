package com.example.criminal_intent

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import androidx.core.content.FileProvider
import androidx.fragment.app.DialogFragment
import java.io.File

private const val ARG_PHOTO_FILE = "photo_file"
private const val ARG_PHOTO_URI = "photo_uri"

class PictureFragment: DialogFragment() {
  private lateinit var fullPictureView: ImageView
  private lateinit var closeButton: Button
  private var callbacks: CrimeListFragment.Callbacks? = null

  companion object {
    fun newInstance (photoFile: File, photoUri: Uri): PictureFragment {
      val args = Bundle().apply {
        putSerializable(ARG_PHOTO_FILE, photoFile)
      }

      return PictureFragment().apply {
        arguments = args
      }
    }
  }

  override fun onCreateView (inflater: LayoutInflater, container: ViewGroup?,
                             savedInstanceState: Bundle?): View? {
    val photoFile = arguments?.get(ARG_PHOTO_FILE) as File
    val photoUri = FileProvider.getUriForFile(requireActivity(),
      "com.example.criminal_intent.fileprovider", photoFile)
    val view = inflater.inflate(R.layout.fragment_picture, container, false)

    fullPictureView = view.findViewById(R.id.photo_fullscreen)
    closeButton = view.findViewById(R.id.close_button)

    if (photoFile.exists()) {
      val bitmap = getScaledBitmap(photoFile.path, requireActivity())
      fullPictureView.setImageBitmap(bitmap)
      fullPictureView.rotation = getRotation(requireActivity(), photoUri)
    }
    else {
      fullPictureView.setImageDrawable(null)
    }

    closeButton.setOnClickListener {
      requireActivity().onBackPressed()
    }

    return view
  }
}