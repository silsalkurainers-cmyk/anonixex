package com.anonixex.launcher

import android.app.Activity
import android.app.AlertDialog
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
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.SeekBar
import android.widget.TextView
import android.widget.TextClock
import android.widget.Toast

class MainActivity : Activity() {
    private val prefs by lazy { getSharedPreferences("anonixex", MODE_PRIVATE) }
    private val bg = Color.rgb(7, 9, 13)
    private val panel = Color.rgb(18, 22, 29)
    private val panel2 = Color.rgb(25, 30, 39)
    private val muted = Color.rgb(143, 153, 168)
    private val white = Color.rgb(244, 247, 250)
    private val accents = intArrayOf(Color.rgb(185, 245, 112), Color.rgb(111, 184, 255), Color.rgb(193, 147, 255), Color.rgb(255, 170, 112))
    private var accent = accents[0]
    private var columns = 4
    private var showLabels = true
    private var showClock = true
    private var iconSize = 43
    private var favorites: MutableSet<String> = mutableSetOf()
    private var hiddenApps: MutableSet<String> = mutableSetOf()
    private lateinit var root: LinearLayout
    private lateinit var appGrid: GridLayout
    private lateinit var search: EditText
    private var apps: List<Launchable> = emptyList()
    private data class Launchable(val title: String, val packageName: String, val icon: android.graphics.drawable.Drawable)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        accent = accents[prefs.getInt("accent", 0).coerceIn(accents.indices)]
        columns = prefs.getInt("columns", 4).coerceIn(3, 6)
        showLabels = prefs.getBoolean("labels", true)
        showClock = prefs.getBoolean("show_clock", true)
        iconSize = prefs.getInt("icon_size", 43).coerceIn(32, 64)
        favorites = prefs.getStringSet("favorites", emptySet())?.toMutableSet() ?: mutableSetOf()
        hiddenApps = prefs.getStringSet("hidden_apps", emptySet())?.toMutableSet() ?: mutableSetOf()
        if (!prefs.getBoolean("setup_complete", false)) showWelcome() else buildHome()
        prefs.getString("wallpaper", null)?.let { uri -> if (::root.isInitialized) applyWallpaper(Uri.parse(uri)) }
    }

    override fun onResume() {
        super.onResume()
        if (::appGrid.isInitialized) loadApps()
    }

    private fun showWelcome() {
        val page = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(28), dp(32), dp(28), dp(28))
            setBackgroundColor(bg)
        }
        page.addView(TextView(this).apply {
            text = "A"
            textSize = 44f
            gravity = Gravity.CENTER
            setTextColor(bg)
            typeface = Typeface.DEFAULT_BOLD
            background = rounded(accent, dp(20))
        }, LinearLayout.LayoutParams(dp(76), dp(76)))
        page.addView(label("WELCOME TO", 11f, muted, true).apply { setPadding(0, dp(34), 0, dp(8)) })
        page.addView(label("anonixex", 38f, white, true))
        page.addView(label("A calmer home for everything on your phone.", 16f, muted, false).apply { setPadding(0, dp(12), 0, dp(28)) })
        val feature = card()
        feature.addView(label("MADE TO FEEL LIKE YOURS", 10f, accent, true))
        feature.addView(label("A clean app space. Your wallpaper. Your colors. No ads.", 15f, white, false).apply { setPadding(0, dp(10), 0, 0) })
        page.addView(feature, LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = dp(24) })
        val start = actionButton("Set up Anonixex", accent, bg) {
            prefs.edit().putBoolean("setup_complete", true).apply()
            buildHome()
        }
        page.addView(start)
        page.addView(label("You can change everything later in Customize.", 12f, muted, false).apply {
            gravity = Gravity.CENTER
            setPadding(0, dp(16), 0, 0)
        })
        setContentView(page)
    }

    private fun buildHome() {
        root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(28), dp(20), dp(12))
            setBackgroundColor(bg)
        }
        val scroll = ScrollView(this).apply { clipToPadding = false }
        val content = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        val top = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL }
        val brandStack = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        brandStack.addView(label("ANONIXEX", 11f, accent, true))
        brandStack.addView(label("Your space.", 29f, white, true).apply { setPadding(0, dp(2), 0, 0) })
        top.addView(brandStack, LinearLayout.LayoutParams(0, -2, 1f))
        top.addView(actionButton("Quick", panel2, white) { showQuickPanel() }, LinearLayout.LayoutParams(dp(68), dp(46)).apply { rightMargin = dp(7) })
        top.addView(actionButton("•••", panel2, white) { showSettings() }.apply { textSize = 18f }, LinearLayout.LayoutParams(dp(48), dp(46)))
        content.addView(top)

        val hero = card().apply {
            orientation = LinearLayout.VERTICAL
            background = GradientDrawable(GradientDrawable.Orientation.TL_BR, intArrayOf(Color.rgb(31, 43, 39), Color.rgb(19, 25, 31))).apply { cornerRadius = dp(24).toFloat() }
            setPadding(dp(20), dp(20), dp(20), dp(20))
        }
        hero.addView(label("A HOME THAT FITS YOU", 10f, accent, true))
        hero.addView(label("Everything in its place.", 21f, white, true).apply { setPadding(0, dp(8), 0, dp(5)) })
        hero.addView(label("Your apps, one tap away.", 13f, muted, false))
        content.addView(hero, LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(22); bottomMargin = dp(22) })

        val searchWrap = LinearLayout(this).apply {
            gravity = Gravity.CENTER_VERTICAL
            orientation = LinearLayout.HORIZONTAL
            background = rounded(panel, dp(16))
            setPadding(dp(14), 0, dp(12), 0)
        }
        searchWrap.addView(label("⌕", 24f, accent, false), LinearLayout.LayoutParams(dp(30), dp(52)))
        search = EditText(this).apply {
            hint = "Find an app"
            textSize = 15f
            setSingleLine(true)
            setTextColor(white)
            setHintTextColor(muted)
            background = null
            addTextChangedListener(SimpleTextWatcher { renderApps(it) })
        }
        searchWrap.addView(search, LinearLayout.LayoutParams(0, dp(52), 1f))
        content.addView(searchWrap)

        val section = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL; setPadding(0, dp(25), 0, dp(12)) }
        section.addView(label("YOUR APPS", 11f, muted, true), LinearLayout.LayoutParams(0, -2, 1f))
        section.addView(label("Tap to open  ·  Hold for info", 10f, muted, false))
        content.addView(section)
        appGrid = GridLayout(this).apply { alignmentMode = GridLayout.ALIGN_BOUNDS; useDefaultMargins = false }
        content.addView(appGrid)
        scroll.addView(content)
        root.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))

        val bottom = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            background = rounded(panel, dp(18))
            setPadding(dp(8), dp(7), dp(8), dp(7))
        }
        bottom.addView(actionButton("Apps", panel2, white) { search.setText(""); search.clearFocus() }, LinearLayout.LayoutParams(0, dp(44), 1f))
        bottom.addView(actionButton("Customize", accent, bg) { showSettings() }, LinearLayout.LayoutParams(0, dp(44), 1f).apply { leftMargin = dp(8) })
        root.addView(bottom, LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(10) })
        setContentView(root)
        loadApps()
    }

    private fun loadApps() {
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        apps = packageManager.queryIntentActivities(intent, 0).mapNotNull { info ->
            val activity = info.activityInfo ?: return@mapNotNull null
            if (activity.packageName == packageName) return@mapNotNull null
            Launchable(info.loadLabel(packageManager).toString(), activity.packageName, info.loadIcon(packageManager))
        }.distinctBy { it.packageName }.sortedBy { it.title.lowercase() }
        renderApps(search.text?.toString().orEmpty())
    }

    private fun renderApps(filter: String) {
        if (!::appGrid.isInitialized) return
        appGrid.removeAllViews()
        appGrid.columnCount = columns
        val shown = apps.filter { it.packageName !in hiddenApps && it.title.contains(filter, true) }
        shown.forEach { app ->
            val tile = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER
                setPadding(dp(3), dp(12), dp(3), dp(12))
                background = rounded(panel, dp(17))
                setOnClickListener {
                    packageManager.getLaunchIntentForPackage(app.packageName)?.let { startActivity(it) }
                        ?: Toast.makeText(this@MainActivity, "App cannot be opened", Toast.LENGTH_SHORT).show()
                }
                setOnLongClickListener { showAppActions(app); true }
            }
            tile.addView(ImageView(this).apply { setImageDrawable(app.icon); scaleType = ImageView.ScaleType.FIT_CENTER }, LinearLayout.LayoutParams(dp(iconSize), dp(iconSize)))
            if (showLabels) tile.addView(TextView(this).apply {
                text = app.title
                textSize = 10.5f
                gravity = Gravity.CENTER
                maxLines = 2
                setTextColor(white)
                setPadding(dp(2), dp(7), dp(2), 0)
            }, LinearLayout.LayoutParams(-1, -2))
            appGrid.addView(tile, GridLayout.LayoutParams().apply {
                width = 0
                height = dp(if (showLabels) 102 else 82)
                columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                setMargins(dp(3), dp(3), dp(3), dp(3))
            })
        }
        if (shown.isEmpty()) {
            appGrid.addView(label(if (apps.isEmpty()) "No launchable apps found." else "No apps match your search.", 14f, muted, false).apply {
                setPadding(dp(8), dp(20), 0, dp(20))
            })
        }
    }

    private fun showSettings() {
        val page = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(22), dp(28), dp(22), dp(20))
            setBackgroundColor(bg)
        }
        page.addView(label("PERSONALIZE", 11f, accent, true))
        page.addView(label("Make it yours.", 29f, white, true).apply { setPadding(0, dp(4), 0, dp(22)) })
        val scroll = ScrollView(this)
        val settings = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        val appearance = card()
        appearance.addView(label("Appearance", 18f, white, true))
        appearance.addView(label("Choose an accent", 12f, muted, false).apply { setPadding(0, dp(15), 0, dp(8)) })
        val colors = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        accents.forEachIndexed { index, color ->
            colors.addView(View(this).apply {
                background = GradientDrawable().apply { shape = GradientDrawable.OVAL; setColor(color); setStroke(dp(if (accent == color) 3 else 0), white) }
                setOnClickListener {
                    accent = color
                    prefs.edit().putInt("accent", index).apply()
                    showSettings()
                }
            }, LinearLayout.LayoutParams(dp(43), dp(43)).apply { marginEnd = dp(13) })
        }
        appearance.addView(colors)
        appearance.addView(label("App grid  ·  $columns columns", 12f, muted, false).apply { setPadding(0, dp(17), 0, 0) })
        appearance.addView(SeekBar(this).apply {
            max = 3; progress = columns - 3
            progressTintList = android.content.res.ColorStateList.valueOf(accent)
            thumbTintList = android.content.res.ColorStateList.valueOf(accent)
            setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    if (fromUser) { columns = progress + 3; prefs.edit().putInt("columns", columns).apply() }
                }
                override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                override fun onStopTrackingTouch(seekBar: SeekBar?) { showSettings() }
            })
        })
        appearance.addView(actionButton(if (showLabels) "Hide app names" else "Show app names", panel2, white) {
            showLabels = !showLabels; prefs.edit().putBoolean("labels", showLabels).apply(); showSettings()
        })
        settings.addView(appearance)
        val personalization = card()
        personalization.addView(label("Wallpaper", 18f, white, true))
        personalization.addView(label("Use an image from your phone.", 12f, muted, false).apply { setPadding(0, dp(6), 0, dp(10)) })
        personalization.addView(actionButton("Choose wallpaper", panel2, white) {
            val pick = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE); type = "image/*"
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
            }
            startActivityForResult(pick, WALLPAPER_REQUEST)
        })
        settings.addView(personalization, LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(12) })
        val system = card()
        system.addView(label("System", 18f, white, true))
        system.addView(label("Choose Anonixex as your Android home app.", 12f, muted, false).apply { setPadding(0, dp(6), 0, dp(10)) })
        system.addView(actionButton("Set as default home app", panel2, white) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val manager = getSystemService(RoleManager::class.java)
                if (manager.isRoleAvailable(RoleManager.ROLE_HOME) && !manager.isRoleHeld(RoleManager.ROLE_HOME))
                    startActivity(manager.createRequestRoleIntent(RoleManager.ROLE_HOME))
                else startActivity(Intent(Settings.ACTION_HOME_SETTINGS))
            } else startActivity(Intent(Settings.ACTION_HOME_SETTINGS))
        })
        settings.addView(system, LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(12) })
        scroll.addView(settings)
        page.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))
        page.addView(actionButton("Back to home", accent, bg) { buildHome() })
        setContentView(page)
    }

    @Deprecated("Handled for compatibility with the minimum SDK")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == WALLPAPER_REQUEST && resultCode == Activity.RESULT_OK) {
            data?.data?.let { uri ->
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

    private fun applyWallpaper(uri: Uri) {
        try {
            contentResolver.openInputStream(uri)?.use { stream ->
                val bitmap = BitmapFactory.decodeStream(stream) ?: return
                root.background = android.graphics.drawable.BitmapDrawable(resources, bitmap).apply { gravity = Gravity.CENTER }
            }
        } catch (_: Exception) {
            Toast.makeText(this, "Wallpaper unavailable", Toast.LENGTH_SHORT).show()
        }
    }

    private fun card() = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(dp(17), dp(17), dp(17), dp(17))
        background = rounded(panel, dp(20))
    }

    private fun label(value: String, size: Float, color: Int, bold: Boolean) = TextView(this).apply {
        text = value
        textSize = size
        setTextColor(color)
        if (bold) typeface = Typeface.create("sans-serif", Typeface.BOLD)
    }

    private fun actionButton(textValue: String, color: Int, textColor: Int, action: () -> Unit) = Button(this).apply {
        text = textValue
        isAllCaps = false
        textSize = 13f
        typeface = Typeface.DEFAULT_BOLD
        setTextColor(textColor)
        background = rounded(color, dp(14))
        setOnClickListener { action() }
        minHeight = dp(44)
        minimumHeight = dp(44)
        stateListAnimator = null
    }

    private fun rounded(color: Int, radius: Int) = GradientDrawable().apply {
        setColor(color)
        cornerRadius = radius.toFloat()
    }

    private fun dp(value: Int) = (value * resources.displayMetrics.density).toInt()
    companion object { private const val WALLPAPER_REQUEST = 1001 }
}
