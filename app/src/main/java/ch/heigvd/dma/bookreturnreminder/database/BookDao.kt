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

    @Query("SELECT * FROM books ORDER BY return_date ASC")
    fun getAllByAscReturnDate(): LiveData<List<Book>>

    @Query("DELETE FROM books")
    suspend fun deleteAll()
}