package ch.heigvd.dma.bookreturnreminder

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import ch.heigvd.dma.bookreturnreminder.models.Book
import ch.heigvd.dma.bookreturnreminder.ui.BookViewModel
import ch.heigvd.dma.bookreturnreminder.utils.DateUtils
import java.util.Calendar

class InsertBookActivity : AppCompatActivity() {

    private val bookViewModel: BookViewModel by viewModels()
    private lateinit var textViewTitle: TextView
    private lateinit var textViewAuthor: TextView
    private lateinit var textViewReturnDate: TextView
    private lateinit var buttonSelectDate: Button
    private lateinit var currentBook: Book

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_insert_book)

        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        textViewTitle = findViewById(R.id.textViewTitle)
        textViewAuthor = findViewById(R.id.textViewAuthor)
        textViewReturnDate = findViewById(R.id.textViewReturnDate)
        buttonSelectDate = findViewById(R.id.buttonSelectDate)
        val buttonSave: Button = findViewById(R.id.buttonSave)
        val buttonCancel: Button = findViewById(R.id.buttonCancel)

        val calendar = Calendar.getInstance()
        textViewReturnDate.text = DateUtils.formatDate(calendar)

        buttonSelectDate.setOnClickListener {
            DateUtils.showDatePickerDialog(this, calendar) { selectedDate ->
                textViewReturnDate.text = DateUtils.formatDate(selectedDate)
            }
        }

        val isbnCode = intent.getStringExtra("isbnCode")
        Log.d("InsertBookActivity", "ISBN code: $isbnCode")
        if (isbnCode != null) {
            bookViewModel.getBookByIsbn(isbnCode).observe(this, Observer { book ->
                if (book != null) {
                    currentBook = book
                    textViewTitle.text = book.title
                    textViewAuthor.text = book.author
                } else {
                    // Handle book not found in the database
                    Log.e("InsertBookActivity", "Book not found in the database")
                    finish()
                }
            })
        }

        buttonSave.setOnClickListener {
            currentBook.returnDate = textViewReturnDate.text.toString()
            bookViewModel.update(currentBook.isbnCode, currentBook.returnDate)
            finish()
        }

        buttonCancel.setOnClickListener {
            finish()
        }
    }
}