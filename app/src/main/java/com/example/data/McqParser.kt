package com.example.data

import com.example.data.model.Difficulty
import com.example.data.model.QuizQuestion
import org.json.JSONArray
import org.json.JSONObject

object McqParser {

  fun parseJson(raw: String): Result<List<QuizQuestion>> = runCatching {
    val trimmed = raw.trim()
    val array = JSONArray(trimmed)
    val out = mutableListOf<QuizQuestion>()
    for (i in 0 until array.length()) {
      val obj = array.optJSONObject(i) ?: continue
      val questionText = obj.optString("question", "").trim()
      val optionsJson = obj.optJSONArray("options") ?: continue
      if (questionText.isEmpty() || optionsJson.length() < 2) continue

      val rawOpts = mutableListOf<String>()
      for (j in 0 until minOf(4, optionsJson.length())) {
        rawOpts.add(optionsJson.optString(j, ""))
      }
      while (rawOpts.size < 4) {
        rawOpts.add("")
      }

      val subject = obj.optString("subject", "").trim().ifEmpty { "General" }
      val difficulty = Difficulty.fromString(obj.optString("difficulty", "Medium"))
      val correctIndex = obj.optInt("correct_index", 0).coerceIn(0, 3)
      val explanation = obj.optString("explanation", "")

      out.add(
        QuizQuestion(
          id = SampleData.newId(),
          subject = subject,
          difficulty = difficulty,
          question = questionText,
          options = rawOpts,
          correctIndex = correctIndex,
          explanation = explanation,
        )
      )
    }
    out
  }

  fun parseCsv(raw: String): Result<List<QuizQuestion>> = runCatching {
    val rows = parseCsvRows(raw)
    if (rows.isEmpty()) return@runCatching emptyList()
    val headers = rows.first().map { it.trim().lowercase() }
    val dataRows = rows.drop(1)

    fun col(row: List<String>, name: String): String {
      val idx = headers.indexOf(name)
      return if (idx in row.indices) row[idx].trim() else ""
    }

    dataRows.mapNotNull { row ->
      val qText = col(row, "question")
      val optA = col(row, "option_a")
      val optB = col(row, "option_b")
      val optC = col(row, "option_c")
      val optD = col(row, "option_d")
      val options = listOf(optA, optB, optC, optD)
      if (qText.isEmpty() || options.count { it.isNotEmpty() } < 2) return@mapNotNull null

      val subject = col(row, "subject").ifEmpty { "General" }
      val difficulty = Difficulty.fromString(col(row, "difficulty"))
      val correctIndex = (col(row, "correct_index").toIntOrNull() ?: 0).coerceIn(0, 3)
      val explanation = col(row, "explanation")

      QuizQuestion(
        id = SampleData.newId(),
        subject = subject,
        difficulty = difficulty,
        question = qText,
        options = options,
        correctIndex = correctIndex,
        explanation = explanation,
      )
    }
  }

  private fun parseCsvRows(text: String): List<List<String>> {
    val rows = mutableListOf<List<String>>()
    var currentRow = mutableListOf<String>()
    val cell = StringBuilder()
    var inQuotes = false
    var i = 0
    while (i < text.length) {
      val ch = text[i]
      if (inQuotes) {
        if (ch == '"') {
          if (i + 1 < text.length && text[i + 1] == '"') {
            cell.append('"')
            i++
          } else {
            inQuotes = false
          }
        } else {
          cell.append(ch)
        }
      } else {
        when (ch) {
          '"' -> inQuotes = true
          ',' -> {
            currentRow.add(cell.toString())
            cell.clear()
          }
          '\n', '\r' -> {
            if (ch == '\r' && i + 1 < text.length && text[i + 1] == '\n') {
              i++
            }
            currentRow.add(cell.toString())
            cell.clear()
            if (currentRow.any { it.isNotBlank() }) {
              rows.add(currentRow)
            }
            currentRow = mutableListOf()
          }
          else -> cell.append(ch)
        }
      }
      i++
    }
    if (cell.isNotEmpty() || currentRow.isNotEmpty()) {
      currentRow.add(cell.toString())
      if (currentRow.any { it.isNotBlank() }) {
        rows.add(currentRow)
      }
    }
    return rows
  }

  fun exportToJson(questions: List<QuizQuestion>): String {
    val array = JSONArray()
    questions.forEach { q ->
      val obj = JSONObject()
      obj.put("subject", q.subject)
      obj.put("difficulty", q.difficulty.label)
      obj.put("question", q.question)
      obj.put("options", JSONArray(q.options))
      obj.put("correct_index", q.correctIndex)
      obj.put("explanation", q.explanation)
      array.put(obj)
    }
    return array.toString(2)
  }
}
