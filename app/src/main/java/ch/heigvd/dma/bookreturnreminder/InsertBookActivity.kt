package ch.heigvd.dma.bookreturnreminder

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.viewModels
import ch.heigvd.dma.bookreturnreminder.models.Book
import ch.heigvd.dma.bookreturnreminder.ui.BookViewModel
import java.util.Calendar

class InsertBookActivity : AppCompatActivity() {

    private val bookViewModel: BookViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_insert_book)

        val editTextTitle: EditText = findViewById(R.id.editTextTitle)
        val editTextAuthor: EditText = findViewById(R.id.editTextAuthor)
        val editTextReturnDate: EditText = findViewById(R.id.editTextReturnDate)
        val buttonSave: Button = findViewById(R.id.buttonSave)
        val buttonCancel: Button = findViewById(R.id.buttonCancel)

        val calendar = Calendar.getInstance()
        val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
            val selectedDate = "$year-${month + 1}-$dayOfMonth"
            editTextReturnDate.setText(selectedDate)
        }

        editTextReturnDate.setOnClickListener {
            DatePickerDialog(
                this,
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
            finish()
        }

        buttonCancel.setOnClickListener {
            finish()
        }
    }
}