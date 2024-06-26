package ch.heigvd.dma.bookreturnreminder.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import ch.heigvd.dma.bookreturnreminder.models.Book

/**
 * DMA project - Managing reminder for borrowed Books at the Library - scan book barcode
 * and detection of iBeacons in a foreground service.
 * @author Bijelic Alen & Bogale Tegest
 * @Date 10.06.2024
 * Data access object for the Book entity.

 */
@Dao
interface BookDao {
    @Insert
    suspend fun insert(book: Book)

    @Query("SELECT * FROM books WHERE return_date IS NOT NULL AND return_date != '' ORDER BY return_date ASC")
    fun getBooksToReturn(): LiveData<List<Book>>

    @Query("DELETE FROM books")
    suspend fun deleteAll()

    @Query("SELECT * FROM books WHERE isbn_code = :isbnCode LIMIT 1")
    fun getBookByIsbn(isbnCode: String): LiveData<Book>

    @Query("UPDATE books SET return_date = :returnDate WHERE isbn_code = :isbnCode")
    suspend fun update(isbnCode: String, returnDate: String)

    @Query("SELECT * FROM books WHERE return_date != ''")
    suspend fun getBooksListToReturn(): List<Book>

    @Query("SELECT COUNT(*) FROM books")
    suspend fun getBooksCount(): Int
}