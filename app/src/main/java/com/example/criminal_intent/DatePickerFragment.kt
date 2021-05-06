package com.example.criminal_intent

import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import java.util.*

private const val ARG_DATE = "date"

class DatePickerFragment: DialogFragment() {

    interface Callbacks {
        fun setDate(date: Date)
    }

    companion object {
        fun newInstance(date: Date): DatePickerFragment {
            val args = Bundle().apply {
                putSerializable(ARG_DATE, date)
            }
            return DatePickerFragment().apply {
                arguments = args
            }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val date = arguments?.get(ARG_DATE) as Date
        val calendar = Calendar.getInstance()
        calendar.time = date

        val initialYear = calendar.get(Calendar.YEAR)
        val initialMonth = calendar.get(Calendar.MONTH)
        val initialDay = calendar.get(Calendar.DAY_OF_MONTH)

        val onDateSetListener = DatePickerDialog.OnDateSetListener {_, year, month, day ->
            val date = GregorianCalendar(year, month, day).time
            (targetFragment as Callbacks)?.setDate(date)
        }

        return DatePickerDialog(requireContext(), onDateSetListener,
            initialYear, initialMonth, initialDay)
    }
}