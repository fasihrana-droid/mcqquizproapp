package com.example.data.model

enum class AppTab(val label: String) {
  STUDIO("Test Studio"),
  BANK("Question Bank"),
  SAVED("Saved"),
  UPLOAD("Upload"),
  STATS("Analytics"),
}

enum class Difficulty(val label: String) {
  EASY("Easy"),
  MEDIUM("Medium"),
  HARD("Hard");

  companion object {
    fun fromString(value: String?): Difficulty =
      when (value?.trim()?.lowercase()) {
        "easy" -> EASY
        "hard" -> HARD
        else -> MEDIUM
      }
  }
}

enum class DifficultyFilter(val label: String) {
  ALL("All"),
  EASY("Easy"),
  MEDIUM("Medium"),
  HARD("Hard");

  fun matches(difficulty: Difficulty): Boolean =
    when (this) {
      ALL -> true
      EASY -> difficulty == Difficulty.EASY
      MEDIUM -> difficulty == Difficulty.MEDIUM
      HARD -> difficulty == Difficulty.HARD
    }
}

enum class ExamMode(val title: String, val subtitle: String) {
  STRICT(
    "Strict Exam Simulator",
    "Timed test, negative marking, answers and solutions revealed ONLY upon final submission.",
  ),
  STUDY(
    "Study & Practice Drill",
    "Instant right/wrong feedback and rationale after each click.",
  ),
}

enum class NegativeMarking(val penalty: Double, val label: String) {
  NONE(0.0, "None (0.00)"),
  STANDARD(0.25, "Standard 0.25 (PPSC/FPSC)"),
  ONE_THIRD(0.33, "One Third 0.33"),
  HALF(0.50, "Half 0.50");

  companion object {
    fun fromPenalty(value: Double): NegativeMarking =
      entries.minByOrNull { kotlin.math.abs(it.penalty - value) } ?: NONE
  }
}

data class QuizQuestion(
  val id: String,
  val subject: String,
  val difficulty: Difficulty,
  val question: String,
  val options: List<String>,
  val correctIndex: Int,
  val explanation: String,
)

data class SubjectSelection(
  val enabled: Boolean = true,
  val rangeOrCount: String = "",
)

data class StudioConfig(
  val subjectSelections: Map<String, SubjectSelection> = emptyMap(),
  val title: String = "",
  val mode: ExamMode = ExamMode.STUDY,
  val difficultyFilter: DifficultyFilter = DifficultyFilter.ALL,
  val negativeMarking: NegativeMarking = NegativeMarking.NONE,
  val passPercentage: Int = 33,
  val zeroRepeats: Boolean = false,
  val shuffleQuestions: Boolean = false,
  val randomizeOptions: Boolean = true,
  val questionCount: Int = 10,
  val timeMinutes: Int = 20,
)

data class LaunchConfig(
  val title: String,
  val mode: ExamMode,
  val negativeMarking: Double,
  val passPercentage: Int,
  val timeMinutes: Int,
  val randomizeOptions: Boolean,
)

data class QuizOptionItem(
  val text: String,
  val isCorrect: Boolean,
)

data class ActiveQuizQuestion(
  val questionId: String,
  val options: List<QuizOptionItem>,
)

data class ActiveQuizState(
  val questions: List<ActiveQuizQuestion>,
  val answers: List<Int?>,
  val currentIndex: Int,
  val config: LaunchConfig,
  val endTimeMillis: Long,
  val remainingSeconds: Int,
)

data class QuizResultState(
  val questions: List<ActiveQuizQuestion>,
  val answers: List<Int?>,
  val config: LaunchConfig,
  val correctCount: Int,
  val wrongCount: Int,
  val skippedCount: Int,
  val score: Double,
  val percentage: Double,
  val passed: Boolean,
  val wrongIds: List<String>,
  val skippedIds: List<String>,
)

data class AttemptRecord(
  val id: String,
  val title: String,
  val mode: ExamMode,
  val dateLabel: String,
  val percentage: Double,
  val passed: Boolean,
  val questionIds: List<String>,
  val negativeMarking: Double,
  val passPercentage: Int,
  val timeMinutes: Int,
  val randomizeOptions: Boolean,
)
