package ch.heigvd.dma.bookreturnreminder.repositories

import android.app.Application
import androidx.lifecycle.LiveData
import ch.heigvd.dma.bookreturnreminder.database.BookDao
import ch.heigvd.dma.bookreturnreminder.database.BookDatabase
import ch.heigvd.dma.bookreturnreminder.models.Book

class BookRepository(application: Application) {
    private val bookDao: BookDao = BookDatabase.getDatabase(application).bookDao()
    val allBooks: LiveData<List<Book>> = bookDao.getAllByDescReturnDate()

    suspend fun insert(book: Book) {
        bookDao.insert(book)
    }
}