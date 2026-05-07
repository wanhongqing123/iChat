package com.ichat.login.data

object PhoneUtils {

    fun digitsOnly(input: String): String = input.filter { it.isDigit() }

    fun format(raw: String): String {
        val digits = digitsOnly(raw).take(11)
        return when {
            digits.length <= 3 -> digits
            digits.length <= 7 -> "${digits.substring(0,3)} ${digits.substring(3)}"
            else               -> "${digits.substring(0,3)} ${digits.substring(3,7)} ${digits.substring(7)}"
        }
    }

    fun isValid(phone: String): Boolean {
        val d = digitsOnly(phone)
        return d.length == 11 && d[0] == '1' && d[1] in '3'..'9'
    }

    fun mask(phone: String): String {
        val d = digitsOnly(phone)
        return if (d.length == 11) "${d.substring(0,3)}****${d.substring(7)}" else d
    }
}
