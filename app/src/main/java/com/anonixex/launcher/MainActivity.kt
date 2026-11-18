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
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.SeekBar
import android.widget.TextClock
import android.widget.TextView
import android.widget.Toast

class MainActivity : Activity() {
    private val prefs by lazy { getSharedPreferences("anonixex", MODE_PRIVATE) }
    private val bg = Color.rgb(5, 5, 5)
    private val glass = Color.rgb(25, 25, 25)
    private val glassBright = Color.rgb(43, 43, 43)
    private val muted = Color.rgb(168, 168, 168)
    private val white = Color.rgb(248, 248, 248)
    private val accents = intArrayOf(
        Color.rgb(138, 180, 248), Color.rgb(180, 145, 255),
        Color.rgb(120, 245, 194), Color.rgb(255, 171, 135),
        Color.rgb(255, 213, 119), Color.rgb(244, 133, 190),
        Color.rgb(226, 235, 255), Color.rgb(160, 190, 255)
    )
    private var accent = accents[0]
    private var columns = 4
    private var iconSize = 44
    private var fontScale = 100
    private var showLabels = true
    private var showClock = true
    private var showSearch = true
    private var glassMode = true
    private var glassOpacity = 78
    private var cornerRadius = 26
    private var islandExpanded = false
    private var swipeStartY = 0f
    private var launcherHomeVisible = false
    private var favorites: MutableSet<String> = mutableSetOf()
    private var hiddenApps: MutableSet<String> = mutableSetOf()
    private var apps: List<Launchable> = emptyList()
    private lateinit var root: LinearLayout
    private lateinit var appGrid: GridLayout
    private lateinit var search: EditText
    private lateinit var favoriteDock: LinearLayout
    private var setupStep = 0
    private val quickStates = mutableMapOf(
        "Internet" to true,
        "Bluetooth" to false,
        "Do Not Disturb" to false,
        "Flashlight" to false,
        "Airplane mode" to false,
        "Battery saver" to false
    )

