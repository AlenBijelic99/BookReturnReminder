package ch.heigvd.dma.bookreturnreminder.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import ch.heigvd.dma.bookreturnreminder.models.Book

@Dao
interface BookDao {
    @Insert
    suspend fun insert(book: Book)

    @Query("SELECT * FROM books WHERE return_date IS NOT NULL AND return_date != '' ORDER BY return_date ASC")
    fun getToReturnByAscReturnDate(): LiveData<List<Book>>

    @Query("DELETE FROM books")
    suspend fun deleteAll()

    @Query("SELECT * FROM books WHERE isbn_code = :isbnCode LIMIT 1")
    fun getBookByIsbn(isbnCode: String): LiveData<Book>

    @Query("UPDATE books SET return_date = :returnDate WHERE isbn_code = :isbnCode")
    suspend fun update(isbnCode: String, returnDate: String)
}