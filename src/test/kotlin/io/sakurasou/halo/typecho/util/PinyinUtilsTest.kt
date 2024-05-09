package io.sakurasou.halo.typecho.util

import net.sourceforge.pinyin4j.PinyinHelper
import org.junit.jupiter.api.Test

/**
 * @author mashirot
 * 2024/5/9 18:51
 */
class PinyinUtilsTest {
    @Test
    fun testTrans2Pinyin() {
        val str = "博客 - ！？ a 11 2== 博1"
        println("=====")
        println(trans2Pinyin(str))
        println(trans2Pinyin2(str))
        println("=====")
    }

    private fun trans2Pinyin(input: String): String {
        val result = mutableListOf<String>()
        val sb = StringBuilder()
        for (ch in input) {
            if (ch.isLowerCase() || ch.isUpperCase()) {
                if (sb.isNotBlank() && !sb.last().isLetter()) {
                    result.add(sb.toString())
                    sb.clear()
                }
                sb.append(ch.lowercaseChar())
            } else if (ch.isDigit()) {
                if (sb.isNotBlank() && !sb.last().isDigit()) {
                    result.add(sb.toString())
                    sb.clear()
                }
                sb.append(ch)
            } else if (ch.code in '\u4E00'.code..'\u9FA5'.code) {
                if (sb.isNotBlank()) {
                    result.add(sb.toString())
                    sb.clear()
                }
                result.add(PinyinHelper.toHanyuPinyinStringArray(ch)[0].dropLast(1))
            }
        }
        if (sb.isNotBlank()) result.add(sb.toString())
        return result.joinToString("-")
    }

    // optimised by gpt4
    private fun trans2Pinyin2(input: String): String {
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