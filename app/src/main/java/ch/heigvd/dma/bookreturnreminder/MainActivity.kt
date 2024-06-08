package ch.heigvd.dma.bookreturnreminder

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ch.heigvd.dma.bookreturnreminder.adapter.BookAdapter
import ch.heigvd.dma.bookreturnreminder.models.Book
import ch.heigvd.dma.bookreturnreminder.ui.BookViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.util.Calendar
import ch.heigvd.dma.bookreturnreminder.utils.DateUtils

class MainActivity : AppCompatActivity(), BookAdapter.OnItemClickListener {

    private val bookViewModel: BookViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        val emptyView = findViewById<TextView>(R.id.emptyView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        bookViewModel.booksToReturn.observe(this) { books ->
            val adapter = BookAdapter(books, this)
            recyclerView.adapter = adapter
            if (books.isEmpty()) {
                recyclerView.visibility = RecyclerView.GONE
                emptyView.visibility = TextView.VISIBLE
            } else {
                recyclerView.visibility = RecyclerView.VISIBLE
                emptyView.visibility = TextView.GONE
            }
        }

        val fab: FloatingActionButton = findViewById(R.id.fab)
        fab.setOnClickListener {
            val intent = Intent(this, BarcodeScanningActivity::class.java)
            startActivity(intent)
        }

        // Uncomment the following lines to insert some books in the database
        /*
        bookViewModel.deleteAll()

        val books = listOf(
            Book(1, "9782212566659", "Blockchain: La révolution de la confiance", "Laurent Leloup", ""),
            Book(2, "9782266159203", "Le Horla", "Guy de Maupassant", ""),
            Book(3, "9782266161107", "Le dernier jour d'un condamné", "Victor Hugo", ""),
        )
        books.forEach { bookViewModel.insert(it) }
        */

    }

    override fun onItemClick(book: Book) {
        val options = arrayOf("Modifier la date de retour", "Marquer comme rendu")
        AlertDialog.Builder(this)
            .setTitle(book.title)
            .setItems(options) { _, which ->
                when (which) {
                    0 -> showDatePicker(book)
                    1 -> markAsReturned(book)
                }
            }
            .show()
    }

    private fun showDatePicker(book: Book) {
        val initialDate = if (book.returnDate.isNotEmpty()) {
            DateUtils.parseDate(book.returnDate)
        } else {
            Calendar.getInstance()
        }
        DateUtils.showDatePickerDialog(this, initialDate) { selectedDate ->
            val selectedDateString = DateUtils.formatDate(selectedDate)
            book.returnDate = selectedDateString
            bookViewModel.update(book.isbnCode, selectedDateString)
            Toast.makeText(this, "Return date updated", Toast.LENGTH_SHORT).show()
        }
    }

    private fun markAsReturned(book: Book) {
        book.returnDate = ""
        bookViewModel.update(book.isbnCode, "")
        Toast.makeText(this, "Book returned", Toast.LENGTH_SHORT).show()
    }
}
