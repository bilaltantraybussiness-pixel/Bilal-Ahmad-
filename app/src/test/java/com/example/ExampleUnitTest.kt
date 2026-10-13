package com.example

import com.example.data.repository.AppRepository
import com.example.util.PhoneAuthHelper
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun passwordHashing_isDeterministicAndSecure() {
    val hash1 = AppRepository.hashPassword("secret123")
    val hash2 = AppRepository.hashPassword("secret123")
    val hashDifferent = AppRepository.hashPassword("different")

    assertEquals(hash1, hash2)
    assertNotEquals(hash1, hashDifferent)
    assertEquals(64, hash1.length) // SHA-256 hex length
  }

  @Test
  fun phoneFormatting_normalizesIndianKashmirNumbers() {
    assertEquals("+919419011111", PhoneAuthHelper.formatPhoneNumber("9419011111"))
    assertEquals("+919419011111", PhoneAuthHelper.formatPhoneNumber("09419011111"))
    assertEquals("+919419011111", PhoneAuthHelper.formatPhoneNumber("+91 94190 11111"))
    assertEquals("9419011111", PhoneAuthHelper.extract10DigitPhone("+919419011111"))
    assertEquals("9419011111", PhoneAuthHelper.extract10DigitPhone("09419011111"))
  }

  @Test
  fun spokenDigitsParsing_supportsEnglishUrduKashmiriWords() {
    // English spoken numbers
    val englishSpoken = "nine four one nine zero one one one one one"
    assertEquals("9419011111", PhoneAuthHelper.parseSpokenDigits(englishSpoken))

    // Urdu / Hindi spoken numbers
    val urduSpoken = "nau char ek nau sifar ek do teen"
    assertEquals("94190123", PhoneAuthHelper.parseSpokenDigits(urduSpoken))

    // Kashmiri numeral words
    val kashmiriSpoken = "akh zı treh tsor paantsh sheh"
    assertEquals("123456", PhoneAuthHelper.parseSpokenDigits(kashmiriSpoken))

    // 6-digit OTP verification spoken code
    val otpSpoken = "one two three four five six"
    assertEquals("123456", PhoneAuthHelper.parseSpokenDigits(otpSpoken))
  }
}
