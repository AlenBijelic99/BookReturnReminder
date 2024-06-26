package ch.heigvd.dma.bookreturnreminder.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import ch.heigvd.dma.bookreturnreminder.models.Book
import ch.heigvd.dma.bookreturnreminder.repositories.BookRepository
import kotlinx.coroutines.launch

/**
 * DMA project - Managing reminder for borrowed Books at the Library - scan book barcode
 * and detection of iBeacons in a foreground service.
 * @author Bijelic Alen & Bogale Tegest
 * @Date 10.06.2024
 * ViewModel for the Book entity.
 */
class BookViewModel(application: Application): AndroidViewModel(application) {

    private val bookRepository = BookRepository(application)

    val booksToReturn = bookRepository.booksToReturn

    fun insert(book: Book) {
        viewModelScope.launch {
            bookRepository.insert(book)
        }
    }

    fun update(isbnCode: String, returnDate: String) {
        viewModelScope.launch {
            bookRepository.update(isbnCode, returnDate)
        }
    }

    fun getBookByIsbn(isbnCode: String): LiveData<Book> = bookRepository.getBookByIsbn(isbnCode)

    fun deleteAll() {
        viewModelScope.launch {
            bookRepository.deleteAll()
        }
    }
}