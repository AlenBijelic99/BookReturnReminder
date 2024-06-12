package ch.heigvd.dma.bookreturnreminder.utils

import android.app.DatePickerDialog
import android.content.Context
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * DMA project - Managing reminder for borrowed Books at the Library - scan book barcode
 * and detection of iBeacons in a foreground service.
 * @author Bijelic Alen & Bogale Tegest
 * @Date 10.06.2024
 * Utility class for date formatting and parsing.

 */
object DateUtils {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    fun formatDate(calendar: Calendar): String {
        return dateFormat.format(calendar.time)
    }

    fun parseDate(dateString: String): Calendar {
        val calendar = Calendar.getInstance()
        calendar.time = dateFormat.parse(dateString)!!
        return calendar
    }

    fun showDatePickerDialog(context: Context, initialDate: Calendar, onDateSet: (Calendar) -> Unit) {
        val year = initialDate.get(Calendar.YEAR)
        val month = initialDate.get(Calendar.MONTH)
        val day = initialDate.get(Calendar.DAY_OF_MONTH)

        val dateSetListener = DatePickerDialog.OnDateSetListener { _, selectedYear, selectedMonth, selectedDay ->
            val selectedDate = Calendar.getInstance()
            selectedDate.set(selectedYear, selectedMonth, selectedDay)
            onDateSet(selectedDate)
        }

        DatePickerDialog(context, dateSetListener, year, month, day).show()
    }
}