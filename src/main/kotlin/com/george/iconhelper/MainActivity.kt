package com.george.iconhelper

import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.HorizontalScrollView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.george.iconhelper.extraction.CategoryExtractor
import com.george.iconhelper.extraction.IconExtractor
import com.george.iconhelper.extraction.LabelExtractor
import com.george.iconhelper.storage.ExportWriter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class MainActivity : AppCompatActivity() {

    private lateinit var exportButton: Button
    private lateinit var statusText: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var iconScrollView: HorizontalScrollView
    private lateinit var iconInnerLayout: LinearLayout
    private var iconScrollPending = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val rootLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setBackgroundColor(Color.WHITE)
            setPadding(16.dpToPx(), 16.dpToPx(), 16.dpToPx(), 16.dpToPx())
        }

        val versionText = TextView(this).apply {
            text = "v${BuildConfig.VERSION_NAME}"
            textSize = 12f
            setTextColor(Color.GRAY)
            gravity = Gravity.CENTER
        }
        rootLayout.addView(versionText, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply { bottomMargin = 4.dpToPx() })

        val titleText = TextView(this).apply {
            text = getString(R.string.export_title)
            textSize = 24f
            setTextColor(Color.BLACK)
        }
        rootLayout.addView(titleText, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply { bottomMargin = 16.dpToPx() })

        progressBar = ProgressBar(this, null, android.R.attr.progressBarStyle).apply {
            visibility = android.view.View.GONE
        }
        rootLayout.addView(progressBar, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply { bottomMargin = 16.dpToPx() })

        statusText = TextView(this).apply {
            text = getString(R.string.ready_to_export)
            textSize = 16f
            setTextColor(Color.DKGRAY)
        }
        rootLayout.addView(statusText, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply { bottomMargin = 16.dpToPx() })

        iconInnerLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        iconScrollView = HorizontalScrollView(this).apply {
            isHorizontalScrollBarEnabled = false
            importantForAccessibility = android.view.View.IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS
            visibility = android.view.View.GONE
            addView(iconInnerLayout, LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            ))
        }
        rootLayout.addView(iconScrollView, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            72.dpToPx()  // 72dp strip height — tall enough for 56dp icons with 8dp vertical breathing room
        ).apply { bottomMargin = 16.dpToPx() })

        exportButton = Button(this).apply {
            text = getString(R.string.export_button)
            setOnClickListener {
                requestPermissionsAndExport()
            }
        }
        rootLayout.addView(exportButton, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ))

        setContentView(rootLayout)
    }

    private fun Int.dpToPx(): Int = (this * resources.displayMetrics.density).toInt()

    private fun requestPermissionsAndExport() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            performExport()
            return
        }
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE),
                101
            )
            return
        }
        performExport()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 101 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            performExport()
        } else if (requestCode == 101) {
            statusText.text = getString(R.string.permission_required)
        }
    }

    private fun performExport() {
        exportButton.isEnabled = false
        progressBar.visibility = android.view.View.VISIBLE
        statusText.text = getString(R.string.exporting)
        iconInnerLayout.removeAllViews()
        iconScrollView.visibility = android.view.View.GONE
        iconScrollPending = false

        lifecycleScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    val pm = packageManager
                    val packages = pm.getInstalledApplications(0)
                        .filter { it.packageName != packageName }

                    val iconExtractor = IconExtractor(this@MainActivity)
                    val labelExtractor = LabelExtractor(this@MainActivity)
                    val categoryExtractor = CategoryExtractor()
                    val exportWriter = ExportWriter(this@MainActivity)

                    exportWriter.clearExportDirectory()

                    val labels = mutableMapOf<String, String>()
                    val categories = mutableMapOf<String, String>()
                    var iconsExtracted = 0
                    val total = packages.size

                    for ((index, appInfo) in packages.withIndex()) {
                        val pkg = appInfo.packageName

                        labels[pkg] = labelExtractor.getLabel(appInfo)

                        val category = categoryExtractor.getCategory(appInfo)
                        if (category != null) categories[pkg] = category

                        val iconFile = File(exportWriter.getExportDirectory(), "$pkg.png")
                        if (iconExtractor.extractIconToFile(appInfo, iconFile)) {
                            iconsExtracted++
                            val bitmap = BitmapFactory.decodeFile(iconFile.absolutePath)
                            if (bitmap != null) {
                                withContext(Dispatchers.Main) {
                                    if (iconScrollView.visibility == android.view.View.GONE) {
                                        iconScrollView.visibility = android.view.View.VISIBLE
                                    }
                                    if (iconInnerLayout.childCount >= 50) {
                                        (iconInnerLayout.getChildAt(0) as? ImageView)?.setImageDrawable(null)
                                        iconInnerLayout.removeViewAt(0)
                                    }
                                    val iv = ImageView(this@MainActivity).apply {
                                        scaleType = ImageView.ScaleType.CENTER_CROP
                                        setImageBitmap(bitmap)
                                    }
                                    iconInnerLayout.addView(iv, LinearLayout.LayoutParams(
                                        56.dpToPx(), 56.dpToPx()
                                    ).apply {
                                        marginStart = 4.dpToPx()
                                        marginEnd = 4.dpToPx()
                                    })
                                    if (!iconScrollPending) {
                                        iconScrollPending = true
                                        iconScrollView.post {
                                            iconScrollView.fullScroll(android.view.View.FOCUS_RIGHT)
                                            iconScrollPending = false
                                        }
                                    }
                                }
                            }
                        }

                        if (index % 10 == 0) {
                            withContext(Dispatchers.Main) {
                                statusText.text = getString(R.string.exporting_progress, index + 1, total)
                            }
                        }
                    }

                    exportWriter.writeLabelsToJson(labels)
                    exportWriter.writeCategoriesToJson(categories)

                    withContext(Dispatchers.Main) {
                        progressBar.visibility = android.view.View.GONE
                        statusText.text = getString(R.string.export_complete, iconsExtracted, labels.size)
                        exportButton.isEnabled = true
                    }
                }
            } catch (e: Throwable) {
                withContext(Dispatchers.Main) {
                    progressBar.visibility = android.view.View.GONE
                    statusText.text = getString(R.string.export_failed, e.message)
                    exportButton.isEnabled = true
                }
                e.printStackTrace()
            }
        }
    }
}
