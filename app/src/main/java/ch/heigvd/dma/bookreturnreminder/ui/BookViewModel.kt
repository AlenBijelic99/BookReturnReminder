package ch.heigvd.dma.bookreturnreminder.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import ch.heigvd.dma.bookreturnreminder.models.Book
import ch.heigvd.dma.bookreturnreminder.repositories.BookRepository
import kotlinx.coroutines.launch

class BookViewModel(application: Application): AndroidViewModel(application) {

    private val bookRepository = BookRepository(application)

    val booksToReturn = bookRepository.booksToReturn

    fun insert(book: Book) {
        viewModelScope.launch {
            bookRepository.insert(book)
        }
    }

    fun deleteAll() {
        viewModelScope.launch {
            bookRepository.deleteAll()
        }
    }
}