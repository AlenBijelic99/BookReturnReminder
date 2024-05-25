package ch.heigvd.dma.bookreturnreminder.fragments

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import ch.heigvd.dma.bookreturnreminder.R
import ch.heigvd.dma.bookreturnreminder.models.Book
import ch.heigvd.dma.bookreturnreminder.ui.BookViewModel
import java.text.SimpleDateFormat
import java.util.Calendar

class InsertBookFragment: Fragment() {

    private val bookViewModel: BookViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_insert_book, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val editTextTitle: EditText = view.findViewById(R.id.editTextTitle)
        val editTextAuthor: EditText = view.findViewById(R.id.editTextAuthor)
        val editTextReturnDate: EditText = view.findViewById(R.id.editTextReturnDate)
        val buttonSave: Button = view.findViewById(R.id.buttonSave)

        val calendar = Calendar.getInstance()
        val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
            val selectedDate = "$year-${month + 1}-$dayOfMonth"
            editTextReturnDate.setText(selectedDate)
        }

        editTextReturnDate.setOnClickListener {
            DatePickerDialog(
                requireContext(),
                dateSetListener,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        buttonSave.setOnClickListener {
            val book = Book(
                code = "",
                title = editTextTitle.text.toString(),
                author = editTextAuthor.text.toString(),
                returnDate = editTextReturnDate.text.toString()
            )
            bookViewModel.insert(book)
            requireActivity().supportFragmentManager.popBackStack()
        }
    }
}