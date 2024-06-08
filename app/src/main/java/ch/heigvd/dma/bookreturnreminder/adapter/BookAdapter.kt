package ch.heigvd.dma.bookreturnreminder.adapter

import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import ch.heigvd.dma.bookreturnreminder.R
import ch.heigvd.dma.bookreturnreminder.models.Book
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class BookAdapter(private val books: List<Book>, private val itemClickListener: OnItemClickListener) : RecyclerView.Adapter<BookAdapter.BookViewHolder>() {

    interface OnItemClickListener {
        fun onItemClick(book: Book)
    }

    class BookViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.tv_item_book_title)
        val author: TextView = itemView.findViewById(R.id.tv_item_book_author)
        val returnDate: TextView = itemView.findViewById(R.id.tv_item_book_return_date)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.book_item, parent, false)
        return BookViewHolder(view)
    }

    override fun onBindViewHolder(holder: BookViewHolder, position: Int) {
        val book = books[position]
        holder.title.text = book.title
        holder.author.text = book.author
        holder.returnDate.text = book.returnDate

        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        try {
            val returnDate = dateFormat.parse(book.returnDate)
            val currentDate = Date()

            if (returnDate != null && returnDate.before(currentDate)) {
                holder.returnDate.setTextColor(Color.RED)
            } else {
                holder.returnDate.setTextColor(Color.BLACK)
            }
        } catch (e: ParseException) {
            Log.e("BookAdapter", "Error parsing date: ${book.returnDate}", e)
            holder.returnDate.setTextColor(Color.BLACK)
        }

        holder.itemView.setOnClickListener {
            itemClickListener.onItemClick(book)
        }
    }

    override fun getItemCount(): Int {
        Log.d("BookAdapter", "getItemCount: ${books.size}")
        return books.size
    }
}