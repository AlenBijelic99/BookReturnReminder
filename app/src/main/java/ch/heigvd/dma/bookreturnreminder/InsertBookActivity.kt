package ch.heigvd.dma.bookreturnreminder

import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import ch.heigvd.dma.bookreturnreminder.models.Book
import ch.heigvd.dma.bookreturnreminder.ui.BookViewModel
import java.util.Calendar

class InsertBookActivity : AppCompatActivity() {

    private val bookViewModel: BookViewModel by viewModels()
    private lateinit var textViewTitle: TextView
    private lateinit var textViewAuthor: TextView
    private lateinit var editTextReturnDate: EditText
    private lateinit var currentBook: Book

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_insert_book)

        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        textViewTitle = findViewById(R.id.textViewTitle)
        textViewAuthor = findViewById(R.id.textViewAuthor)
        editTextReturnDate = findViewById(R.id.editTextReturnDate)
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
            currentBook.returnDate = editTextReturnDate.text.toString()
            bookViewModel.update(currentBook.isbnCode, currentBook.returnDate)
            finish()
        }

        buttonCancel.setOnClickListener {
            finish()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_scan -> {
                // Handle scan action
                true
            }
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}