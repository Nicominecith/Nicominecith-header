package com.markdownheaders

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.text.Spannable
import android.text.Spanned
import android.text.style.BackgroundColorSpan
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.text.style.StyleSpan
import android.text.style.TypefaceSpan
import com.aliucord.annotations.AliucordPlugin
import com.aliucord.entities.Plugin
import com.aliucord.patcher.after
import com.discord.widgets.chat.list.adapter.WidgetChatListAdapterItemMessage
import com.discord.widgets.chat.list.entries.MessageEntry
import com.facebook.drawee.span.SimpleDraweeSpanTextView

private class HeaderSizeSpan(proportion: Float) : RelativeSizeSpan(proportion)

private val HEADER_REGEX = Regex("^(#{1,3}) (\\S.*)$")
private const val FLAGS = Spanned.SPAN_EXCLUSIVE_EXCLUSIVE

@AliucordPlugin
class MarkdownHeaderNicominecith : Plugin() {

    override fun start(context: Context) {
        patcher.after<WidgetChatListAdapterItemMessage>(
            "processMessageText",
            SimpleDraweeSpanTextView::class.java,
            MessageEntry::class.java
        ) { param ->
            val textView = param.args[0] as SimpleDraweeSpanTextView
            val text = textView.text as? Spannable ?: return@after
            applyHeaders(text)
        }
    }

    override fun stop(context: Context) {
        patcher.unpatchAll()
    }

    private fun applyHeaders(text: Spannable) {
        if (text.getSpans(0, text.length, HeaderSizeSpan::class.java).isNotEmpty()) return

        val s = text.toString()
        var lineStart = 0
        while (lineStart < s.length) {
            var lineEnd = s.indexOf('\n', lineStart)
            if (lineEnd == -1) lineEnd = s.length

            val match = HEADER_REGEX.find(s.substring(lineStart, lineEnd))
            if (match != null && !isCode(text, lineStart)) {
                val level = match.groupValues[1].length
                val markerEnd = lineStart + level + 1
                val scale = when (level) {
                    1 -> 1.6f
                    2 -> 1.35f
                    else -> 1.15f
                }
                text.setSpan(HeaderSizeSpan(0.01f), lineStart, markerEnd, FLAGS)
                text.setSpan(ForegroundColorSpan(Color.TRANSPARENT), lineStart, markerEnd, FLAGS)
                text.setSpan(HeaderSizeSpan(scale), markerEnd, lineEnd, FLAGS)
                text.setSpan(StyleSpan(Typeface.BOLD), markerEnd, lineEnd, FLAGS)
            }
            lineStart = lineEnd + 1
        }
    }

    private fun isCode(text: Spanned, pos: Int): Boolean =
        text.getSpans(pos, pos + 1, TypefaceSpan::class.java).isNotEmpty() ||
            text.getSpans(pos, pos + 1, BackgroundColorSpan::class.java).isNotEmpty()
}
