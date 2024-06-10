package ch.heigvd.dma.bookreturnreminder


import android.Manifest
import android.app.AlertDialog
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
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import ch.heigvd.dma.bookreturnreminder.service.IBeaconMonitoringService

class MainActivity : AppCompatActivity(), BookAdapter.OnItemClickListener {


    companion object {
        private const val PERMISSION_REQUEST_CODE = 1001
    }

    private val permissionsGranted = MutableLiveData(false)
    private val bookViewModel: BookViewModel by viewModels()
    private var foregroundPermissionsGranted = false

    private val requestForegroundPermissionsLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val deniedPermissions = permissions.filter { !it.value }
            if (deniedPermissions.isNotEmpty()) {
                Log.d("Permissions", "Denied foreground permissions: ${deniedPermissions.keys.joinToString()}")
                Toast.makeText(this, "Foreground permissions denied: ${deniedPermissions.keys.joinToString()}", Toast.LENGTH_LONG).show()
            }
            foregroundPermissionsGranted = permissions.entries.all { it.value }
            permissionsGranted.postValue(foregroundPermissionsGranted)
        }

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

        permissionsGranted.observe(this) { granted ->
            if (granted) startIBeaconMonitoringService()
        }

        // Check permissions
        checkAndRequestPermissions()

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
                ""
            ),
        )
        books.forEach { bookViewModel.insert(it) }


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
    private fun startIBeaconMonitoringService() {
        val intent = Intent(this, IBeaconMonitoringService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }

    private fun checkAndRequestPermissions() {
        val permissionsNeeded = mutableListOf<String>()
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            permissionsNeeded.add(Manifest.permission.ACCESS_FINE_LOCATION)
            Log.d("Permissions", "ACCESS_FINE_LOCATION not granted")
        }
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            permissionsNeeded.add(Manifest.permission.ACCESS_COARSE_LOCATION)
            Log.d("Permissions", "ACCESS_COARSE_LOCATION not granted")
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.BLUETOOTH_SCAN
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                permissionsNeeded.add(Manifest.permission.BLUETOOTH_SCAN)
                Log.d("Permissions", "BLUETOOTH_SCAN not granted")
            }
        }

        if (permissionsNeeded.isNotEmpty()) {
            requestForegroundPermissionsLauncher.launch(permissionsNeeded.toTypedArray())
        } else {
            foregroundPermissionsGranted = true
            permissionsGranted.postValue(true)
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

