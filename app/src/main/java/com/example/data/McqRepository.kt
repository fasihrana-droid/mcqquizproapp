package com.example.data

import android.content.Context
import com.example.data.local.AttemptHistoryEntity
import com.example.data.local.McqDao
import com.example.data.local.QuestionEntity
import com.example.data.local.SavedQuestionEntity
import com.example.data.model.AttemptRecord
import com.example.data.model.QuizQuestion
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class McqRepository(
  private val dao: McqDao,
  context: Context,
) {
  private val prefs = context.applicationContext.getSharedPreferences("mcqquiz_prefs", Context.MODE_PRIVATE)

  val questions: Flow<List<QuizQuestion>> =
    dao.observeAllQuestions().map { entities -> entities.map { it.toDomain() } }

  val savedIds: Flow<List<String>> = dao.observeSavedIds()

  val history: Flow<List<AttemptRecord>> =
    dao.observeHistory().map { entities -> entities.map { it.toDomain() } }

  suspend fun ensureSeededOnFirstLaunch() {
    val alreadySeeded = prefs.getBoolean("initial_bank_seeded", false)
    if (!alreadySeeded && dao.countQuestions() == 0) {
      val baseTime = System.currentTimeMillis()
      val entities = SampleData.pakStudiesPack().mapIndexed { index, q ->
        QuestionEntity.fromDomain(q, createdAt = baseTime + index)
      }
      dao.insertQuestions(entities)
    }
    if (!alreadySeeded) {
      prefs.edit().putBoolean("initial_bank_seeded", true).apply()
    }
  }

  suspend fun addQuestions(list: List<QuizQuestion>) {
    val baseTime = System.currentTimeMillis()
    val entities = list.mapIndexed { index, q ->
      QuestionEntity.fromDomain(q, createdAt = baseTime + index)
    }
    dao.insertQuestions(entities)
  }

  suspend fun addQuestion(question: QuizQuestion) {
    dao.insertQuestion(QuestionEntity.fromDomain(question))
  }

  suspend fun updateQuestion(question: QuizQuestion) {
    dao.updateQuestion(QuestionEntity.fromDomain(question))
  }

  suspend fun deleteQuestion(id: String) {
    dao.deleteQuestionById(id)
    dao.deleteSavedById(id)
  }

  suspend fun deleteSubject(subject: String) {
    dao.deleteSavedBySubject(subject)
    dao.deleteQuestionsBySubject(subject)
  }

  suspend fun toggleSaved(questionId: String, currentlySaved: Boolean) {
    if (currentlySaved) {
      dao.deleteSavedById(questionId)
    } else {
      dao.insertSaved(SavedQuestionEntity(questionId = questionId))
    }
  }

  suspend fun clearAllSaved() {
    dao.clearAllSaved()
  }

  suspend fun recordAttempt(record: AttemptRecord) {
    dao.insertHistory(AttemptHistoryEntity.fromDomain(record))
  }

  suspend fun clearAllHistory() {
    dao.clearAllHistory()
  }
}
