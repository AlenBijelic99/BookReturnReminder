package ch.heigvd.dma.bookreturnreminder

import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.commit
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ch.heigvd.dma.bookreturnreminder.adapter.BookAdapter
import ch.heigvd.dma.bookreturnreminder.fragments.InsertBookFragment
import ch.heigvd.dma.bookreturnreminder.models.Book
import ch.heigvd.dma.bookreturnreminder.ui.BookViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity() {

    private val bookViewModel: BookViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val emptyView = findViewById<TextView>(R.id.emptyView)

        bookViewModel.allBooks.observe(this) { books ->
            if (books.isEmpty()) {
                recyclerView.visibility = RecyclerView.GONE
                emptyView.visibility = TextView.VISIBLE
            } else {
                recyclerView.visibility = RecyclerView.VISIBLE
                emptyView.visibility = TextView.GONE
            }
            val adapter = BookAdapter(books)
            recyclerView.adapter = adapter
        }

        val fab: FloatingActionButton = findViewById(R.id.fab)
        fab.setOnClickListener {
            supportFragmentManager.commit {
                replace(R.id.main, InsertBookFragment())
                addToBackStack(null)
            }
        }

        // TODO: Remove this code
        // bookViewModel.deleteAll()
    }
}