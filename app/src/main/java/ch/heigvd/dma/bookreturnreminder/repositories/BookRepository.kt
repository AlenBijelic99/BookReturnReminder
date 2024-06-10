package ch.heigvd.dma.bookreturnreminder.repositories

import android.app.Application
import androidx.lifecycle.LiveData
import ch.heigvd.dma.bookreturnreminder.database.BookDao
import ch.heigvd.dma.bookreturnreminder.database.BookDatabase
import ch.heigvd.dma.bookreturnreminder.models.Book
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class BookRepository(application: Application) {
    private val bookDao: BookDao = BookDatabase.getDatabase(application).bookDao()
    val booksToReturn: LiveData<List<Book>> = bookDao.getBooksToReturn()

    suspend fun insert(book: Book) {
        bookDao.insert(book)
    }

    suspend fun deleteAll() {
        bookDao.deleteAll()
    }

    fun getBookByIsbn(isbnCode: String): LiveData<Book> {
        return bookDao.getBookByIsbn(isbnCode)
    }

    suspend fun update(isbnCode: String, returnDate: String) {
        bookDao.update(isbnCode, returnDate)
    }

    suspend fun getBooksListToReturn(): List<Book> {
        return withContext(Dispatchers.IO) {
            bookDao.getBooksListToReturn()
        }
    }
}