    private data class Launchable(
        val title: String,
        val packageName: String,
        val icon: android.graphics.drawable.Drawable
    )

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) swipeStartY = event.rawY
        if (event.action == MotionEvent.ACTION_UP && prefs.getBoolean("setup_complete", false) &&
            launcherHomeVisible && swipeStartY > resources.displayMetrics.heightPixels * 0.70f &&
            swipeStartY - event.rawY > dp(90) && ::root.isInitialized) {
            showAppLibrary()
            swipeStartY = 0f
            return true
        }
        return super.dispatchTouchEvent(event)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = bg
        window.navigationBarColor = bg
        window.decorView.systemUiVisibility = 0
        accent = accents[prefs.getInt("accent", 0).coerceIn(accents.indices)]
        columns = prefs.getInt("columns", 4).coerceIn(3, 6)
        iconSize = prefs.getInt("icon_size", 44).coerceIn(32, 68)
        fontScale = prefs.getInt("font_scale", 100).coerceIn(85, 125)
        showLabels = prefs.getBoolean("labels", true)
        showClock = prefs.getBoolean("show_clock", true)
        showSearch = prefs.getBoolean("show_search", true)
        glassMode = prefs.getBoolean("glass_mode", true)
        glassOpacity = prefs.getInt("glass_opacity", 78).coerceIn(30, 96)
        cornerRadius = prefs.getInt("corner_radius", 26).coerceIn(14, 38)
        favorites = prefs.getStringSet("favorites", emptySet())?.toMutableSet() ?: mutableSetOf()
        quickStates.keys.forEach { key -> quickStates[key] = prefs.getBoolean("quick_$key", quickStates[key] ?: false) }
        hiddenApps = prefs.getStringSet("hidden_apps", emptySet())?.toMutableSet() ?: mutableSetOf()
        loadApps()
        if (prefs.getBoolean("setup_complete", false)) buildHome() else showSetup()
    }

    override fun onResume() {
        super.onResume()
        loadApps()
    }

    private fun showSetup() {
        val page = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(25), dp(28), dp(25), dp(25))
            background = backgroundGradient()
        }
        val body = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        body.addView(label("ANONIXEX", 11f, accent, true))
        body.addView(label("A cleaner\nAndroid home.", 36f, white, true).apply {
            setPadding(0, dp(14), 0, dp(12))
        })
        body.addView(label("Minimal by design. Yours by default.", 14f, muted, false))
        val progress = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(0, dp(25), 0, dp(22))
        }
        for (i in 0..2) {
            progress.addView(View(this).apply {
                background = rounded(if (i <= setupStep) accent else Color.rgb(54, 54, 54), dp(5))
            }, LinearLayout.LayoutParams(0, dp(4), 1f).apply { rightMargin = dp(5) })
        }
        body.addView(progress)
        when (setupStep) {
            0 -> body.addView(glassCard().apply {
                addView(label("THE BASICS", 10f, accent, true))
                addView(label("Home screen. App drawer. Quick panel.", 20f, white, true).apply {
                    setPadding(0, dp(10), 0, dp(7))
                })
                addView(label("No feed, no extra sidebar, no unnecessary panels. Anonixex works as a regular Android app.", 13f, muted, false))
            })
            1 -> {
                body.addView(label("CHOOSE AN ACCENT", 10f, accent, true))
                body.addView(label("Keep it simple.", 24f, white, true).apply {
                    setPadding(0, dp(9), 0, dp(17))
                })
                body.addView(colorPicker())
            }
            else -> body.addView(glassCard().apply {
                addView(label("READY", 10f, accent, true))
                addView(label("Your home, simplified.", 21f, white, true).apply {
                    setPadding(0, dp(10), 0, dp(7))
                })
                addView(label("You can set Anonixex as your default Home app later. Nothing is flashed or changed at system level.", 13f, muted, false))
            })
        }
        page.addView(body, LinearLayout.LayoutParams(-1, 0, 1f))
        val nav = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        if (setupStep > 0) nav.addView(actionButton("Back", glass, white) {
            setupStep--
            showSetup()
        }, LinearLayout.LayoutParams(0, dp(50), 1f).apply { rightMargin = dp(8) })
        nav.addView(actionButton(if (setupStep == 2) "Start Anonixex" else "Continue", accent, bg) {
            if (setupStep < 2) {
                setupStep++
                showSetup()
            } else {
                prefs.edit().putBoolean("setup_complete", true).apply()
                buildHome()
            }
        }, LinearLayout.LayoutParams(0, dp(50), 2f))
        page.addView(nav)
        setContentView(page)
    }

    private fun buildHome() {
        launcherHomeVisible = true
        root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(22), dp(20), dp(10))
            background = backgroundGradient()
        }

        val scroll = ScrollView(this).apply {
            clipToPadding = false
            isFillViewport = false
        }
        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        val header = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        val brand = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }
        brand.addView(label("ANONIXEX", 10f, accent, true))
        brand.addView(label("Welcome back.", 29f, white, true).apply {
            setPadding(0, dp(4), 0, 0)
        })
        header.addView(brand, LinearLayout.LayoutParams(0, -2, 1f))
        header.addView(actionButton("⌗", glass, white) {
            showControlCenter()
        }, LinearLayout.LayoutParams(dp(48), dp(48)).apply {
            rightMargin = dp(7)
        })
        header.addView(actionButton("⚙", glass, white) {
            showSettings()
        }, LinearLayout.LayoutParams(dp(48), dp(48)))
        content.addView(header)


        if (showClock) {
            val clock = glassCard().apply {
                setPadding(dp(20), dp(18), dp(20), dp(18))
            }
            clock.addView(label("TODAY", 9f, muted, true))
            clock.addView(TextClock(this).apply {
                format12Hour = "h:mm"
                format24Hour = "HH:mm"
                textSize = 52f
                setTextColor(white)
                typeface = Typeface.create("sans-serif-light", Typeface.NORMAL)
                includeFontPadding = false
            }.apply {
                setPadding(0, dp(3), 0, 0)
            })
            clock.addView(TextClock(this).apply {
                format12Hour = "EEEE, d MMMM"
                format24Hour = "EEEE, d MMMM"
                textSize = 13f
                setTextColor(muted)
            })
            content.addView(clock, LinearLayout.LayoutParams(-1, -2).apply {
                topMargin = dp(18)
            })
        }

        if (showSearch) {
            val searchPanel = glassCard().apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                setPadding(dp(15), 0, dp(14), 0)
            }
            searchPanel.addView(label("⌕", 25f, muted, false), LinearLayout.LayoutParams(dp(32), dp(54)))
            search = EditText(this).apply {
                hint = "Search apps"
                textSize = 15f * fontScale / 100f
                setSingleLine(true)
                setTextColor(white)
                setHintTextColor(muted)
                background = null
                addTextChangedListener(SimpleTextWatcher { renderApps(it) })
            }
            searchPanel.addView(search, LinearLayout.LayoutParams(0, dp(54), 1f))
            content.addView(searchPanel, LinearLayout.LayoutParams(-1, -2).apply {
                topMargin = dp(13)
            })
        }

        val favHeader = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        favHeader.addView(label("FAVOURITES", 11f, white, true), LinearLayout.LayoutParams(0, -2, 1f))
        favHeader.addView(label("Hold an app to edit", 10f, muted, false))
        content.addView(favHeader, LinearLayout.LayoutParams(-1, -2).apply {
            topMargin = dp(24)
            bottomMargin = dp(11)
        })

        favoriteDock = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        content.addView(favoriteDock)
        renderFavorites()

        val appHeader = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        appHeader.addView(label("APP LIBRARY", 11f, white, true), LinearLayout.LayoutParams(0, -2, 1f))
        appHeader.addView(actionButton("See all  ›", glass, white) {
            showAppLibrary()
        }, LinearLayout.LayoutParams(-2, dp(39)))
        content.addView(appHeader, LinearLayout.LayoutParams(-1, -2).apply {
            topMargin = dp(25)
            bottomMargin = dp(10)
        })

        appGrid = GridLayout(this).apply {
            alignmentMode = GridLayout.ALIGN_BOUNDS
            useDefaultMargins = false
        }
        content.addView(appGrid)
        scroll.addView(content)
        root.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))

        val bottom = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            background = rounded(Color.rgb(24, 24, 24), dp(24))
            setPadding(dp(7), dp(6), dp(7), dp(6))
        }
        bottom.addView(actionButton("Home", glassBright, white) {
            buildHome()
        }, LinearLayout.LayoutParams(0, dp(46), 1f))
        bottom.addView(actionButton("Apps", glass, white) {
            showAppLibrary()
        }, LinearLayout.LayoutParams(0, dp(46), 1f).apply {
            leftMargin = dp(5)
        })
        bottom.addView(actionButton("Settings", glass, white) {
            showSettings()
        }, LinearLayout.LayoutParams(0, dp(46), 1f).apply {
            leftMargin = dp(5)
        })
        root.addView(bottom, LinearLayout.LayoutParams(-1, -2).apply {
            topMargin = dp(9)
        })

        val signature = label("made by hacer62828", 8f, Color.argb(85, 220, 220, 220), false).apply {
            gravity = Gravity.END
            setPadding(0, dp(3), dp(3), 0)
        }
        root.addView(signature)
        setContentView(root)
        loadApps()
    }

    private fun showTerminal() {
        buildHome()
    }

    private fun runAnonixexCommand(raw: String): String = "This optional tool is not included in the minimal launcher."

    private fun showAppLibrary() {
        launcherHomeVisible = false
        val page = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(18), dp(25), dp(18), dp(14))
            background = backgroundGradient()
        }
        page.addView(label("ANONIXEX  /  APPS", 10f, accent, true))
        page.addView(label("Apps", 31f, white, true).apply { setPadding(0, dp(5), 0, dp(15)) })
        val input = EditText(this).apply {
            hint = "Search your phone"
            setTextColor(white)
            setHintTextColor(muted)
            background = glassCardBackground()
            setPadding(dp(15), 0, dp(15), 0)
            addTextChangedListener(SimpleTextWatcher { renderLibraryGrid(grid, it) })
        }
        page.addView(input, LinearLayout.LayoutParams(-1, dp(50)))
        val scroll = ScrollView(this)
        grid = GridLayout(this).apply { columnCount = columns; useDefaultMargins = false }
        scroll.addView(grid)
        page.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f).apply { topMargin = dp(12) })
        page.addView(actionButton("Back to home", accent, bg) { buildHome() })
        setContentView(page)
        renderLibraryGrid(grid, "")
    }

    private lateinit var grid: GridLayout

    private fun renderLibraryGrid(target: GridLayout, filter: String) {
        target.removeAllViews()
        target.columnCount = columns
        apps.filter { it.packageName !in hiddenApps && it.title.contains(filter, true) }.forEach { app ->
            target.addView(appTile(app), GridLayout.LayoutParams().apply {
                width = 0
                height = dp(if (showLabels) 106 else 82)
                columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                setMargins(dp(3), dp(3), dp(3), dp(3))
            })
        }
    }

    private fun appTile(app: Launchable): View {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            background = glassCardBackground()
            clipToOutline = true
            setPadding(dp(3), dp(9), dp(3), dp(9))
            setOnClickListener {
                packageManager.getLaunchIntentForPackage(app.packageName)?.let { startActivity(it) }
                    ?: Toast.makeText(this@MainActivity, "App cannot be opened", Toast.LENGTH_SHORT).show()
            }
            setOnLongClickListener { showAppActions(app); true }
            addView(ImageView(this@MainActivity).apply {
                setImageDrawable(app.icon)
                scaleType = ImageView.ScaleType.FIT_CENTER
            }, LinearLayout.LayoutParams(dp(iconSize), dp(iconSize)))
            if (showLabels) addView(label(app.title, 10f * fontScale / 100f, white, false).apply {
                gravity = Gravity.CENTER
                maxLines = 2
                setPadding(dp(2), dp(6), dp(2), 0)
            }, LinearLayout.LayoutParams(-1, -2))
        }
    }

    private fun renderApps(filter: String) {
        if (!::appGrid.isInitialized) return
        appGrid.removeAllViews()
        appGrid.columnCount = columns
        apps.filter { it.packageName !in hiddenApps && it.title.contains(filter, true) }.forEach { app ->
            appGrid.addView(appTile(app), GridLayout.LayoutParams().apply {
                width = 0
                height = dp(if (showLabels) 106 else 82)
                columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                setMargins(dp(3), dp(3), dp(3), dp(3))
            })
        }
    }

    private fun renderFavorites() {
        if (!::favoriteDock.isInitialized) return
        favoriteDock.removeAllViews()
        val pinned = apps.filter { it.packageName in favorites && it.packageName !in hiddenApps }.take(5)
        if (pinned.isEmpty()) {
            favoriteDock.addView(label("Hold an app in the library and pin it here.", 11f, muted, false))
            return
        }
        pinned.forEach { app ->
            favoriteDock.addView(LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER
                background = glassCardBackground()
                clipToOutline = true
                setPadding(dp(7), dp(7), dp(7), dp(7))
                setOnClickListener { packageManager.getLaunchIntentForPackage(app.packageName)?.let { startActivity(it) } }
                setOnLongClickListener { showAppActions(app); true }
                addView(ImageView(this@MainActivity).apply { setImageDrawable(app.icon) }, LinearLayout.LayoutParams(dp(36), dp(36)))
                addView(label(app.title, 9f, white, false).apply { gravity = Gravity.CENTER; maxLines = 1 })
            }, LinearLayout.LayoutParams(0, dp(69), 1f).apply { rightMargin = dp(5) })
        }
    }

    private fun loadApps() {
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        apps = packageManager.queryIntentActivities(intent, 0).mapNotNull { info ->
            val activity = info.activityInfo ?: return@mapNotNull null
            if (activity.packageName == packageName) return@mapNotNull null
            Launchable(info.loadLabel(packageManager).toString(), activity.packageName, info.loadIcon(packageManager))
        }.distinctBy { it.packageName }.sortedBy { it.title.lowercase() }
        if (::appGrid.isInitialized) renderApps(search.text?.toString().orEmpty())
        if (::favoriteDock.isInitialized) renderFavorites()
    }

    private fun showAppActions(app: Launchable) {
        val actions = arrayOf(
            if (app.packageName in favorites) "Unpin from home" else "Pin to home",
            if (app.packageName in hiddenApps) "Restore to Anonixex" else "Remove from Anonixex"
        )
        AlertDialog.Builder(this).setTitle(app.title).setItems(actions) { _, index ->
            when (index) {
                0 -> {
                    if (!favorites.add(app.packageName)) favorites.remove(app.packageName)
                    prefs.edit().putStringSet("favorites", favorites.toSet()).apply()
                    if (::favoriteDock.isInitialized) renderFavorites()
                }
                1 -> {
                    if (!hiddenApps.add(app.packageName)) hiddenApps.remove(app.packageName)
                    prefs.edit().putStringSet("hidden_apps", hiddenApps.toSet()).apply()
                    loadApps()
                }
            }
        }.show()
    }

    private fun showSettings() {
        launcherHomeVisible = false
        val page = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(19), dp(25), dp(19), dp(14))
            background = backgroundGradient()
        }
        page.addView(label("ANONIXEX", 10f, accent, true))
        page.addView(label("Settings", 31f, white, true).apply {
            setPadding(0, dp(4), 0, dp(15))
        })
        val scroll = ScrollView(this)
        val settings = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }

        val appearance = glassCard()
        appearance.addView(label("APPEARANCE", 10f, accent, true))
        appearance.addView(label("Liquid Glass", 18f, white, true).apply {
            setPadding(0, dp(8), 0, dp(5))
        })
        appearance.addView(actionButton(if (glassMode) "Glass material  ·  ON" else "Glass material  ·  OFF", glassBright, white) {
            glassMode = !glassMode
            prefs.edit().putBoolean("glass_mode", glassMode).apply()
            showSettings()
        })
        appearance.addView(colorPicker())
        settings.addView(appearance)

        val home = glassCard()
        home.addView(label("HOME SCREEN", 10f, accent, true))
        home.addView(actionButton(if (showClock) "Clock  ·  ON" else "Clock  ·  OFF", glass, white) {
            showClock = !showClock
            prefs.edit().putBoolean("show_clock", showClock).apply()
            showSettings()
        })
        home.addView(actionButton(if (showSearch) "Search  ·  ON" else "Search  ·  OFF", glass, white) {
            showSearch = !showSearch
            prefs.edit().putBoolean("show_search", showSearch).apply()
            showSettings()
        })
        home.addView(actionButton(if (showLabels) "App labels  ·  ON" else "App labels  ·  OFF", glass, white) {
            showLabels = !showLabels
            prefs.edit().putBoolean("labels", showLabels).apply()
            showSettings()
        })
        settings.addView(home, LinearLayout.LayoutParams(-1, -2).apply {
            topMargin = dp(12)
        })

        val layout = glassCard()
        layout.addView(label("APP GRID", 10f, accent, true))
        addSlider(layout, "Columns", columns, 3, 6) {
            columns = it
            prefs.edit().putInt("columns", it).apply()
        }
        addSlider(layout, "Icon size", iconSize, 32, 68) {
            iconSize = it
            prefs.edit().putInt("icon_size", it).apply()
        }
        settings.addView(layout, LinearLayout.LayoutParams(-1, -2).apply {
            topMargin = dp(12)
        })

        scroll.addView(settings)
        page.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))
        page.addView(actionButton("Done", accent, bg) {
            buildHome()
        }, LinearLayout.LayoutParams(-1, dp(50)).apply {
            topMargin = dp(10)
        })
        setContentView(page)
    }

    private fun showControlCenter() {
        launcherHomeVisible = false
        val page = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(25), dp(20), dp(16))
            background = backgroundGradient()
        }
        page.addView(label("ANONIXEX", 10f, accent, true))
        page.addView(label("Quick panel", 30f, white, true).apply {
            setPadding(0, dp(5), 0, dp(4))
        })
        page.addView(TextClock(this).apply {
            format12Hour = "h:mm  ·  EEEE, d MMMM"
            format24Hour = "HH:mm  ·  EEEE, d MMMM"
            textSize = 13f
            setTextColor(muted)
        })

        val tiles = GridLayout(this).apply {
            columnCount = 2
            useDefaultMargins = false
        }
        quickStates.keys.toList().forEach { key ->
            val enabled = quickStates[key] == true
            val tile = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER_VERTICAL
                setPadding(dp(15), dp(12), dp(12), dp(12))
                background = glassCardBackground()
                clipToOutline = true
                isClickable = true
                isFocusable = true
                addView(label(key, 14f, white, true))
                addView(label(if (enabled) "On" else "Off", 11f, muted, false).apply {
                    setPadding(0, dp(3), 0, 0)
                })
                setOnClickListener {
                    quickStates[key] = !(quickStates[key] ?: false)
                    prefs.edit().putBoolean("quick_$key", quickStates[key] == true).apply()
                    showControlCenter()
                }
            }
            tiles.addView(tile, GridLayout.LayoutParams().apply {
                width = 0
                height = dp(76)
                columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                setMargins(dp(4), dp(4), dp(4), dp(4))
            })
        }
        page.addView(tiles, LinearLayout.LayoutParams(-1, -2).apply {
            topMargin = dp(20)
        })
        page.addView(label("These switches change Anonixex's preview only. Android controls remain untouched.", 10f, muted, false).apply {
            setPadding(dp(2), dp(12), dp(2), 0)
        })
        page.addView(actionButton("Done", accent, bg) {
            buildHome()
        }, LinearLayout.LayoutParams(-1, dp(50)).apply {
            topMargin = dp(14)
        })
        setContentView(page)
    }

    private fun showKernelLab() {
        buildHome()
    }

    private fun manageHiddenApps() {
        val hidden = apps.filter { it.packageName in hiddenApps }
        if (hidden.isEmpty()) {
            AlertDialog.Builder(this).setMessage("You have no hidden apps. Long-press an app and choose Hide app.")
                .setPositiveButton("OK", null).show()
            return
        }
        val names = hidden.map { it.title }.toTypedArray()
        val checked = BooleanArray(hidden.size) { true }
        AlertDialog.Builder(this).setTitle("Hidden apps")
            .setMultiChoiceItems(names, checked) { _, which, selected -> checked[which] = selected }
            .setPositiveButton("Restore selected") { _, _ ->
                hidden.forEachIndexed { index, app -> if (checked[index]) hiddenApps.remove(app.packageName) }
                prefs.edit().putStringSet("hidden_apps", hiddenApps.toSet()).apply()
                buildHome()
            }.setNegativeButton("Cancel", null).show()
    }

    private fun colorPicker(): View {
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        accents.forEachIndexed { index, color ->
            row.addView(View(this).apply {
                background = GradientDrawable().apply {
                    shape = GradientDrawable.OVAL
                    setColor(color)
                    setStroke(dp(if (accent == color) 3 else 0), white)
                }
                setOnClickListener {
                    accent = color
                    prefs.edit().putInt("accent", index).apply()
                    if (prefs.getBoolean("setup_complete", false)) showSettings() else showSetup()
                }
            }, LinearLayout.LayoutParams(dp(31), dp(31)).apply { rightMargin = dp(7) })
        }
        return row
    }

    private fun addSlider(parent: LinearLayout, title: String, value: Int, min: Int, max: Int, changed: (Int) -> Unit) {
        parent.addView(label("$title  ·  $value", 12f, muted, false).apply { setPadding(0, dp(13), 0, dp(3)) })
        parent.addView(SeekBar(this).apply {
            this.max = max - min
            progress = value - min
            progressTintList = android.content.res.ColorStateList.valueOf(accent)
            thumbTintList = android.content.res.ColorStateList.valueOf(accent)
            setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(bar: SeekBar?, progress: Int, fromUser: Boolean) {
                    if (fromUser) changed(progress + min)
                }
                override fun onStartTrackingTouch(bar: SeekBar?) {}
                override fun onStopTrackingTouch(bar: SeekBar?) { showSettings() }
            })
        })
    }

    private fun pickWallpaper() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "image/*"
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
        }
        startActivityForResult(intent, WALLPAPER_REQUEST)
    }

    private fun requestHomeRole() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val manager = getSystemService(RoleManager::class.java)
                if (manager.isRoleAvailable(RoleManager.ROLE_HOME) && !manager.isRoleHeld(RoleManager.ROLE_HOME)) {
                    startActivity(manager.createRequestRoleIntent(RoleManager.ROLE_HOME))
                } else {
                    startActivity(Intent(Settings.ACTION_HOME_SETTINGS))
                }
            } else {
                startActivity(Intent(Settings.ACTION_HOME_SETTINGS))
            }
        } catch (_: Exception) {
            startActivity(Intent(Settings.ACTION_SETTINGS))
        }
    }

    @Deprecated("Handled for compatibility with the minimum SDK")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == WALLPAPER_REQUEST && resultCode == Activity.RESULT_OK) {
            val uri = data?.data ?: return
            try {
                contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        Toast.makeText(this, "Wallpaper applied", Toast.LENGTH_SHORT).show()
            } catch (_: Exception) {
                Toast.makeText(this, "Could not use that image", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun applyWallpaper(uri: Uri) {
        try {
            contentResolver.openInputStream(uri)?.use { stream ->
                val bitmap = BitmapFactory.decodeStream(stream) ?: return
                root.background = android.graphics.drawable.BitmapDrawable(resources, bitmap).apply {
                    gravity = Gravity.CENTER
                    alpha = 115
                }
            }
        } catch (_: Exception) {
            Toast.makeText(this, "Wallpaper unavailable", Toast.LENGTH_SHORT).show()
        }
    }

    private fun glassCard() = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(dp(16), dp(16), dp(16), dp(16))
        background = glassCardBackground()
        clipToOutline = true
        isClickable = true
    }

    private fun glassCardBackground(): GradientDrawable {
        val top = if (glassMode) Color.argb(150, 55, 58, 64) else Color.rgb(24, 24, 24)
        val bottom = if (glassMode) Color.argb(178, 18, 19, 22) else Color.rgb(24, 24, 24)
        return GradientDrawable(
            GradientDrawable.Orientation.TOP_BOTTOM,
            intArrayOf(top, bottom)
        ).apply {
            setCornerRadius(dp(cornerRadius).toFloat())
            setStroke(dp(1), Color.argb(if (glassMode) 24 else 12, 255, 255, 255))
        }
    }

    private fun backgroundGradient() = GradientDrawable(
        GradientDrawable.Orientation.TL_BR,
        intArrayOf(Color.rgb(5, 5, 5), Color.rgb(13, 13, 13), Color.rgb(5, 5, 5))
    )

    private fun label(value: String, size: Float, color: Int, bold: Boolean) = TextView(this).apply {
        text = value
        textSize = size * fontScale / 100f
        setTextColor(color)
        if (bold) typeface = Typeface.create("sans-serif", Typeface.BOLD)
    }

    private fun actionButton(textValue: String, color: Int, textColor: Int, action: () -> Unit) = Button(this).apply {
        text = textValue
        isAllCaps = false
        textSize = 12f
        typeface = Typeface.DEFAULT_BOLD
        setTextColor(textColor)
        background = rounded(color, dp(15))
        setOnClickListener { action() }
        minHeight = dp(43)
        minimumHeight = dp(43)
        stateListAnimator = null
        val p = (layoutParams as? ViewGroup.MarginLayoutParams)
        p?.bottomMargin = dp(5)
    }

    private fun rounded(color: Int, radius: Int) = GradientDrawable().apply {
        setColor(color)
        cornerRadius = radius.toFloat()
    }

    private fun dp(value: Int) = (value * resources.displayMetrics.density).toInt()

    companion object {
        private const val WALLPAPER_REQUEST = 1001
    }
}
