package ch.heigvd.dma.bookreturnreminder

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ch.heigvd.dma.bookreturnreminder.adapter.BookAdapter
import ch.heigvd.dma.bookreturnreminder.ui.BookViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity() {

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
        recyclerView.layoutManager = LinearLayoutManager(this)

        bookViewModel.booksToReturn.observe(this) { books ->
            val adapter = BookAdapter(books)
            recyclerView.adapter = adapter
        }

        val fab: FloatingActionButton = findViewById(R.id.fab)
        fab.setOnClickListener {
            val intent = Intent(this, BarcodeScanningActivity::class.java)
            startActivity(intent)
        }


        // TODO: Remove this code
        /*bookViewModel.deleteAll()

        val book = Book(
            isbnCode = "9782212566659",
            title = "Blockchain: La révolution de la confiance",
            author = "Laurent Leloup",
            returnDate = ""
        )

        bookViewModel.insert(book)*/
    }
}
