package ch.heigvd.dma.bookreturnreminder.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import ch.heigvd.dma.bookreturnreminder.R
import ch.heigvd.dma.bookreturnreminder.models.Book

class BookAdapter(private val books: List<Book>) : RecyclerView.Adapter<BookAdapter.BookViewHolder>() {

    class BookViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.tv_item_book_title)
        val author: TextView = itemView.findViewById(R.id.tv_item_book_author)
        val returnDate: TextView = itemView.findViewById(R.id.tv_item_book_return_date)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_book, parent, false)
        return BookViewHolder(view)
    }

    override fun onBindViewHolder(holder: BookViewHolder, position: Int) {
        val book = books[position]
        holder.title.text = book.title
        holder.author.text = book.author
        holder.returnDate.text = book.returnDate
    }

    override fun getItemCount(): Int {
        Log.d("BookAdapter", "getItemCount: ${books.size}")
        return books.size
    }
}