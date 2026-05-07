package com.ichat.login

import com.ichat.login.data.PhoneUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PhoneUtilsTest {

    @Test fun digitsOnly_removesNonDigits() {
        assertEquals("13800138000", PhoneUtils.digitsOnly("138 0013 8000"))
        assertEquals("13800138000", PhoneUtils.digitsOnly("138-0013-8000"))
        assertEquals("",            PhoneUtils.digitsOnly("abc"))
    }

    @Test fun format_appliesGroupedSpacing() {
        assertEquals("",              PhoneUtils.format(""))
        assertEquals("1",             PhoneUtils.format("1"))
        assertEquals("138",           PhoneUtils.format("138"))
        assertEquals("138 0",         PhoneUtils.format("1380"))
        assertEquals("138 0013",      PhoneUtils.format("1380013"))
        assertEquals("138 0013 8",    PhoneUtils.format("13800138"))
        assertEquals("138 0013 8000", PhoneUtils.format("13800138000"))
    }

    @Test fun format_truncatesAt11Digits() {
        assertEquals("138 0013 8000", PhoneUtils.format("13800138000999"))
    }

    @Test fun isValid_acceptsValidChineseMobile() {
        assertTrue(PhoneUtils.isValid("13800138000"))
        assertTrue(PhoneUtils.isValid("15912345678"))
        assertTrue(PhoneUtils.isValid("18800001111"))
    }

    @Test fun isValid_rejectsInvalid() {
        assertFalse(PhoneUtils.isValid(""))
        assertFalse(PhoneUtils.isValid("1380013800"))     // < 11
        assertFalse(PhoneUtils.isValid("138001380001"))   // > 11
        assertFalse(PhoneUtils.isValid("00000000000"))    // 不以 1 开头
        assertFalse(PhoneUtils.isValid("12345678901"))    // 第二位不在 3..9
        assertFalse(PhoneUtils.isValid("1a800138000"))    // 含字母
    }

    @Test fun mask_hidesMiddleFour() {
        assertEquals("138****8000", PhoneUtils.mask("13800138000"))
    }

    @Test fun mask_returnsAsIs_whenNot11Digits() {
        assertEquals("",         PhoneUtils.mask(""))
        assertEquals("12345",    PhoneUtils.mask("12345"))
    }
}
