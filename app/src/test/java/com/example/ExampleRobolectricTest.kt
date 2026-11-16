package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.McqParser
import com.example.data.SampleData
import com.example.data.model.Difficulty
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read app_name string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("MCQQUIZ", appName)
  }

  @Test
  fun `parse sample json template and export roundtrip`() {
    val parsed = McqParser.parseJson(SampleData.SAMPLE_JSON_TEMPLATE).getOrThrow()
    assertEquals(1, parsed.size)
    assertEquals("Pakistan Studies", parsed[0].subject)
    assertEquals(Difficulty.MEDIUM, parsed[0].difficulty)
    assertEquals(1, parsed[0].correctIndex)

    val exported = McqParser.exportToJson(parsed)
    val reParsed = McqParser.parseJson(exported).getOrThrow()
    assertEquals(parsed[0].question, reParsed[0].question)
    assertEquals(parsed[0].options, reParsed[0].options)
  }

  @Test
  fun `parse quoted csv rows accurately`() {
    val csv = """
      subject,difficulty,question,option_a,option_b,option_c,option_d,correct_index,explanation
      "Pak Studies",Hard,"The Khyber Pass, a historic route, connects Pakistan with?",Iran,China,Afghanistan,India,2,"Links KP to Afghanistan."
    """.trimIndent()
    val parsed = McqParser.parseCsv(csv).getOrThrow()
    assertEquals(1, parsed.size)
    assertTrue(parsed[0].question.contains("Khyber Pass, a historic route"))
    assertEquals(Difficulty.HARD, parsed[0].difficulty)
    assertEquals(2, parsed[0].correctIndex)
  }
}
