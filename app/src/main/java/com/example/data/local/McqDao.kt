package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface McqDao {
  @Query("SELECT * FROM questions ORDER BY createdAt ASC")
  fun observeAllQuestions(): Flow<List<QuestionEntity>>

  @Query("SELECT COUNT(*) FROM questions")
  suspend fun countQuestions(): Int

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertQuestions(questions: List<QuestionEntity>)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertQuestion(question: QuestionEntity)

  @Update
  suspend fun updateQuestion(question: QuestionEntity)

  @Query("DELETE FROM questions WHERE id = :id")
  suspend fun deleteQuestionById(id: String)

  @Query("DELETE FROM questions WHERE subject = :subject")
  suspend fun deleteQuestionsBySubject(subject: String)

  @Query("SELECT questionId FROM saved_questions ORDER BY savedAt ASC")
  fun observeSavedIds(): Flow<List<String>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertSaved(saved: SavedQuestionEntity)

  @Query("DELETE FROM saved_questions WHERE questionId = :questionId")
  suspend fun deleteSavedById(questionId: String)

  @Query("DELETE FROM saved_questions WHERE questionId IN (SELECT id FROM questions WHERE subject = :subject)")
  suspend fun deleteSavedBySubject(subject: String)

  @Query("DELETE FROM saved_questions")
  suspend fun clearAllSaved()

  @Query("SELECT * FROM attempt_history ORDER BY createdAt DESC LIMIT 100")
  fun observeHistory(): Flow<List<AttemptHistoryEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertHistory(attempt: AttemptHistoryEntity)

  @Query("DELETE FROM attempt_history")
  suspend fun clearAllHistory()
}
