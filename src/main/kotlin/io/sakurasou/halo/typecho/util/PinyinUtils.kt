package io.sakurasou.halo.typecho.util

import net.sourceforge.pinyin4j.PinyinHelper

/**
 * @author mashirot
 * 2024/5/9 18:35
 */
object PinyinUtils {
    fun trans2Pinyin(input: String): String {
        val sb = StringBuilder()
        var lastCharType: CharType? = null

        for (ch in input) {
            when {
                ch.isLowerCase() || ch.isUpperCase() -> {
                    if (lastCharType != null && lastCharType != CharType.LETTER) sb.append('-')
                    sb.append(ch.lowercaseChar())
                    lastCharType = CharType.LETTER
                }

                ch.isDigit() -> {
                    if (lastCharType != null && lastCharType != CharType.DIGIT) sb.append('-')
                    sb.append(ch)
                    lastCharType = CharType.DIGIT
                }

                ch.code in '\u4E00'.code..'\u9FA5'.code -> {
                    if (lastCharType != null) sb.append('-')
                    sb.append(PinyinHelper.toHanyuPinyinStringArray(ch)[0].dropLast(1))
                    lastCharType = CharType.HAN_ZI
                }
            }
        }

        return sb.toString()
    }

    private enum class CharType {
        LETTER, DIGIT, HAN_ZI
    }
}