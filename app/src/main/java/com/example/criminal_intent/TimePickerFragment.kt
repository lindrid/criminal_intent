package com.example.criminal_intent

import android.app.DatePickerDialog
import android.app.Dialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.DialogFragment
import java.util.*

private const val ARG_HOUR = "hours"
private const val ARG_MINUTE = "minutes"
private const val TIME_TAG = "time"

class TimePickerFragment: DialogFragment() {

    interface Callbacks {
        fun setTime(hour: Int, minute: Int)
    }

    companion object {
        fun newInstance(hour: Int, minute: Int): TimePickerFragment {
            val args = Bundle().apply {
                putInt(ARG_HOUR, hour)
                Log.d(TIME_TAG, "Hours: $hour")
                putInt(ARG_MINUTE, minute)
                Log.d(TIME_TAG, "Minutes: $minute")
            }
            return TimePickerFragment().apply {
                arguments = args
            }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val hour = arguments?.get(ARG_HOUR) as Int
        val minute = arguments?.get(ARG_MINUTE) as Int

        val onTimeSetListener = TimePickerDialog.OnTimeSetListener {_, hourOfDay, minutes ->
            Log.d(TIME_TAG, "Set hours: $hourOfDay, minutes: $minutes")
            (targetFragment as Callbacks)?.setTime(hourOfDay, minutes)
        }

        return TimePickerDialog(requireContext(), onTimeSetListener,
            hour, minute, true)
    }
}