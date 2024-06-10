package ch.heigvd.dma.bookreturnreminder


import android.Manifest
import android.app.AlarmManager
import android.app.AlertDialog
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ch.heigvd.dma.bookreturnreminder.adapter.BookAdapter
import ch.heigvd.dma.bookreturnreminder.models.Book
import ch.heigvd.dma.bookreturnreminder.ui.BookViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.util.Calendar
import ch.heigvd.dma.bookreturnreminder.utils.DateUtils
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import ch.heigvd.dma.bookreturnreminder.service.BookReminderBroadcastReceiver
import ch.heigvd.dma.bookreturnreminder.service.IBeaconMonitoringService

class MainActivity : AppCompatActivity(), BookAdapter.OnItemClickListener {


    companion object {
        private const val PERMISSION_REQUEST_CODE = 1001
    }

    private val permissionsGranted = MutableLiveData(false)
    private val bookViewModel: BookViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        val emptyView = findViewById<TextView>(R.id.emptyView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        bookViewModel.booksToReturn.observe(this) { books ->
            val adapter = BookAdapter(books, this)
            recyclerView.adapter = adapter
            if (books.isEmpty()) {
                recyclerView.visibility = RecyclerView.GONE
                emptyView.visibility = TextView.VISIBLE
            } else {
                recyclerView.visibility = RecyclerView.VISIBLE
                emptyView.visibility = TextView.GONE
            }
        }

        val fab: FloatingActionButton = findViewById(R.id.fab)
        fab.setOnClickListener {
            val intent = Intent(this, BarcodeScanningActivity::class.java)
            startActivity(intent)
        }

        // Uncomment the following lines to insert some books in the database

        bookViewModel.deleteAll()

        val books = listOf(
            Book(
                1,
                "9782212566659",
                "Blockchain: La révolution de la confiance",
                "Laurent Leloup",
                ""
            ),
            Book(2, "9782266159203", "Le Horla", "Guy de Maupassant", ""),
            Book(3, "9782266161107", "Le dernier jour d'un condamné", "Victor Hugo", ""),
            Book(4, "9782409020865", "Flexbox et Grid", "Christophe AUBRY", ""),
            Book(
                5,
                "9789389932072",
                "Learn angular in 24 hours",
                "Lakshmi Kamala Thota",
                "2024-06-10"
            ),
        )
        books.forEach { bookViewModel.insert(it) }


        // Schedule reminder notifications for all books
        bookViewModel.booksToReturn.observe(this) { toReturnBooks ->
            toReturnBooks.forEach { toReturnBook ->
                scheduleReminderNotification(toReturnBook)
            }
        }

        // Start the iBeacon monitoring service after checking permissions
        if (checkAndRequestPermisions()) {
            startIBeaconMonitoringService()
        }

        permissionsGranted.observe(this) { granted ->
            if (granted) startIBeaconMonitoringService()
        }
    }

    override fun onItemClick(book: Book) {
        val options = arrayOf("Modifier la date de retour", "Marquer comme rendu")
        AlertDialog.Builder(this)
            .setTitle(book.title)
            .setItems(options) { _, which ->
                when (which) {
                    0 -> showDatePicker(book)
                    1 -> markAsReturned(book)
                }
            }
            .show()
    }

    private fun showDatePicker(book: Book) {
        val initialDate = if (book.returnDate.isNotEmpty()) {
            DateUtils.parseDate(book.returnDate)
        } else {
            Calendar.getInstance()
        }
        DateUtils.showDatePickerDialog(this, initialDate) { selectedDate ->
            val selectedDateString = DateUtils.formatDate(selectedDate)
            book.returnDate = selectedDateString
            bookViewModel.update(book.isbnCode, selectedDateString)
            Toast.makeText(this, "Return date updated", Toast.LENGTH_SHORT).show()
        }
    }

    private fun markAsReturned(book: Book) {
        book.returnDate = ""
        bookViewModel.update(book.isbnCode, "")
        Toast.makeText(this, "Book returned", Toast.LENGTH_SHORT).show()
    }


    // Code for Notification
    private fun scheduleReminderNotification(book: Book) {
        if (book.returnDate.isNotEmpty()) {
            val calendar = DateUtils.parseDate(book.returnDate)
            calendar.add(Calendar.DAY_OF_YEAR, -3) // Remind 3 days before due date
            val reminderTime = calendar.timeInMillis

            Log.d("MainActivity", "Setting reminder for book: ${book.title} at $reminderTime")

            val intent = Intent(this, BookReminderBroadcastReceiver::class.java).apply {
                putExtra("bookTitle", book.title)
            }

            val pendingIntent = PendingIntent.getBroadcast(
                this,
                book.id, // Use book.id as the request code to ensure uniqueness
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.set(AlarmManager.RTC_WAKEUP, reminderTime, pendingIntent)

            Log.d("MainActivity", "Alarm set for book: ${book.title}")
        }
    }

    private fun startIBeaconMonitoringService() {
        val intent = Intent(this, IBeaconMonitoringService::class.java)
        startForegroundService(intent)
    }

    private fun checkAndRequestPermisions(): Boolean {
        val permissionsNeeded = mutableListOf<String>()
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            permissionsNeeded.add(Manifest.permission.ACCESS_FINE_LOCATION)
        }
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            permissionsNeeded.add(Manifest.permission.ACCESS_COARSE_LOCATION)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.BLUETOOTH_SCAN
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                permissionsNeeded.add(Manifest.permission.BLUETOOTH_SCAN)
            }
        }
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            permissionsNeeded.add(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        }

        return if (permissionsNeeded.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                this,
                permissionsNeeded.toTypedArray(),
                PERMISSION_REQUEST_CODE
            )
            false
        } else {
            true
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            val allPermissionsGranted =
                grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }
            permissionsGranted.postValue(allPermissionsGranted)
        }
    }

}



