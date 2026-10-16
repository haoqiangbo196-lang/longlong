package com.elegant.reader.reader

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "reader_settings")

/**
 * 阅读设置的持久化存储（DataStore）。
 */
class ReaderSettingsStore(private val context: Context) {

    private object Keys {
        val fontSize = floatPreferencesKey("font_size")
        val lineHeight = floatPreferencesKey("line_height")
        val paragraphSpacing = floatPreferencesKey("paragraph_spacing")
        val marginH = floatPreferencesKey("margin_h")
        val marginTop = floatPreferencesKey("margin_top")
        val marginBottom = floatPreferencesKey("margin_bottom")
        val fontFamily = stringPreferencesKey("font_family")
        val theme = stringPreferencesKey("theme")
        val pageTurnMode = stringPreferencesKey("page_turn_mode")
    }

    val settingsFlow: Flow<ReaderSettings> = context.dataStore.data.map { prefs ->
        ReaderSettings(
            fontSizeSp = prefs[Keys.fontSize] ?: 18f,
            lineHeightMultiplier = prefs[Keys.lineHeight] ?: 1.6f,
            paragraphSpacing = prefs[Keys.paragraphSpacing] ?: 8f,
            pageMarginHorizontalDp = prefs[Keys.marginH] ?: 20f,
            pageMarginTopDp = prefs[Keys.marginTop] ?: 16f,
            pageMarginBottomDp = prefs[Keys.marginBottom] ?: 16f,
            fontFamily = runCatching {
                FontFamilyOption.valueOf(prefs[Keys.fontFamily] ?: "DEFAULT")
            }.getOrDefault(FontFamilyOption.DEFAULT),
            theme = runCatching {
                ReaderTheme.valueOf(prefs[Keys.theme] ?: "PAPER")
            }.getOrDefault(ReaderTheme.PAPER),
            pageTurnMode = runCatching {
                PageTurnMode.valueOf(prefs[Keys.pageTurnMode] ?: "SIMULATION")
            }.getOrDefault(PageTurnMode.SIMULATION)
        )
    }

    suspend fun save(settings: ReaderSettings) {
        context.dataStore.edit { prefs ->
            prefs[Keys.fontSize] = settings.fontSizeSp
            prefs[Keys.lineHeight] = settings.lineHeightMultiplier
            prefs[Keys.paragraphSpacing] = settings.paragraphSpacing
            prefs[Keys.marginH] = settings.pageMarginHorizontalDp
            prefs[Keys.marginTop] = settings.pageMarginTopDp
            prefs[Keys.marginBottom] = settings.pageMarginBottomDp
            prefs[Keys.fontFamily] = settings.fontFamily.name
            prefs[Keys.theme] = settings.theme.name
            prefs[Keys.pageTurnMode] = settings.pageTurnMode.name
        }
    }
}
