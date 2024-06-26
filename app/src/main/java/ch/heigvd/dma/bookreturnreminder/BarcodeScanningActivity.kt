package ch.heigvd.dma.bookreturnreminder

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import ch.heigvd.dma.bookreturnreminder.ui.BookViewModel
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * DMA project - Managing reminder for borrowed Books at the Library - scan book barcode
 * and detection of iBeacons in a foreground service.
 * @author Bijelic Alen & Bogale Tegest
 * @Date 10.06.2024
 * Activity for scanning a book barcode.

 */
class BarcodeScanningActivity : AppCompatActivity() {

    private lateinit var cameraExecutor: ExecutorService
    private var isScanning = false
    private lateinit var bookViewModel: BookViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_barcode_scanner)

        cameraExecutor = Executors.newSingleThreadExecutor()
        bookViewModel = ViewModelProvider(this).get(BookViewModel::class.java)

        // Request camera permissions
        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(
                this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS
            )
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(findViewById<PreviewView>(R.id.live_camera_view).surfaceProvider)
            }

            val barcodeScanner = BarcodeScanning.getClient()

            val imageAnalyzer = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also {
                    it.setAnalyzer(cameraExecutor) { imageProxy ->
                        processImageProxy(barcodeScanner, imageProxy)
                    }
                }

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageAnalyzer
                )
            } catch (exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }
        }, ContextCompat.getMainExecutor(this))
    }

    @OptIn(ExperimentalGetImage::class)
    private fun processImageProxy(barcodeScanner: BarcodeScanner, imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

            barcodeScanner.process(image)
                .addOnSuccessListener { barcodes ->
                    if (!isScanning) {
                        for (barcode in barcodes) {
                            if (barcode.format == Barcode.FORMAT_EAN_13) {
                                isScanning = true
                                val isbnCode = barcode.rawValue
                                if (isbnCode != null) {
                                    bookViewModel.getBookByIsbn(isbnCode).observe(this) { book ->
                                        when {
                                            book == null -> {
                                                showToast("Book not found in the database")
                                                finish()
                                            }

                                            book.returnDate.isNotEmpty() -> {
                                                showToast("Book already borrowed")
                                                finish()
                                            }

                                            else -> {
                                                val intent = Intent(
                                                    this,
                                                    InsertBookActivity::class.java
                                                ).apply {
                                                    putExtra("isbnCode", isbnCode)
                                                }
                                                startActivity(intent)
                                                finish()
                                            }
                                        }
                                    }
                                }
                                break
                            }
                        }
                    }
                }
                .addOnFailureListener {
                    Log.e(TAG, "Barcode processing failed", it)
                }
                .addOnCompleteListener {
                    imageProxy.close()
                }
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                Toast.makeText(this, "Permissions not granted by the user.", Toast.LENGTH_SHORT)
                    .show()
                finish()
            }
        }
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    companion object {
        private const val TAG = "BarcodeScannerActivity"
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
    }
}
