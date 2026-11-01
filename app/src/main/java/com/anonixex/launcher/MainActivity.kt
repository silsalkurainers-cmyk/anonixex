package com.anonixex.launcher

import android.app.Activity
import android.app.role.RoleManager
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity

class MainActivity : Activity() {
    private val prefs by lazy { getSharedPreferences("anonixex", MODE_PRIVATE) }
    private val bg = Color.rgb(8, 10, 13)
    private val panel = Color.rgb(19, 23, 29)
    private val muted = Color.rgb(151, 160, 172)
    private val accents = intArrayOf(Color.rgb(184, 243, 107), Color.rgb(116, 181, 255), Color.rgb(194, 151, 255), Color.rgb(255, 174, 117))
    private var accent = accents[0]
    private var columns = 4
    private var showLabels = true
    private lateinit var root: LinearLayout
    private lateinit var appGrid: GridLayout
    private lateinit var search: EditText
    private var apps: List<Launchable> = emptyList()

    private data class Launchable(val title: String, val packageName: String, val icon: android.graphics.drawable.Drawable)

    private val wallpaperPicker = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                try {
                    contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    prefs.edit().putString("wallpaper", uri.toString()).apply()
                    applyWallpaper(uri)
                } catch (_: Exception) {
                    Toast.makeText(this, "Could not use that image", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        accent = accents[prefs.getInt("accent", 0).coerceIn(accents.indices)]
        columns = prefs.getInt("columns", 4).coerceIn(3, 6)
        showLabels = prefs.getBoolean("labels", true)
        buildUi()
        prefs.getString("wallpaper", null)?.let { applyWallpaper(Uri.parse(it)) }
    }

    override fun onResume() {
        super.onResume()
        loadApps()
    }

    private fun buildUi() {
        root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(22), dp(26), dp(22), dp(12))
            setBackgroundColor(bg)
        }
        val header = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL }
        val brand = TextView(this).apply {
            text = "anonixex"
            textSize = 29f
            setTextColor(Color.WHITE)
            typeface = Typeface.create("sans-serif", Typeface.BOLD)
            letterSpacing = .02f
        }
        header.addView(brand, LinearLayout.LayoutParams(0, -2, 1f))
        header.addView(actionButton("Customize") { showSettings() })
        root.addView(header)

        val date = TextView(this).apply {
            text = "YOUR PHONE. YOUR SPACE."
            textSize = 10f
            letterSpacing = .18f
            setTextColor(muted)
            setPadding(0, dp(5), 0, dp(18))
        }
        root.addView(date)

        search = EditText(this).apply {
            hint = "Search apps"
            textSize = 15f
            setSingleLine(true)
            setTextColor(Color.WHITE)
            setHintTextColor(muted)
            setPadding(dp(16), 0, dp(16), 0)
            background = rounded(panel, dp(16))
            addTextChangedListener(SimpleTextWatcher { renderApps(it) })
        }
        root.addView(search, LinearLayout.LayoutParams(-1, dp(52)))

        val label = TextView(this).apply {
            text = "YOUR APPS"
            textSize = 11f
            letterSpacing = .16f
            setTextColor(muted)
            setPadding(2, dp(22), 0, dp(12))
        }
        root.addView(label)
        val scroll = ScrollView(this)
        appGrid = GridLayout(this).apply { alignmentMode = GridLayout.ALIGN_BOUNDS; useDefaultMargins = false }
        scroll.addView(appGrid)
        root.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))

        val footer = TextView(this).apply {
            text = "ANONIXEX  •  EARLY ACCESS"
            textSize = 10f
            letterSpacing = .12f
            gravity = Gravity.CENTER
            setTextColor(muted)
            setPadding(0, dp(12), 0, dp(4))
        }
        root.addView(footer)
        setContentView(root)
    }

    private fun loadApps() {
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        apps = packageManager.queryIntentActivities(intent, 0)
            .mapNotNull { info ->
                val activity = info.activityInfo ?: return@mapNotNull null
                if (activity.packageName == packageName) return@mapNotNull null
                Launchable(info.loadLabel(packageManager).toString(), activity.packageName, info.loadIcon(packageManager))
            }
            .distinctBy { it.packageName }
            .sortedBy { it.title.lowercase() }
        renderApps(search.text?.toString().orEmpty())
    }

    private fun renderApps(filter: String) {
        if (!::appGrid.isInitialized) return
        appGrid.removeAllViews()
        appGrid.columnCount = columns
        val shown = apps.filter { it.title.contains(filter, true) }
        shown.forEach { app ->
            val cell = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER
                setPadding(dp(4), dp(10), dp(4), dp(12))
                setOnClickListener {
                    val launch = packageManager.getLaunchIntentForPackage(app.packageName)
                    if (launch != null) startActivity(launch)
                    else Toast.makeText(this@MainActivity, "App cannot be opened", Toast.LENGTH_SHORT).show()
                }
                setOnLongClickListener {
                    val details = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:${app.packageName}"))
                    startActivity(details)
                    true
                }
            }
            val icon = ImageView(this).apply { setImageDrawable(app.icon); scaleType = ImageView.ScaleType.FIT_CENTER }
            cell.addView(icon, LinearLayout.LayoutParams(dp(48), dp(48)))
            if (showLabels) {
                cell.addView(TextView(this).apply {
                    text = app.title
                    textSize = 11f
                    gravity = Gravity.CENTER
                    maxLines = 2
                    setTextColor(Color.WHITE)
                    setPadding(0, dp(7), 0, 0)
                }, LinearLayout.LayoutParams(-1, -2))
            }
            appGrid.addView(cell, GridLayout.LayoutParams().apply {
                width = 0
                height = dp(if (showLabels) 94 else 74)
                columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
            })
        }
        if (shown.isEmpty()) {
            appGrid.addView(TextView(this).apply {
                text = if (apps.isEmpty()) "No launchable apps found yet." else "No apps match your search."
                setTextColor(muted)
                textSize = 14f
                setPadding(dp(8), dp(20), dp(8), dp(20))
            })
        }
    }

    private fun showSettings() {
        val box = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(22), dp(18), dp(22), dp(12))
            setBackground(rounded(panel, dp(22)))
        }
        val title = TextView(this).apply { text = "Personalize Anonixex"; textSize = 21f; setTextColor(Color.WHITE); typeface = Typeface.DEFAULT_BOLD }
        box.addView(title)
        box.addView(settingText("Accent color"))
        val colors = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        accents.forEachIndexed { index, color ->
            val swatch = View(this).apply {
                background = GradientDrawable().apply { shape = GradientDrawable.OVAL; setColor(color); setStroke(dp(if (accent == color) 3 else 0), Color.WHITE) }
                setOnClickListener {
                    prefs.edit().putInt("accent", index).apply()
                    accent = color
                    Toast.makeText(this@MainActivity, "Accent updated", Toast.LENGTH_SHORT).show()
                    showSettings()
                }
            }
            colors.addView(swatch, LinearLayout.LayoutParams(dp(42), dp(42)).apply { marginEnd = dp(12); topMargin = dp(8); bottomMargin = dp(8) })
        }
        box.addView(colors)
        box.addView(settingText("App grid: $columns columns"))
        val slider = SeekBar(this).apply {
            max = 3
            progress = columns - 3
            progressTintList = android.content.res.ColorStateList.valueOf(accent)
            thumbTintList = android.content.res.ColorStateList.valueOf(accent)
            setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    if (fromUser) {
                        columns = progress + 3
                        prefs.edit().putInt("columns", columns).apply()
                    }
                }
                override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                override fun onStopTrackingTouch(seekBar: SeekBar?) { renderApps(search.text.toString()) }
            })
        }
        box.addView(slider)
        box.addView(actionButton(if (showLabels) "Hide app names" else "Show app names") {
            showLabels = !showLabels
            prefs.edit().putBoolean("labels", showLabels).apply()
            renderApps(search.text.toString())
            showSettings()
        })
        box.addView(actionButton("Choose wallpaper") {
            val pick = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "image/*"
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
            }
            wallpaperPicker.launch(pick)
        })
        box.addView(actionButton("Set as default home app") {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val roleManager = getSystemService(RoleManager::class.java)
                if (roleManager.isRoleAvailable(RoleManager.ROLE_HOME) && !roleManager.isRoleHeld(RoleManager.ROLE_HOME)) {
                    startActivity(roleManager.createRequestRoleIntent(RoleManager.ROLE_HOME))
                } else {
                    startActivity(Intent(Settings.ACTION_HOME_SETTINGS))
                }
            } else startActivity(Intent(Settings.ACTION_HOME_SETTINGS))
        })
        box.addView(actionButton("Done") { setContentView(root) })
        setContentView(box)
    }

    private fun applyWallpaper(uri: Uri) {
        try {
            contentResolver.openInputStream(uri)?.use { stream ->
                val bitmap = BitmapFactory.decodeStream(stream) ?: return
                root.background = android.graphics.drawable.BitmapDrawable(resources, bitmap).apply { gravity = Gravity.CENTER_CROP }
            }
        } catch (_: Exception) {
            Toast.makeText(this, "Wallpaper unavailable", Toast.LENGTH_SHORT).show()
        }
    }

    private fun settingText(value: String) = TextView(this).apply {
        text = value
        textSize = 14f
        setTextColor(Color.WHITE)
        setPadding(0, dp(16), 0, dp(4))
    }

    private fun actionButton(label: String, action: () -> Unit) = Button(this).apply {
        text = label
        isAllCaps = false
        setTextColor(Color.WHITE)
        background = rounded(Color.rgb(37, 44, 53), dp(14))
        setOnClickListener { action() }
        val lp = LinearLayout.LayoutParams(-1, dp(48))
        lp.topMargin = dp(8)
        layoutParams = lp
    }

    private fun rounded(color: Int, radius: Int) = GradientDrawable().apply { setColor(color); cornerRadius = radius.toFloat() }
    private fun dp(value: Int) = (value * resources.displayMetrics.density).toInt()
}
