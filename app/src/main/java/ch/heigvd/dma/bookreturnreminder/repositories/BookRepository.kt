package ch.heigvd.dma.bookreturnreminder.repositories

import android.app.Application
import androidx.lifecycle.LiveData
import ch.heigvd.dma.bookreturnreminder.database.BookDao
import ch.heigvd.dma.bookreturnreminder.database.BookDatabase
import ch.heigvd.dma.bookreturnreminder.models.Book
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * DMA project - Managing reminder for borrowed Books at the Library - scan book barcode
 * and detection of iBeacons in a foreground service.
 * @author Bijelic Alen & Bogale Tegest
 * @Date 10.06.2024
 * Repository for the Book entity.
 */
class BookRepository(application: Application) {
    private val bookDao: BookDao = BookDatabase.getDatabase(application).bookDao()
    val booksToReturn: LiveData<List<Book>> = bookDao.getBooksToReturn()

    init {
        // Insert sample data only if the database is empty
        GlobalScope.launch {
            if (bookDao.getBooksCount() == 0) {
                val sampleBooks = listOf(
                    Book(1, "9782212566659", "Blockchain: La révolution de la confiance", "Laurent Leloup", ""),
                    Book(2, "9782266159203", "Le Horla", "Guy de Maupassant", ""),
                    Book(3, "9782266161107", "Le dernier jour d'un condamné", "Victor Hugo", ""),
                    Book(4, "9782409020865", "Flexbox et Grid", "Christophe AUBRY", ""),
                    Book(5, "9789389932072", "Learn angular in 24 hours", "Lakshmi Kamala Thota", "")
                )
                sampleBooks.forEach { bookDao.insert(it) }
            }
        }
    }

    suspend fun insert(book: Book) {
        withContext(Dispatchers.IO) {
            bookDao.insert(book)
        }
    }

    suspend fun deleteAll() {
        withContext(Dispatchers.IO) {
            bookDao.deleteAll()
        }
    }

    fun getBookByIsbn(isbnCode: String): LiveData<Book> {
        return bookDao.getBookByIsbn(isbnCode)
    }

    suspend fun update(isbnCode: String, returnDate: String) {
        withContext(Dispatchers.IO) {
            bookDao.update(isbnCode, returnDate)
        }
    }

    suspend fun getBooksListToReturn(): List<Book> {
        return withContext(Dispatchers.IO) {
            bookDao.getBooksListToReturn()
        }
    }
}
