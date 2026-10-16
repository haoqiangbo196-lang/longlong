package com.elegant.reader.parse

import java.io.File
import java.util.zip.ZipFile
import java.util.zip.ZipEntry

/**
 * EPUB 解析器：解压 ZIP、解析 container.xml / OPF / 章节 XHTML。
 */
object EpubParser {

    fun parse(file: File): ParsedBook {
        ZipFile(file).use { zip ->
            // 1. 定位 OPF 路径
            val opfPath = locateOpfPath(zip) ?: throw IllegalArgumentException("EPUB 结构无效：缺少 OPF 文件")

            // 2. 解析 OPF 元数据
            val opfContent = readEntry(zip, opfPath)
                ?: throw IllegalArgumentException("EPUB 结构无效：无法读取 OPF 文件")
            val opfDir = opfPath.substringBeforeLast('/', "")

            val metadata = parseOpfMetadata(opfContent)
            val manifest = parseManifest(opfContent)
            val spine = parseSpine(opfContent)

            // 3. 提取章节
            val chapters = mutableListOf<Chapter>()
            for (idref in spine) {
                val href = manifest[idref] ?: continue
                val entryPath = resolvePath(opfDir, href)
                val html = readEntry(zip, entryPath) ?: continue
                val title = extractTitle(html) ?: "第 ${chapters.size + 1} 章"
                val content = htmlToText(html)
                if (content.isNotBlank()) {
                    chapters.add(Chapter(title, content))
                }
            }

            if (chapters.isEmpty()) {
                // 兜底：从 manifest 里取所有 xhtml 文件
                for ((_, href) in manifest) {
                    val entryPath = resolvePath(opfDir, href)
                    val html = readEntry(zip, entryPath) ?: continue
                    val content = htmlToText(html)
                    if (content.isNotBlank()) {
                        val title = extractTitle(html) ?: "章节 ${chapters.size + 1}"
                        chapters.add(Chapter(title, content))
                    }
                }
            }

            val coverPath = extractCover(zip, opfDir, manifest)

            return ParsedBook(
                title = metadata.first.ifBlank { file.nameWithoutExtension },
                author = metadata.second,
                chapters = chapters,
                coverPath = coverPath
            )
        }
    }

    private fun locateOpfPath(zip: ZipFile): String? {
        val container = readEntry(zip, "META-INF/container.xml") ?: return null
        // 提取 rootfile full-path
        val regex = Regex("full-path\\s*=\\s*[\"']([^\"']+)[\"']")
        return regex.find(container)?.groupValues?.get(1)
    }

    private fun readEntry(zip: ZipFile, path: String): String? {
        val entry: ZipEntry = zip.getEntry(path) ?: return null
        if (entry.isDirectory) return null
        val bytes = zip.getInputStream(entry).use { it.readBytes() }
        return String(bytes, Charsets.UTF_8)
    }

    private fun parseOpfMetadata(opf: String): Pair<String, String> {
        val title = extractTag(opf, "dc:title") ?: ""
        val author = extractTag(opf, "dc:creator") ?: ""
        return title to author
    }

    private fun parseManifest(opf: String): Map<String, String> {
        val result = mutableMapOf<String, String>()
        val regex = Regex(
            "<item[^>]*id\\s*=\\s*[\"']([^\"']+)[\"'][^>]*href\\s*=\\s*[\"']([^\"']+)[\"']" +
                    "|<item[^>]*href\\s*=\\s*[\"']([^\"']+)[\"'][^>]*id\\s*=\\s*[\"']([^\"']+)[\"']"
        )
        for (m in regex.findAll(opf)) {
            val id = m.groupValues[1].ifBlank { m.groupValues[4] }
            val href = m.groupValues[2].ifBlank { m.groupValues[3] }
            if (id.isNotBlank() && href.isNotBlank()) result[id] = href
        }
        return result
    }

    private fun parseSpine(opf: String): List<String> {
        val result = mutableListOf<String>()
        val regex = Regex("<itemref[^>]*idref\\s*=\\s*[\"']([^\"']+)[\"']")
        for (m in regex.findAll(opf)) {
            result.add(m.groupValues[1])
        }
        return result
    }

    private fun extractTag(html: String, tag: String): String? {
        val regex = Regex("<$tag[^>]*>(.*?)</$tag>", setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL))
        val match = regex.find(html) ?: return null
        val group = match.groupValues.getOrNull(1) ?: return null
        return group.trim().replace(Regex("<[^>]+>"), "").trim()
    }

    private fun extractTitle(html: String): String? {
        // 优先 h1/h2/h3/title
        for (tag in listOf("h1", "h2", "h3", "title")) {
            extractTag(html, tag)?.takeIf { it.isNotBlank() }?.let { return it }
        }
        return null
    }

    /**
     * 将 XHTML 转为纯文本，保留段落结构。
     */
    private fun htmlToText(html: String): String {
        var text = html
        // 处理段落标签为换行
        text = text.replace(Regex("(?i)</p\\s*>"), "\n\n")
        text = text.replace(Regex("(?i)<br\\s*/?>"), "\n")
        text = text.replace(Regex("(?i)</(div|h[1-6]|li|tr)\\s*>"), "\n")
        // 去除 script/style
        text = text.replace(Regex("(?is)<(script|style)[^>]*>.*?</\\1>"), "")
        // 去除所有标签
        text = text.replace(Regex("<[^>]+>"), "")
        // 解码常见实体
        text = decodeEntities(text)
        // 压缩多余空行
        text = text.replace(Regex("\n{3,}"), "\n\n")
        return text.trim()
    }

    private fun decodeEntities(text: String): String {
        val map = mapOf(
            "&nbsp;" to " ",
            "&amp;" to "&",
            "&lt;" to "<",
            "&gt;" to ">",
            "&quot;" to "\"",
            "&apos;" to "'",
            "&#160;" to " ",
            "&ldquo;" to "\u201C",
            "&rdquo;" to "\u201D",
            "&lsquo;" to "\u2018",
            "&rsquo;" to "\u2019",
            "&mdash;" to "\u2014",
            "&hellip;" to "\u2026"
        )
        var result = text
        for ((k, v) in map) result = result.replace(k, v)
        return result
    }

    private fun resolvePath(dir: String, href: String): String {
        if (href.startsWith("/")) return href.substring(1)
        if (dir.isEmpty()) return href
        return "$dir/$href"
    }

    private fun extractCover(zip: ZipFile, opfDir: String, manifest: Map<String, String>): String? {
        // 简单策略：查找 cover 图片
        for ((_, href) in manifest) {
            val lower = href.lowercase()
            if (lower.endsWith(".jpg") || lower.endsWith(".jpeg") || lower.endsWith(".png")) {
                if (lower.contains("cover") || lower.contains("封面")) {
                    val entry = zip.getEntry(resolvePath(opfDir, href)) ?: continue
                    if (entry.size < 5 * 1024 * 1024) {
                        return href // 返回相对路径，由上层处理
                    }
                }
            }
        }
        return null
    }
}
