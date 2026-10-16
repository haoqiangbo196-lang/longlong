package com.elegant.reader.reader

import android.graphics.Typeface

/**
 * 字体加载器：参考 Legado 的 getTypeface / loadTypeface。
 *
 * 支持从字体文件路径加载（Typeface.createFromFile），
 * 以及系统内置字体（DEFAULT / SERIF / SANS_SERIF / MONOSPACE）。
 */
object FontLoader {

    /**
     * 根据设置加载正文字体。
     *
     * 优先级：customFontPath（自定义字体文件）> fontFamily（系统内置）。
     * 加载失败时回退到系统默认字体。
     */
    fun load(settings: ReaderSettings): Typeface {
        // 自定义字体文件优先
        if (settings.customFontPath.isNotEmpty()) {
            val loaded = runCatching {
                Typeface.createFromFile(settings.customFontPath)
            }.getOrNull()
            if (loaded != null) return loaded
        }
        return when (settings.fontFamily) {
            FontFamilyOption.DEFAULT -> Typeface.DEFAULT
            FontFamilyOption.SERIF -> Typeface.SERIF
            FontFamilyOption.SANS -> Typeface.SANS_SERIF
            FontFamilyOption.MONO -> Typeface.MONOSPACE
        }
    }
}
