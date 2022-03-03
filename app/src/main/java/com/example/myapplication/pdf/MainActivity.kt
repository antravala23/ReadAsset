package com.example.myapplication.pdf

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.example.myapplication.R
import java.io.*
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity() {
    private val TAG = "_mainacitivity"

    // Folder where we would copy the pdf file and store from assets folder
    // This is necessary to let a viewing app to pickup the file from a globally accessible directory
    private val tmpFolder = Environment.getExternalStorageDirectory().path + "/pdfFiles"

    // Permission code to write to external storage
    private val EXT_STORAGE_PERMISSION_CODE = 101

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Check if the permission is already there. If not, we need user to grant it
        // Manifest.permission.WRITE_EXTERNAL_STORAGE

        // We do not have permission. So need to request it
        if (ContextCompat.checkSelfPermission(
                this@MainActivity,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
            == PackageManager.PERMISSION_DENIED
        ) {
            ActivityCompat.requestPermissions(
                this@MainActivity, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                EXT_STORAGE_PERMISSION_CODE
            )
            Log.d(
                TAG,
                "After getting permission: " + Manifest.permission.WRITE_EXTERNAL_STORAGE + " " + ContextCompat.checkSelfPermission(
                    this@MainActivity,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
            )
        } else {
            // We were granted permission already before
            Log.d(TAG, "Already has permission to write to external storage")
        }


        // Find the table layout by id and start populating the rows. The layout is defined in layout/conent_main.xml
        val table = findViewById<View>(R.id.tableForPDF) as TableLayout

        // get list of pdf files in assets folder
        val fileNames = getPDFFromAssets()
        Log.d(TAG, "Number of pdf files in assets folder:" + fileNames.size)
        // just to alternate row color. Probably better way out there?
        for ((rowCount, fileName) in fileNames.withIndex()) {
            // create a new TableRow
            val row = TableRow(this)
            if (rowCount % 2 == 0) {
                row.setBackgroundColor(Color.LTGRAY)
            } else {
                row.setBackgroundColor(Color.WHITE)
            }

            // create a new TextView for showing file data
            val t = TextView(this)
            // give user feedback that he has clicked
            t.isHapticFeedbackEnabled = true
            // remove trailing .pdf
            val nfileName = fileName.replace(".txt".toRegex(), "")
            t.text = nfileName
            t.textSize = resources.getDimension(R.dimen.textsizeList)

            // add the TextView to the new TableRow
            row.addView(t)
            row.setPadding(2, 5, 2, 5)
            // Add click listener to launch external pdf viewer (most likely Google drive)
            row.setOnClickListener { // copy the file to external storage accessible by all
                copyFileFromAssets(fileName)
                /** PDF reader code  */
                /** PDF reader code  */
                val file = File(
                    tmpFolder
                            + "/"
                            + fileName
                )
                val uri = FileProvider.getUriForFile(
                    this@MainActivity,
                    getString(R.string.file_provider_authority),
                    file
                )
                Log.i(TAG, "Launching viewer " + fileName + " " + file.absolutePath)

                //Intent intent = new Intent(Intent.ACTION_VIEW, FileProvider.getUriForFile(v.getContext(), "org.eicsanjose.quranbasic.fileprovider", file));
                val intent = Intent(Intent.ACTION_VIEW)
                //intent.setDataAndType(Uri.fromFile(file), "application/pdf");
                intent.setDataAndType(uri, "application/txt")
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                try {
                    Log.i(TAG, "Staring pdf viewer activity")
                    startActivity(intent)
                } catch (e: ActivityNotFoundException) {
                    Log.e(TAG, e.message!!)
                }
            }
            // add the TableRow to the TableLayout
            table.addView(
                row, TableLayout.LayoutParams(
                    TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.MATCH_PARENT
                )
            )
        }
    }


    /**
     * Helper method to copy a file from assets folder to a tmp folder which can be accessed by other
     * applications.
     * @param fileName name of the file to copy
     */
    private fun copyFileFromAssets(fileName: String) {
        Log.i(TAG, "Copy file from asset:$fileName")
        val assetManager = this.assets


        // file to copy to from assets
        val cacheFile = File("$tmpFolder/$fileName")
        var `in`: InputStream? = null
        var out: OutputStream? = null
        try {
            Log.d(TAG, "Copying from assets folder to cache folder")
            if (cacheFile.exists()) {
                // already there. Do not copy
                Log.d(TAG, "Cache file exists at:" + cacheFile.absolutePath)
                return
            } else {
                Log.d(TAG, "Cache file does NOT exist at:" + cacheFile.absolutePath)
                // TODO: There should be some error catching/validation etc before proceeding
                `in` = assetManager.open(fileName)
                out = FileOutputStream(cacheFile)
                copyFile(`in`, out)
                `in`.close()
                `in` = null
                out.flush()
                out.close()
                out = null
            }
        } catch (ioe: IOException) {
            Log.e(TAG, "Error in copying file from assets $fileName")
            ioe.printStackTrace()
        }
    }

    /**
     * Helper method to copy file from origin to target
     * @param in InputStream of the original file
     * @param out OutputStream of the destination file
     * @throws IOException
     */
    @Throws(IOException::class)
    private fun copyFile(`in`: InputStream, out: OutputStream) {
        val buffer = ByteArray(1024)
        var read: Int
        while (`in`.read(buffer).also { read = it } != -1) {
            out.write(buffer, 0, read)
        }
    }

    /**
     * This method will return all pdf files found in the assets folder. Note: will not traverse
     * nested directory. Returned list is not sorted
     * @return List of file names
     */
    private fun getPDFFromAssets(): List<String> {
        val pdfFiles: MutableList<String> = ArrayList()
        val assetManager = this.assets
        try {
            for (name in assetManager.list("")!!) {
                // include files which end with pdf only
                if (name.lowercase(Locale.getDefault()).endsWith("pdf")) {
                    pdfFiles.add(name)
                }
            }
        } catch (ioe: IOException) {
            val mesg = "Could not read files from assets folder"
            Log.e(TAG, mesg)
            Toast.makeText(
                this@MainActivity,
                mesg,
                Toast.LENGTH_LONG
            )
                .show()
        }
        return pdfFiles
    }

    // THis is invoked upon grant/denying the request for permission
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super
            .onRequestPermissionsResult(
                requestCode,
                permissions,
                grantResults
            )
        Log.d(
            TAG,
            "Request for write permission to external storage result:" + permissions[0] + " " + grantResults[0]
        )
        // Now let us make sure our cache dir exists. This would not work if user denied. But then again
        // in that case the whole app will not work. Add error checking
        val tmpDir = File(tmpFolder)
        if (!tmpDir.exists()) {
            Log.d(TAG, "Tmp dir to store pdf does not exist")
            tmpDir.mkdir()
            Log.d(TAG, "Tmpdir created " + tmpDir.exists())
        } else {
            Log.d(TAG, "Tmpdir already exists " + tmpDir.exists())
        }
    }
}