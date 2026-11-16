package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.AttemptRecord
import com.example.data.model.Difficulty
import com.example.data.model.ExamMode
import com.example.data.model.QuizQuestion
import org.json.JSONArray

@Entity(tableName = "questions")
data class QuestionEntity(
  @PrimaryKey val id: String,
  val subject: String,
  val difficulty: String,
  val question: String,
  val optionA: String,
  val optionB: String,
  val optionC: String,
  val optionD: String,
  val correctIndex: Int,
  val explanation: String,
  val createdAt: Long = System.currentTimeMillis(),
) {
  fun toDomain(): QuizQuestion =
    QuizQuestion(
      id = id,
      subject = subject,
      difficulty = Difficulty.fromString(difficulty),
      question = question,
      options = listOf(optionA, optionB, optionC, optionD),
      correctIndex = correctIndex.coerceIn(0, 3),
      explanation = explanation,
    )

  companion object {
    fun fromDomain(q: QuizQuestion, createdAt: Long = System.currentTimeMillis()): QuestionEntity {
      val opts = q.options.take(4).let { list ->
        if (list.size < 4) list + List(4 - list.size) { "" } else list
      }
      return QuestionEntity(
        id = q.id,
        subject = q.subject,
        difficulty = q.difficulty.label,
        question = q.question,
        optionA = opts[0],
        optionB = opts[1],
        optionC = opts[2],
        optionD = opts[3],
        correctIndex = q.correctIndex.coerceIn(0, 3),
        explanation = q.explanation,
        createdAt = createdAt,
      )
    }
  }
}

@Entity(tableName = "saved_questions")
data class SavedQuestionEntity(
  @PrimaryKey val questionId: String,
  val savedAt: Long = System.currentTimeMillis(),
)

@Entity(tableName = "attempt_history")
data class AttemptHistoryEntity(
  @PrimaryKey val id: String,
  val title: String,
  val mode: String,
  val dateLabel: String,
  val percentage: Double,
  val passed: Boolean,
  val questionIdsJson: String,
  val negativeMarking: Double,
  val passPercentage: Int,
  val timeMinutes: Int,
  val randomizeOptions: Boolean,
  val createdAt: Long = System.currentTimeMillis(),
) {
  fun toDomain(): AttemptRecord {
    val ids = buildList {
      val arr = runCatching { JSONArray(questionIdsJson) }.getOrNull()
      if (arr != null) {
        for (i in 0 until arr.length()) {
          add(arr.optString(i))
        }
      }
    }
    return AttemptRecord(
      id = id,
      title = title,
      mode = if (mode == ExamMode.STUDY.name) ExamMode.STUDY else ExamMode.STRICT,
      dateLabel = dateLabel,
      percentage = percentage,
      passed = passed,
      questionIds = ids,
      negativeMarking = negativeMarking,
      passPercentage = passPercentage,
      timeMinutes = timeMinutes,
      randomizeOptions = randomizeOptions,
    )
  }

  companion object {
    fun fromDomain(record: AttemptRecord): AttemptHistoryEntity {
      val json = JSONArray().apply { record.questionIds.forEach { put(it) } }.toString()
      return AttemptHistoryEntity(
        id = record.id,
        title = record.title,
        mode = record.mode.name,
        dateLabel = record.dateLabel,
        percentage = record.percentage,
        passed = record.passed,
        questionIdsJson = json,
        negativeMarking = record.negativeMarking,
        passPercentage = record.passPercentage,
        timeMinutes = record.timeMinutes,
        randomizeOptions = record.randomizeOptions,
      )
    }
  }
}
