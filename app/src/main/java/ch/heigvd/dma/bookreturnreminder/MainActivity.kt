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
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import ch.heigvd.dma.bookreturnreminder.service.IBeaconMonitoringService

/**
 * DMA project - Managing reminder for borrowed Books at the Library - scan book barcode
 * and detection of iBeacons in a foreground service.
 * @author Bijelic Alen & Bogale Tegest
 * @Date 10.06.2024
 * Main activity that manages borrowed books and iBeacon monitoring.
 * The activity does the following:
 *      - displays the list of borrowed books and allows the user to manage them.
 *      - allows user to mark a book as returned or set a return date.
 *      - allows user can also scan a book barcode to add a new book to the list.
 *      - starts the iBeacon monitoring service.
 *      - requests the necessary permissions to start the iBeacon monitoring service.
 *      - inserts some books in the database.
 *      - displays a notification when a book is returned.
 *      - displays a notification when an iBeacon is detected.
 *      - displays a notification when the app is in the foreground.
 *      - display a notification to remind the user to return a book when the user is near the library (ibeacon configured)
 *      - sets reminder when a book due date is reached ( 3 days or less before the due date)
 */

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
                Log.d(
                    "Permissions",
                    "Denied foreground permissions: ${deniedPermissions.keys.joinToString()}"
                )
                Toast.makeText(
                    this,
                    "Foreground permissions denied: ${deniedPermissions.keys.joinToString()}",
                    Toast.LENGTH_LONG
                ).show()
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

        // Observe permissions granted to start the iBeacon monitoring service
        permissionsGranted.observe(this) { granted ->
            if (granted) startIBeaconMonitoringService()
        }

        // Check and request necessary permissions
        checkAndRequestPermissions()

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


    private fun startIBeaconMonitoringService() {
        val intent = Intent(this, IBeaconMonitoringService::class.java)
        startForegroundService(intent)
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


    /**
     * Handles the result of the permission request.
     */
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

