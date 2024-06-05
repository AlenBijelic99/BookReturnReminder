package ch.heigvd.dma.bookreturnreminder.repositories

import android.app.Application
import androidx.lifecycle.LiveData
import ch.heigvd.dma.bookreturnreminder.database.BookDao
import ch.heigvd.dma.bookreturnreminder.database.BookDatabase
import ch.heigvd.dma.bookreturnreminder.models.Book

class BookRepository(application: Application) {
    private val bookDao: BookDao = BookDatabase.getDatabase(application).bookDao()
    val booksToReturn: LiveData<List<Book>> = bookDao.getToReturnByAscReturnDate()

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
}