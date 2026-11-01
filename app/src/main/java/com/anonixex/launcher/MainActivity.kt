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
    private val bg = Color.rgb(7, 9, 16)
    private val glass = Color.argb(174, 27, 34, 49)
    private val glassBright = Color.argb(205, 42, 52, 72)
    private val muted = Color.rgb(157, 170, 191)
    private val white = Color.rgb(244, 247, 255)
    private val accents = intArrayOf(
        Color.rgb(119, 224, 255), Color.rgb(180, 145, 255),
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
        val scroll = ScrollView(this)
        val body = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        body.addView(label("A N O N I X E X", 11f, accent, true))
        body.addView(label("A new dimension\nof mobile.", 37f, white, true).apply {
            setPadding(0, dp(15), 0, dp(12))
        })
        body.addView(label("Your phone. Reimagined around you.", 15f, muted, false))
        val progress = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(0, dp(25), 0, dp(22))
        }
        for (i in 0..3) {
            val segment = View(this).apply {
                background = rounded(if (i <= setupStep) accent else Color.rgb(54, 64, 83), dp(5))
            }
            progress.addView(segment, LinearLayout.LayoutParams(0, dp(4), 1f).apply {
                rightMargin = dp(5)
            })
        }
        body.addView(progress)
        when (setupStep) {
            0 -> {
                body.addView(glassCard().apply {
                    addView(label("01  /  THE EXPERIENCE", 10f, accent, true))
                    addView(label("Liquid glass. Deep space. Your rules.", 21f, white, true).apply {
                        setPadding(0, dp(12), 0, dp(7))
                    })
                    addView(label("A home screen, app library, quick controls and deep visual customization. No ads. No account. No tracking.", 14f, muted, false))
                })
                body.addView(label("Designed to work with your existing Android phone—not erase its system.", 12f, muted, false).apply {
                    setPadding(0, dp(18), 0, 0)
                })
            }
            1 -> {
                body.addView(label("02  /  PICK YOUR COLOR", 10f, accent, true))
                body.addView(label("Choose your atmosphere.", 25f, white, true).apply {
                    setPadding(0, dp(9), 0, dp(18))
                })
                body.addView(colorPicker())
                body.addView(label("You can change this any time.", 12f, muted, false).apply {
                    setPadding(0, dp(12), 0, 0)
                })
            }
            2 -> {
                body.addView(label("03  /  YOUR WALLPAPER", 10f, accent, true))
                body.addView(label("Make it yours.", 25f, white, true).apply {
                    setPadding(0, dp(9), 0, dp(8))
                })
                body.addView(glassCard().apply {
                    addView(label("Choose a photo from your gallery. It becomes the backdrop for your Anonixex home.", 14f, muted, false))
                    addView(actionButton("Choose from gallery", glassBright, white) { pickWallpaper() })
                    addView(actionButton("Keep the default background", glass, white) { Toast.makeText(this@MainActivity, "Default atmosphere selected", Toast.LENGTH_SHORT).show() })
                })
            }
            else -> {
                body.addView(label("04  /  READY WHEN YOU ARE", 10f, accent, true))
                body.addView(label("Your phone.\nYour universe.", 31f, white, true).apply {
                    setPadding(0, dp(10), 0, dp(10))
                })
                body.addView(glassCard().apply {
                    addView(label("Set Anonixex as your default Home app to use it when you press Home. You can do this later in Settings.", 14f, muted, false))
                    addView(actionButton("Choose default Home app", glassBright, white) { requestHomeRole() })
                    addView(actionButton("Set Dynamic Island app", glass, white) { configureIsland() })
                    addView(actionButton("Set Dynamic Island link", glass, white) { configureIslandLink() })
                })
            }
        }
        scroll.addView(body)
        page.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))
        val nav = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL }
        if (setupStep > 0) nav.addView(actionButton("Back", glass, white) {
            setupStep--
            showSetup()
        }, LinearLayout.LayoutParams(0, dp(52), 1f).apply { rightMargin = dp(8) })
        nav.addView(actionButton(if (setupStep == 3) "Enter Anonixex" else "Continue", accent, bg) {
            if (setupStep < 3) {
                setupStep++
                showSetup()
            } else {
                prefs.edit().putBoolean("setup_complete", true).apply()
                buildHome()
            }
        }, LinearLayout.LayoutParams(0, dp(52), 2f))
        page.addView(nav, LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(15) })
        setContentView(page)
    }

    private fun buildHome() {
        launcherHomeVisible = true
        root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(17), dp(23), dp(17), dp(11))
            background = backgroundGradient()
        }
        val scroll = ScrollView(this).apply { clipToPadding = false; isFillViewport = false }
        val content = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        val header = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL }
        val brand = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        brand.addView(label("A N O N I X E X", 10f, accent, true))
        brand.addView(label("Good to see you.", 26f, white, true).apply { setPadding(0, dp(3), 0, 0) })
        header.addView(brand, LinearLayout.LayoutParams(0, -2, 1f))
        header.addView(actionButton("◉", glass, white) { showControlCenter() }, LinearLayout.LayoutParams(dp(48), dp(46)).apply { rightMargin = dp(6) })
        header.addView(actionButton("⚙", glass, white) { showSettings() }, LinearLayout.LayoutParams(dp(48), dp(46)))
        content.addView(header)
        addDynamicIsland(root)

        if (showClock) {
            val clock = glassCard().apply { setPadding(dp(18), dp(14), dp(18), dp(14)) }
            clock.addView(TextClock(this).apply {
                format12Hour = "h:mm"
                format24Hour = "HH:mm"
                textSize = 42f
                setTextColor(white)
                typeface = Typeface.create("sans-serif-light", Typeface.NORMAL)
            })
            clock.addView(TextClock(this).apply {
                format12Hour = "EEEE  ·  d MMMM"
                format24Hour = "EEEE  ·  d MMMM"
                textSize = 12f
                setTextColor(muted)
            })
            content.addView(clock, LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(19) })
        }

        if (showSearch) {
            val searchPanel = glassCard().apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                setPadding(dp(13), 0, dp(12), 0)
            }
            searchPanel.addView(label("⌕", 25f, accent, false), LinearLayout.LayoutParams(dp(31), dp(50)))
            search = EditText(this).apply {
                hint = "Search apps"
                textSize = 14f * fontScale / 100f
                setSingleLine(true)
                setTextColor(white)
                setHintTextColor(muted)
                background = null
                addTextChangedListener(SimpleTextWatcher { renderApps(it) })
            }
            searchPanel.addView(search, LinearLayout.LayoutParams(0, dp(50), 1f))
            searchPanel.addView(label("⌘", 16f, muted, true))
            content.addView(searchPanel, LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(14) })
        }

        val favHeader = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL }
        favHeader.addView(label("PINNED", 10f, muted, true), LinearLayout.LayoutParams(0, -2, 1f))
        favHeader.addView(label("Hold an app to edit", 10f, muted, false))
        content.addView(favHeader, LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(22); bottomMargin = dp(9) })
        favoriteDock = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        content.addView(favoriteDock)
        renderFavorites()

        val appHeader = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL }
        appHeader.addView(label("APP LIBRARY", 10f, muted, true), LinearLayout.LayoutParams(0, -2, 1f))
        appHeader.addView(actionButton("All apps  ↗", glass, white) { showAppLibrary() }, LinearLayout.LayoutParams(-2, dp(38)))
        content.addView(appHeader, LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(23); bottomMargin = dp(9) })
        appGrid = GridLayout(this).apply { alignmentMode = GridLayout.ALIGN_BOUNDS; useDefaultMargins = false }
        content.addView(appGrid)
        scroll.addView(content)
        root.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))

        val bottom = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            background = glassCardBackground()
            setPadding(dp(7), dp(6), dp(7), dp(6))
        }
        bottom.addView(actionButton("Home", glassBright, white) { buildHome() }, LinearLayout.LayoutParams(0, dp(44), 1f))
        bottom.addView(actionButton("Apps", glass, white) { showAppLibrary() }, LinearLayout.LayoutParams(0, dp(44), 1f).apply { leftMargin = dp(5) })
        bottom.addView(actionButton("Studio", accent, bg) { showSettings() }, LinearLayout.LayoutParams(0, dp(44), 1f).apply { leftMargin = dp(5) })
        root.addView(bottom, LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(9) })
        val signature = label("made by hacer62828", 8f, Color.argb(85, 220, 230, 245), false).apply {
            gravity = Gravity.END
            setPadding(0, dp(3), dp(3), 0)
        }
        root.addView(signature)
        setContentView(root)
        loadApps()
        prefs.getString("wallpaper", null)?.let { applyWallpaper(Uri.parse(it)) }
    }

    private fun addDynamicIsland(container: LinearLayout) {
        val wrap = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; gravity = Gravity.CENTER; setPadding(0, dp(1), 0, dp(8)) }
        val island = TextView(this).apply {
            text = "●   ●"; textSize = 10f; gravity = Gravity.CENTER; setTextColor(accent)
            background = GradientDrawable().apply { setColor(Color.rgb(1, 3, 8)); cornerRadius = dp(24).toFloat(); setStroke(dp(1), Color.argb(110, 220, 240, 255)) }
            elevation = dp(8).toFloat(); setPadding(dp(25), dp(7), dp(25), dp(7))
        }
        val panel = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; background = glassCardBackground(); setPadding(dp(13), dp(10), dp(13), dp(10)); visibility = if (islandExpanded) View.VISIBLE else View.GONE }
        panel.addView(label("QUICK ACCESS", 9f, accent, true))
        val selected = apps.firstOrNull { it.packageName == prefs.getString("island_app", null) }
        if (selected != null) panel.addView(actionButton("Open " + selected.title, glassBright, white) {
            packageManager.getLaunchIntentForPackage(selected.packageName)?.let { startActivity(it) }
        })
        val savedUrl = prefs.getString("island_url", "").orEmpty()
        if (savedUrl.isNotBlank()) panel.addView(actionButton("Open saved link", glass, white) {
            try { startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(savedUrl))) } catch (_: Exception) { Toast.makeText(this, "Could not open link", Toast.LENGTH_SHORT).show() }
        })
        if (selected == null && savedUrl.isBlank()) panel.addView(label("Set an app or link in Studio.", 11f, muted, false))
        val detector = GestureDetector(this, object : GestureDetector.SimpleOnGestureListener() {
            override fun onDoubleTap(e: MotionEvent): Boolean {
                islandExpanded = !islandExpanded
                panel.visibility = if (islandExpanded) View.VISIBLE else View.GONE
                return true
            }
        })
        island.setOnTouchListener { _, event -> detector.onTouchEvent(event); true }
        wrap.addView(island, LinearLayout.LayoutParams(dp(122), dp(31)))
        wrap.addView(panel, LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(8) })
        container.addView(wrap, 0, LinearLayout.LayoutParams(-1, -2))
    }

    private fun configureIsland() {
        val names = apps.map { it.title }.toTypedArray()
        val packages = apps.map { it.packageName }
        AlertDialog.Builder(this).setTitle("Dynamic Island · Quick app")
            .setItems(names) { _, which ->
                prefs.edit().putString("island_app", packages[which]).apply()
                configureIslandLink()
            }.setNeutralButton("Clear app") { _, _ -> prefs.edit().remove("island_app").apply() }.setNegativeButton("Cancel", null).show()
    }

    private fun configureIslandLink() {
        val input = EditText(this).apply { hint = "https://example.com"; setSingleLine(true); setText(prefs.getString("island_url", "")); setTextColor(white); setHintTextColor(muted) }
        AlertDialog.Builder(this).setTitle("Dynamic Island · Quick link").setView(input)
            .setPositiveButton("Save") { _, _ ->
                val value = input.text.toString().trim()
                if (value.isBlank() || value.startsWith("https://") || value.startsWith("http://")) {
                    prefs.edit().putString("island_url", value).apply()
                    Toast.makeText(this, "Dynamic Island actions saved", Toast.LENGTH_SHORT).show()
                } else Toast.makeText(this, "Use an http or https link", Toast.LENGTH_SHORT).show()
            }.setNegativeButton("Cancel", null).show()
    }

    private fun showTerminal() {
        launcherHomeVisible = false
        val page = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(18), dp(25), dp(18), dp(15)); background = backgroundGradient() }
        page.addView(label("ANONIXEX CORE", 10f, accent, true))
        page.addView(label("Terminal", 29f, white, true).apply { setPadding(0, dp(6), 0, dp(12)) })
        val output = TextView(this).apply {
            text = "Anonixex shell 0.1 · isolated command environment\nType 'help' to list supported commands.\n\n"
            textSize = 11f; setTextColor(Color.rgb(153, 232, 197)); typeface = Typeface.MONOSPACE
            setPadding(dp(12), dp(12), dp(12), dp(12)); background = rounded(Color.rgb(3, 7, 13), dp(14))
        }
        val scroll = ScrollView(this); scroll.addView(output)
        page.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))
        val input = EditText(this).apply { hint = "anonixex $ help"; setSingleLine(true); setTextColor(white); setHintTextColor(muted); typeface = Typeface.MONOSPACE }
        page.addView(input, LinearLayout.LayoutParams(-1, dp(52)).apply { topMargin = dp(10) })
        page.addView(actionButton("Run command", accent, bg) {
            val raw = input.text.toString().trim()
            if (raw.isNotBlank()) { if (raw == "clear") { output.text = "" } else { output.append("\nanonixex $ " + raw + "\n"); output.append(runAnonixexCommand(raw)); output.append("\n") }; input.setText(""); scroll.post { scroll.fullScroll(View.FOCUS_DOWN) } }
        })
        page.addView(label("Commands run inside Anonixex only; arbitrary host shell commands are not executed.", 10f, muted, false))
        page.addView(actionButton("Back to Studio", glassBright, white) { showSettings() })
        setContentView(page)
    }

    private fun runAnonixexCommand(raw: String): String {
        val parts = raw.trim().split(Regex("\\s+"), limit = 2)
        val command = parts.firstOrNull()?.lowercase().orEmpty()
        val arg = parts.getOrNull(1).orEmpty()
        return when (command) {
            "help" -> "help, clear, neofetch, uname, date, uptime, apps, open <name>, theme list, theme <color>, wallpaper, settings, about, ls, pwd, whoami, free, lscpu, pkg list"
            "clear" -> ""
            "neofetch" -> "ANONIXEX · Android " + Build.VERSION.RELEASE + "\nCPU ABI: " + (Build.SUPPORTED_ABIS.firstOrNull() ?: "unknown")
            "uname" -> "Anonixex-runtime 0.1 · Android userspace"
            "date" -> java.util.Date().toString()
            "uptime" -> "System uptime: " + (android.os.SystemClock.elapsedRealtime() / 1000) + " seconds"
            "apps" -> apps.joinToString(" · ") { it.title }
            "open" -> {
                val app = apps.firstOrNull { it.title.equals(arg, true) || it.title.contains(arg, true) }
                if (app == null) "App not found: " + arg else {
                    packageManager.getLaunchIntentForPackage(app.packageName)?.let { startActivity(it) }
                    "Launching " + app.title
                }
            }
            "theme" -> {
                val colors = mapOf("cyan" to 0, "violet" to 1, "mint" to 2, "peach" to 3, "amber" to 4, "pink" to 5, "ice" to 6, "blue" to 7)
                if (arg == "list") colors.keys.joinToString(" · ") else if (colors.containsKey(arg)) {
                    accent = accents[colors.getValue(arg)]; prefs.edit().putInt("accent", colors.getValue(arg)).apply(); "Accent set to " + arg
                } else "Usage: theme list | theme cyan"
            }
            "wallpaper" -> if (prefs.contains("wallpaper")) "Custom wallpaper is active." else "No custom wallpaper selected."
            "settings" -> { showSettings(); "Opening Anonixex Studio" }
            "about" -> "Anonixex phone-first launcher · isolated command runtime · no separate kernel"
            "ls" -> "home/  apps/  settings/  themes/  wallpaper/"
            "pwd" -> "/anonixex"
            "whoami" -> "user"
            "free" -> "App runtime free memory: " + (Runtime.getRuntime().freeMemory() / 1024 / 1024) + " MB"
            "lscpu" -> "CPU ABI: " + Build.SUPPORTED_ABIS.joinToString() + " · cores: " + Runtime.getRuntime().availableProcessors()
            "pkg" -> if (arg == "list") "anonixex-shell  anonixex-launcher  glass-theme  dynamic-island" else "Local component registry preview; no system packages installed."
            else -> "Command not found: " + command + ". Type help."
        }
    }

    private fun showAppLibrary() {
        launcherHomeVisible = false
        val page = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(18), dp(25), dp(18), dp(14))
            background = backgroundGradient()
        }
        page.addView(label("ANONIXEX  /  LIBRARY", 10f, accent, true))
        page.addView(label("Your apps.", 29f, white, true).apply { setPadding(0, dp(5), 0, dp(15)) })
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
            if (app.packageName in favorites) "Remove from pinned" else "Pin to home",
            if (app.packageName in hiddenApps) "Unhide app" else "Hide app from Anonixex",
            "App information"
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
                2 -> startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + app.packageName)))
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
        page.addView(label("ANONIXEX STUDIO", 10f, accent, true))
        page.addView(label("Make it yours.", 29f, white, true).apply { setPadding(0, dp(4), 0, dp(15)) })
        val scroll = ScrollView(this)
        val settings = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        val look = glassCard()
        look.addView(label("01  /  VISUAL IDENTITY", 10f, accent, true))
        look.addView(label("Accent palette", 18f, white, true).apply { setPadding(0, dp(8), 0, dp(10)) })
        look.addView(colorPicker())
        look.addView(actionButton(if (glassMode) "Liquid glass: ON" else "Liquid glass: OFF", glassBright, white) {
            glassMode = !glassMode
            prefs.edit().putBoolean("glass_mode", glassMode).apply()
            showSettings()
        })
        look.addView(actionButton(if (showLabels) "App labels: ON" else "App labels: OFF", glass, white) {
            showLabels = !showLabels
            prefs.edit().putBoolean("labels", showLabels).apply()
            showSettings()
        })
        look.addView(actionButton(if (showClock) "Home clock: ON" else "Home clock: OFF", glass, white) {
            showClock = !showClock
            prefs.edit().putBoolean("show_clock", showClock).apply()
            buildHome()
        })
        look.addView(actionButton(if (showSearch) "Home search: ON" else "Home search: OFF", glass, white) {
            showSearch = !showSearch
            prefs.edit().putBoolean("show_search", showSearch).apply()
            buildHome()
        })
        settings.addView(look)
        val glassTuning = glassCard()
        glassTuning.addView(label("GLASS MATERIAL", 10f, accent, true))
        addSlider(glassTuning, "Glass opacity (%)", glassOpacity, 30, 96) { glassOpacity = it; prefs.edit().putInt("glass_opacity", it).apply() }
        addSlider(glassTuning, "Corner roundness", cornerRadius, 14, 38) { cornerRadius = it; prefs.edit().putInt("corner_radius", it).apply() }
        settings.addView(glassTuning, LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(12) })

        val layout = glassCard()
        layout.addView(label("02  /  LAYOUT & TYPOGRAPHY", 10f, accent, true))
        addSlider(layout, "App columns", columns, 3, 6) { columns = it; prefs.edit().putInt("columns", it).apply() }
        addSlider(layout, "Icon size", iconSize, 32, 68) { iconSize = it; prefs.edit().putInt("icon_size", it).apply() }
        addSlider(layout, "Text scale (%)", fontScale, 85, 125) { fontScale = it; prefs.edit().putInt("font_scale", it).apply() }
        settings.addView(layout, LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(12) })

        val wallpaper = glassCard()
        wallpaper.addView(label("03  /  YOUR BACKDROP", 10f, accent, true))
        wallpaper.addView(label("Wallpaper studio", 18f, white, true).apply { setPadding(0, dp(7), 0, dp(8)) })
        wallpaper.addView(actionButton("Choose photo from gallery", glassBright, white) { pickWallpaper() })
        wallpaper.addView(actionButton("Remove custom wallpaper", glass, white) {
            prefs.edit().remove("wallpaper").apply()
            buildHome()
        })
        settings.addView(wallpaper, LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(12) })

        val privacy = glassCard()
        privacy.addView(label("04  /  APP LIBRARY", 10f, accent, true))
        privacy.addView(actionButton("Manage hidden apps", glass, white) { manageHiddenApps() })
        privacy.addView(actionButton("Restore default layout", glass, white) {
            AlertDialog.Builder(this).setTitle("Reset Anonixex appearance?")
                .setMessage("Your wallpaper, pinned apps and hidden apps will also be cleared.")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Reset") { _, _ ->
                    prefs.edit().clear().apply()
                    accent = accents[0]; columns = 4; iconSize = 44; fontScale = 100
                    showLabels = true; showClock = true; showSearch = true; glassMode = true
                    favorites.clear(); hiddenApps.clear()
                    buildHome()
                }.show()
        })
        privacy.addView(actionButton("Choose Dynamic Island app", glassBright, white) { configureIsland() })
        privacy.addView(actionButton("Set Dynamic Island link", glass, white) { configureIslandLink() })
        privacy.addView(actionButton("Open Anonixex Terminal", glassBright, white) { showTerminal() })
        settings.addView(privacy, LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(12) })

        val system = glassCard()
        system.addView(label("05  /  DEVICE & OS", 10f, accent, true))
        system.addView(actionButton("Set Anonixex as Home app", glassBright, white) { requestHomeRole() })
        system.addView(actionButton("Android system settings", glass, white) { startActivity(Intent(Settings.ACTION_SETTINGS)) })
        system.addView(actionButton("Notifications", glass, white) { startActivity(Intent("android.settings.NOTIFICATION_SETTINGS")) })
        system.addView(actionButton("Display & brightness", glass, white) { startActivity(Intent(Settings.ACTION_DISPLAY_SETTINGS)) })
        system.addView(actionButton("Sound & vibration", glass, white) { startActivity(Intent(Settings.ACTION_SOUND_SETTINGS)) })
        system.addView(actionButton("Battery", glass, white) { startActivity(Intent(Intent.ACTION_POWER_USAGE_SUMMARY)) })
        system.addView(actionButton("Privacy", glass, white) { startActivity(Intent(Settings.ACTION_PRIVACY_SETTINGS)) })
        system.addView(actionButton("Storage", glass, white) { startActivity(Intent(Settings.ACTION_INTERNAL_STORAGE_SETTINGS)) })
        system.addView(actionButton("Accessibility", glass, white) { startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) })
        system.addView(actionButton("About Anonixex & Kernel Lab", glass, white) { showKernelLab() })
        settings.addView(system, LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(12) })
        scroll.addView(settings)
        page.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))
        page.addView(actionButton("Back to home", accent, bg) { buildHome() }, LinearLayout.LayoutParams(-1, dp(50)).apply { topMargin = dp(10) })
        setContentView(page)
    }

    private fun showControlCenter() {
        launcherHomeVisible = false
        val page = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(27), dp(20), dp(16))
            background = backgroundGradient()
        }
        page.addView(label("ANONIXEX  /  CONTROL", 10f, accent, true))
        page.addView(label("Control center.", 29f, white, true).apply { setPadding(0, dp(5), 0, dp(8)) })
        page.addView(TextClock(this).apply {
            format12Hour = "h:mm  ·  EEEE, d MMMM"
            format24Hour = "HH:mm  ·  EEEE, d MMMM"
            textSize = 15f
            setTextColor(muted)
        })
        val scroll = ScrollView(this)
        val controls = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        val connectivity = glassCard()
        connectivity.addView(label("CONNECTIVITY", 10f, accent, true))
        connectivity.addView(actionButton("Wi-Fi & networks", glassBright, white) { startActivity(Intent(Settings.ACTION_WIFI_SETTINGS)) })
        connectivity.addView(actionButton("Bluetooth & connected devices", glass, white) { startActivity(Intent(Settings.ACTION_BLUETOOTH_SETTINGS)) })
        connectivity.addView(actionButton("Airplane mode settings", glass, white) { startActivity(Intent(Settings.ACTION_AIRPLANE_MODE_SETTINGS)) })
        connectivity.addView(actionButton("Location", glass, white) { startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)) })
        controls.addView(connectivity)
        val device = glassCard()
        device.addView(label("DEVICE", 10f, accent, true))
        device.addView(actionButton("Brightness & display", glass, white) { startActivity(Intent(Settings.ACTION_DISPLAY_SETTINGS)) })
        device.addView(actionButton("Sound & Do Not Disturb", glass, white) { startActivity(Intent(Settings.ACTION_SOUND_SETTINGS)) })
        device.addView(actionButton("Battery & power", glass, white) { startActivity(Intent(Intent.ACTION_POWER_USAGE_SUMMARY)) })
        device.addView(actionButton("Notifications", glass, white) { startActivity(Intent("android.settings.NOTIFICATION_SETTINGS")) })
        device.addView(actionButton("Privacy & security", glass, white) { startActivity(Intent(Settings.ACTION_PRIVACY_SETTINGS)) })
        controls.addView(device, LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(12) })
        scroll.addView(controls)
        page.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f).apply { topMargin = dp(15) })
        page.addView(actionButton("Done", accent, bg) { buildHome() })
        setContentView(page)
    }

    private fun showKernelLab() {
        launcherHomeVisible = false
        val page = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(25), dp(20), dp(15))
            background = backgroundGradient()
        }
        page.addView(label("ANONIXEX  /  SYSTEM CORE", 10f, accent, true))
        page.addView(label("Kernel Lab.", 30f, white, true).apply { setPadding(0, dp(5), 0, dp(12)) })
        val info = glassCard()
        info.addView(label("RUNNING DEVICE", 10f, accent, true))
        info.addView(label("Android ${Build.VERSION.RELEASE}  ·  API ${Build.VERSION.SDK_INT}", 16f, white, true).apply { setPadding(0, dp(10), 0, dp(5)) })
        info.addView(label("Kernel: ${System.getProperty("os.version") ?: "Unavailable"}", 12f, muted, false))
        info.addView(label("CPU ABI: ${Build.SUPPORTED_ABIS.joinToString()}", 12f, muted, false).apply { setPadding(0, dp(5), 0, 0) })
        info.addView(label("Device: ${Build.MANUFACTURER} ${Build.MODEL}", 12f, muted, false).apply { setPadding(0, dp(5), 0, 0) })
        page.addView(info)
        val note = glassCard()
        note.addView(label("THE ANONIXEX KERNEL PROJECT", 10f, accent, true))
        note.addView(label("The launcher reads information about the kernel currently running on your phone. The original Anonixex kernel is a separate experimental project; it is not installed or running through this APK.", 13f, muted, false).apply { setPadding(0, dp(9), 0, dp(5)) })
        note.addView(label("A real phone kernel needs device-specific drivers, a boot chain, hardware support and a tested recovery route. This screen never flashes or modifies your phone.", 12f, muted, false))
        page.addView(note, LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(12) })
        page.addView(actionButton("Back to Studio", accent, bg) { showSettings() }, LinearLayout.LayoutParams(-1, dp(50)).apply { topMargin = dp(12) })
        setContentView(page)
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
                prefs.edit().putString("wallpaper", uri.toString()).apply()
                if (::root.isInitialized) applyWallpaper(uri)
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
    }

    private fun glassCardBackground(): GradientDrawable {
        val fill = if (glassMode) Color.argb((glassOpacity * 2.35f).toInt().coerceIn(60, 235), 31, 42, 61) else Color.rgb(24, 29, 40)
        return GradientDrawable(
            GradientDrawable.Orientation.TL_BR,
            intArrayOf(fill, Color.argb(190, 21, 27, 40))
        ).apply {
            cornerRadius = dp(this@MainActivity.cornerRadius).toFloat()
            setStroke(dp(1), Color.argb(if (glassMode) 145 else 35, 220, 236, 255))
        }
    }

    private fun backgroundGradient() = GradientDrawable(
        GradientDrawable.Orientation.TL_BR,
        intArrayOf(Color.rgb(8, 13, 25), Color.rgb(17, 19, 36), Color.rgb(7, 9, 16))
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
        setStroke(dp(1), Color.argb(55, 220, 236, 255))
    }

    private fun dp(value: Int) = (value * resources.displayMetrics.density).toInt()

    companion object {
        private const val WALLPAPER_REQUEST = 1001
    }
}
