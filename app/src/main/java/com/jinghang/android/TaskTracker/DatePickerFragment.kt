package com.jinghang.android.TaskTracker

import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Bundle
import android.widget.DatePicker
import androidx.fragment.app.DialogFragment
import java.util.*

private const val ARG_DATE = "date"

class DatePickerFragment : DialogFragment() {
    private lateinit var dateListener: DatePickerDialog.OnDateSetListener

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val date: Date = arguments?.getSerializable(ARG_DATE) as Date

        val calendar = Calendar.getInstance()
        calendar.time = date

        val initialYear = calendar.get(Calendar.YEAR)
        val initialMonth = calendar.get(Calendar.MONTH)
        val initialDay = calendar.get(Calendar.DAY_OF_MONTH)

        dateListener =
            DatePickerDialog.OnDateSetListener { _: DatePicker, year: Int, month: Int, day: Int ->
                val resultDate: Date = GregorianCalendar(year, month, day).time
                (targetFragment as? Callbacks)?.onDateSelected(resultDate)
            }

        return DatePickerDialog(
            requireContext(),
            dateListener,
            initialYear,
            initialMonth,
            initialDay
        )
    }

    interface Callbacks {
        fun onDateSelected(date: Date)
    }
}
