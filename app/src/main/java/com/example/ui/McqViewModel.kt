package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.McqParser
import com.example.data.McqRepository
import com.example.data.SampleData
import com.example.data.local.AppDatabase
import com.example.data.model.ActiveQuizQuestion
import com.example.data.model.ActiveQuizState
import com.example.data.model.AppTab
import com.example.data.model.AttemptRecord
import com.example.data.model.Difficulty
import com.example.data.model.DifficultyFilter
import com.example.data.model.ExamMode
import com.example.data.model.LaunchConfig
import com.example.data.model.NegativeMarking
import com.example.data.model.QuizOptionItem
import com.example.data.model.QuizQuestion
import com.example.data.model.QuizResultState
import com.example.data.model.StudioConfig
import com.example.data.model.SubjectSelection
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class BankFilterState(
  val searchQuery: String = "",
  val subjectFilter: String = "All",
  val difficultyFilter: DifficultyFilter = DifficultyFilter.ALL,
)

data class McqUiState(
  val bank: List<QuizQuestion> = emptyList(),
  val subjects: List<String> = emptyList(),
  val savedIds: Set<String> = emptySet(),
  val history: List<AttemptRecord> = emptyList(),
  val currentTab: AppTab = AppTab.STUDIO,
  val studio: StudioConfig = StudioConfig(),
  val poolQuestions: List<QuizQuestion> = emptyList(),
  val activeQuiz: ActiveQuizState? = null,
  val quizResult: QuizResultState? = null,
  val bankFilter: BankFilterState = BankFilterState(),
)

class McqViewModel(application: Application) : AndroidViewModel(application) {
  private val repository = McqRepository(
    dao = AppDatabase.getInstance(application).mcqDao(),
    context = application,
  )

  private val currentTab = MutableStateFlow(AppTab.STUDIO)
  private val studioConfig = MutableStateFlow(StudioConfig())
  private val activeQuiz = MutableStateFlow<ActiveQuizState?>(null)
  private val quizResult = MutableStateFlow<QuizResultState?>(null)
  private val bankFilter = MutableStateFlow(BankFilterState())

  private val _messages = MutableSharedFlow<String>(extraBufferCapacity = 8)
  val messages = _messages.asSharedFlow()

  private var timerJob: Job? = null

  private val localUiFlows = combine(
    currentTab,
    studioConfig,
    activeQuiz,
    quizResult,
    bankFilter,
  ) { tab, studio, quiz, result, filter ->
    LocalUiBundle(tab, studio, quiz, result, filter)
  }

  val uiState: StateFlow<McqUiState> =
    combine(
      repository.questions,
      repository.savedIds,
      repository.history,
      localUiFlows,
    ) { bank, savedList, history, local ->
      val subjects = bank.map { it.subject }.distinct()
      val syncedSelections = buildMap {
        subjects.forEach { subj ->
          put(subj, local.studio.subjectSelections[subj] ?: SubjectSelection(enabled = true, rangeOrCount = ""))
        }
      }
      val syncedStudio = local.studio.copy(subjectSelections = syncedSelections)
      val pool = computePool(bank, subjects, syncedStudio)
      val maxQuestions = max(1, pool.size)
      val clampedStudio = syncedStudio.copy(
        questionCount = syncedStudio.questionCount.coerceIn(1, maxQuestions),
      )

      McqUiState(
        bank = bank,
        subjects = subjects,
        savedIds = savedList.toSet(),
        history = history,
        currentTab = local.tab,
        studio = clampedStudio,
        poolQuestions = pool,
        activeQuiz = local.quiz,
        quizResult = local.result,
        bankFilter = local.filter,
      )
    }.stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5_000),
      initialValue = McqUiState(),
    )

  init {
    viewModelScope.launch {
      repository.ensureSeededOnFirstLaunch()
    }
  }

  fun selectTab(tab: AppTab) {
    timerJob?.cancel()
    activeQuiz.value = null
    quizResult.value = null
    currentTab.value = tab
  }

  // ---------- TEST STUDIO ----------

  fun toggleSubject(subject: String, enabled: Boolean) {
    studioConfig.update { s ->
      val updated = s.subjectSelections.toMutableMap()
      val existing = updated[subject] ?: SubjectSelection()
      updated[subject] = existing.copy(enabled = enabled)
      s.copy(subjectSelections = updated)
    }
  }

  fun updateSubjectRange(subject: String, rangeOrCount: String) {
    studioConfig.update { s ->
      val updated = s.subjectSelections.toMutableMap()
      val existing = updated[subject] ?: SubjectSelection()
      updated[subject] = existing.copy(rangeOrCount = rangeOrCount)
      s.copy(subjectSelections = updated)
    }
  }

  fun toggleSelectAllSubjects() {
    val state = uiState.value
    val allSelected = state.subjects.isNotEmpty() &&
      state.subjects.all { state.studio.subjectSelections[it]?.enabled ?: true }
    studioConfig.update { s ->
      val updated = state.subjects.associateWith { subj ->
        val existing = s.subjectSelections[subj] ?: SubjectSelection()
        existing.copy(enabled = !allSelected)
      }
      s.copy(subjectSelections = updated)
    }
  }

  fun drillSubject(subject: String) {
    val state = uiState.value
    val countInSubject = state.bank.count { it.subject == subject }.coerceAtLeast(1)
    studioConfig.update { s ->
      val updated = state.subjects.associateWith { subj ->
        val existing = s.subjectSelections[subj] ?: SubjectSelection()
        existing.copy(enabled = subj == subject)
      }
      s.copy(
        subjectSelections = updated,
        questionCount = min(countInSubject, s.questionCount).coerceAtLeast(1),
      )
    }
  }

  fun updateTitle(title: String) {
    studioConfig.update { it.copy(title = title) }
  }

  fun updateMode(mode: ExamMode) {
    studioConfig.update { it.copy(mode = mode) }
  }

  fun updateDifficultyFilter(filter: DifficultyFilter) {
    studioConfig.update { it.copy(difficultyFilter = filter) }
  }

  fun updateNegativeMarking(neg: NegativeMarking) {
    studioConfig.update { it.copy(negativeMarking = neg) }
  }

  fun updatePassPercentage(percent: Int) {
    studioConfig.update { it.copy(passPercentage = percent.coerceIn(33, 60)) }
  }

  fun updateZeroRepeats(enabled: Boolean) {
    studioConfig.update { it.copy(zeroRepeats = enabled) }
  }

  fun updateShuffleQuestions(enabled: Boolean) {
    studioConfig.update { it.copy(shuffleQuestions = enabled) }
  }

  fun updateRandomizeOptions(enabled: Boolean) {
    studioConfig.update { it.copy(randomizeOptions = enabled) }
  }

  fun setQuestionCount(count: Int) {
    val maxPool = max(1, uiState.value.poolQuestions.size)
    val clamped = count.coerceIn(1, maxPool)
    studioConfig.update {
      it.copy(
        questionCount = clamped,
        timeMinutes = min(180, clamped * 2),
      )
    }
  }

  fun setTimeMinutes(minutes: Int) {
    studioConfig.update { it.copy(timeMinutes = minutes.coerceIn(1, 180)) }
  }

  fun launchRapidBlitz() {
    val bank = uiState.value.bank
    if (bank.isEmpty()) {
      emitMessage("No questions available. Upload or add MCQs first.")
      return
    }
    val list = bank.shuffled().take(10)
    launchQuiz(
      list = list,
      config = LaunchConfig(
        title = "10-MCQ Rapid Blitz",
        mode = ExamMode.STRICT,
        negativeMarking = 0.0,
        passPercentage = 33,
        timeMinutes = 10,
        randomizeOptions = true,
      ),
    )
  }

  fun launchFullMock() {
    val state = uiState.value
    val pool = state.poolQuestions
    if (pool.isEmpty()) {
      emitMessage("Select at least one subject with questions.")
      return
    }
    val count = pool.size
    val mins = min(180, count * 2).coerceAtLeast(1)
    studioConfig.update { it.copy(questionCount = count, timeMinutes = mins) }

    val ordered = applyQuestionOrdering(pool, state.history, state.studio)
    launchQuiz(
      list = ordered,
      config = LaunchConfig(
        title = state.studio.title.ifBlank { "Mock Test" },
        mode = state.studio.mode,
        negativeMarking = state.studio.negativeMarking.penalty,
        passPercentage = state.studio.passPercentage,
        timeMinutes = mins,
        randomizeOptions = state.studio.randomizeOptions,
      ),
    )
  }

  fun startMockTest() {
    val state = uiState.value
    val pool = state.poolQuestions
    if (pool.isEmpty()) {
      emitMessage("Select at least one subject with questions.")
      return
    }
    val ordered = applyQuestionOrdering(pool, state.history, state.studio)
      .take(state.studio.questionCount)
    launchQuiz(
      list = ordered,
      config = LaunchConfig(
        title = state.studio.title.ifBlank { "Mock Test" },
        mode = state.studio.mode,
        negativeMarking = state.studio.negativeMarking.penalty,
        passPercentage = state.studio.passPercentage,
        timeMinutes = state.studio.timeMinutes,
        randomizeOptions = state.studio.randomizeOptions,
      ),
    )
  }

  private fun applyQuestionOrdering(
    pool: List<QuizQuestion>,
    history: List<AttemptRecord>,
    studio: StudioConfig,
  ): List<QuizQuestion> =
    when {
      studio.zeroRepeats -> {
        val seenIds = history.flatMap { it.questionIds }.toSet()
        val unseen = pool.filter { it.id !in seenIds }.shuffled()
        val seen = pool.filter { it.id in seenIds }.shuffled()
        unseen + seen
      }
      studio.shuffleQuestions -> pool.shuffled()
      else -> pool
    }

  private fun launchQuiz(list: List<QuizQuestion>, config: LaunchConfig) {
    if (list.isEmpty()) {
      emitMessage("No questions to practice.")
      return
    }
    val activeQuestions = list.map { q ->
      val opts = q.options.mapIndexed { idx, text ->
        QuizOptionItem(text = text, isCorrect = idx == q.correctIndex)
      }.let { if (config.randomizeOptions) it.shuffled() else it }
      ActiveQuizQuestion(questionId = q.id, options = opts)
    }
    val totalSeconds = config.timeMinutes.coerceAtLeast(1) * 60
    val endTime = System.currentTimeMillis() + totalSeconds * 1000L

    quizResult.value = null
    activeQuiz.value = ActiveQuizState(
      questions = activeQuestions,
      answers = List(activeQuestions.size) { null },
      currentIndex = 0,
      config = config,
      endTimeMillis = endTime,
      remainingSeconds = totalSeconds,
    )
    startTimer(endTime)
  }

  private fun startTimer(endTimeMillis: Long) {
    timerJob?.cancel()
    timerJob = viewModelScope.launch {
      while (true) {
        val left = ((endTimeMillis - System.currentTimeMillis()) / 1000.0)
          .roundToInt()
          .coerceAtLeast(0)
        activeQuiz.update { it?.copy(remainingSeconds = left) }
        if (left <= 0) {
          finishQuiz()
          break
        }
        delay(500)
      }
    }
  }

  // ---------- QUIZ INTERACTION ----------

  fun pickOption(optionIndex: Int) {
    activeQuiz.update { quiz ->
      if (quiz == null) return@update null
      val currentAnswer = quiz.answers[quiz.currentIndex]
      if (quiz.config.mode == ExamMode.STUDY && currentAnswer != null) {
        return@update quiz
      }
      val newAnswers = quiz.answers.toMutableList()
      newAnswers[quiz.currentIndex] = optionIndex
      quiz.copy(answers = newAnswers)
    }
  }

  fun clearChoice() {
    activeQuiz.update { quiz ->
      if (quiz == null) return@update null
      val currentAnswer = quiz.answers[quiz.currentIndex]
      if (quiz.config.mode == ExamMode.STUDY && currentAnswer != null) {
        return@update quiz
      }
      val newAnswers = quiz.answers.toMutableList()
      newAnswers[quiz.currentIndex] = null
      quiz.copy(answers = newAnswers)
    }
  }

  fun goToQuestion(index: Int) {
    activeQuiz.update { quiz ->
      if (quiz == null) return@update null
      quiz.copy(currentIndex = index.coerceIn(0, quiz.questions.lastIndex))
    }
  }

  fun finishQuiz() {
    val quiz = activeQuiz.value ?: return
    timerJob?.cancel()

    var ok = 0
    var bad = 0
    var skip = 0
    val wrongIds = mutableListOf<String>()
    val skippedIds = mutableListOf<String>()

    quiz.questions.forEachIndexed { idx, q ->
      val a = quiz.answers[idx]
      if (a == null) {
        skip++
        skippedIds.add(q.questionId)
      } else if (q.options.getOrNull(a)?.isCorrect == true) {
        ok++
      } else {
        bad++
        wrongIds.add(q.questionId)
      }
    }

    val score = ok - bad * quiz.config.negativeMarking
    val pct = max(0.0, (score / quiz.questions.size.toDouble()) * 100.0)
    val roundedPct = (pct * 10.0).roundToInt() / 10.0
    val passed = pct >= quiz.config.passPercentage

    val dateStr = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).format(Date())
    val record = AttemptRecord(
      id = SampleData.newId(),
      title = quiz.config.title,
      mode = quiz.config.mode,
      dateLabel = dateStr,
      percentage = roundedPct,
      passed = passed,
      questionIds = quiz.questions.map { it.questionId },
      negativeMarking = quiz.config.negativeMarking,
      passPercentage = quiz.config.passPercentage,
      timeMinutes = quiz.config.timeMinutes,
      randomizeOptions = quiz.config.randomizeOptions,
    )

    viewModelScope.launch {
      repository.recordAttempt(record)
    }

    activeQuiz.value = null
    quizResult.value = QuizResultState(
      questions = quiz.questions,
      answers = quiz.answers,
      config = quiz.config,
      correctCount = ok,
      wrongCount = bad,
      skippedCount = skip,
      score = score,
      percentage = pct,
      passed = passed,
      wrongIds = wrongIds,
      skippedIds = skippedIds,
    )
  }

  fun quitQuizToStudio() {
    timerJob?.cancel()
    activeQuiz.value = null
    quizResult.value = null
    currentTab.value = AppTab.STUDIO
  }

  fun reattemptIncorrect() {
    val res = quizResult.value ?: return
    val byId = uiState.value.bank.associateBy { it.id }
    val list = (res.wrongIds + res.skippedIds).mapNotNull { byId[it] }
    if (list.isEmpty()) {
      emitMessage("No incorrect or skipped MCQs to retry.")
      return
    }
    launchQuiz(
      list = list,
      config = LaunchConfig(
        title = "Re-attempt: ${res.config.title}",
        mode = ExamMode.STUDY,
        negativeMarking = 0.0,
        passPercentage = 33,
        timeMinutes = max(1, list.size * 2),
        randomizeOptions = true,
      ),
    )
  }

  fun practiceSaved() {
    val state = uiState.value
    val byId = state.bank.associateBy { it.id }
    val list = state.savedIds.mapNotNull { byId[it] }.shuffled()
    if (list.isEmpty()) {
      emitMessage("No saved MCQs to practice.")
      return
    }
    launchQuiz(
      list = list,
      config = LaunchConfig(
        title = "Saved MCQs Practice",
        mode = ExamMode.STUDY,
        negativeMarking = 0.0,
        passPercentage = 33,
        timeMinutes = max(1, list.size * 2),
        randomizeOptions = true,
      ),
    )
  }

  fun retakeAttempt(record: AttemptRecord) {
    val byId = uiState.value.bank.associateBy { it.id }
    val list = record.questionIds.mapNotNull { byId[it] }
    if (list.isEmpty()) {
      emitMessage("Original questions for this attempt were deleted.")
      return
    }
    launchQuiz(
      list = list,
      config = LaunchConfig(
        title = record.title,
        mode = record.mode,
        negativeMarking = record.negativeMarking,
        passPercentage = record.passPercentage,
        timeMinutes = record.timeMinutes.coerceAtLeast(1),
        randomizeOptions = record.randomizeOptions,
      ),
    )
  }

  // ---------- BOOKMARKS & BANK ----------

  fun toggleSaved(questionId: String) {
    val isCurrentlySaved = questionId in uiState.value.savedIds
    viewModelScope.launch {
      repository.toggleSaved(questionId, isCurrentlySaved)
    }
  }

  fun clearAllSaved() {
    viewModelScope.launch {
      repository.clearAllSaved()
    }
  }

  fun updateBankSearch(query: String) {
    bankFilter.update { it.copy(searchQuery = query) }
  }

  fun updateBankSubjectFilter(subject: String) {
    bankFilter.update { it.copy(subjectFilter = subject) }
  }

  fun updateBankDifficultyFilter(filter: DifficultyFilter) {
    bankFilter.update { it.copy(difficultyFilter = filter) }
  }

  fun addQuestion(
    subject: String,
    difficulty: Difficulty,
    question: String,
    options: List<String>,
    correctIndex: Int,
    explanation: String,
  ) {
    val cleanSubject = subject.trim().ifEmpty { "General" }
    val cleanQuestion = question.trim()
    if (cleanQuestion.isEmpty() || options.any { it.trim().isEmpty() }) {
      emitMessage("Please fill in the question and all 4 options.")
      return
    }
    viewModelScope.launch {
      repository.addQuestion(
        QuizQuestion(
          id = SampleData.newId(),
          subject = cleanSubject,
          difficulty = difficulty,
          question = cleanQuestion,
          options = options.map { it.trim() },
          correctIndex = correctIndex.coerceIn(0, 3),
          explanation = explanation.trim(),
        )
      )
      emitMessage("MCQ added to $cleanSubject.")
    }
  }

  fun updateQuestion(updated: QuizQuestion) {
    viewModelScope.launch {
      repository.updateQuestion(updated)
      emitMessage("Question updated.")
    }
  }

  fun deleteQuestion(id: String) {
    viewModelScope.launch {
      repository.deleteQuestion(id)
    }
  }

  fun deleteSubject(subject: String) {
    viewModelScope.launch {
      repository.deleteSubject(subject)
      if (bankFilter.value.subjectFilter == subject) {
        bankFilter.update { it.copy(subjectFilter = "All") }
      }
      studioConfig.update { s ->
        val mutable = s.subjectSelections.toMutableMap()
        mutable.remove(subject)
        s.copy(subjectSelections = mutable)
      }
      emitMessage("Deleted subject \"$subject\".")
    }
  }

  // ---------- UPLOAD & EXPORT ----------

  fun importContent(raw: String, isCsv: Boolean) {
    val result = if (isCsv) McqParser.parseCsv(raw) else McqParser.parseJson(raw)
    result
      .onSuccess { list ->
        if (list.isEmpty()) {
          emitMessage("No valid questions found.")
        } else {
          viewModelScope.launch {
            repository.addQuestions(list)
            studioConfig.update { it.copy(subjectSelections = emptyMap()) }
            emitMessage("${list.size} MCQs added.")
            selectTab(AppTab.BANK)
          }
        }
      }
      .onFailure { err ->
        emitMessage("Could not read that file: ${err.message ?: "Invalid format"}")
      }
  }

  fun loadSamplePack() {
    val pack = SampleData.pakStudiesPack()
    viewModelScope.launch {
      repository.addQuestions(pack)
      studioConfig.update { it.copy(subjectSelections = emptyMap()) }
      emitMessage("${pack.size} MCQs added.")
      selectTab(AppTab.BANK)
    }
  }

  fun exportBankAsJson(): String = McqParser.exportToJson(uiState.value.bank)

  // ---------- ANALYTICS ----------

  fun clearAllHistory() {
    viewModelScope.launch {
      repository.clearAllHistory()
    }
  }

  fun notifyCopied() {
    emitMessage("Copied")
  }

  fun notifyMessage(msg: String) {
    emitMessage(msg)
  }

  private fun emitMessage(msg: String) {
    _messages.tryEmit(msg)
  }

  private fun computePool(
    bank: List<QuizQuestion>,
    subjects: List<String>,
    studio: StudioConfig,
  ): List<QuizQuestion> {
    val out = mutableListOf<QuizQuestion>()
    subjects.forEach { subj ->
      val sel = studio.subjectSelections[subj] ?: SubjectSelection(enabled = true)
      if (!sel.enabled) return@forEach
      val filtered = bank.filter {
        it.subject == subj && studio.difficultyFilter.matches(it.difficulty)
      }
      val range = parseRange(sel.rangeOrCount, filtered.size)
      if (range.first < range.last) {
        out.addAll(filtered.subList(range.first, range.last))
      }
    }
    return out
  }

  private fun parseRange(raw: String, len: Int): IntRange {
    val trimmed = raw.trim()
    if (trimmed.isEmpty()) return 0 until len
    val rangeMatch = Regex("^(\\d+)\\s*-\\s*(\\d+)$").matchEntire(trimmed)
    if (rangeMatch != null) {
      val start = max(0, (rangeMatch.groupValues[1].toIntOrNull() ?: 1) - 1)
      val end = min(len, rangeMatch.groupValues[2].toIntOrNull() ?: len)
      return start until max(start, end)
    }
    if (Regex("^\\d+$").matches(trimmed)) {
      val count = min(len, trimmed.toIntOrNull() ?: len)
      return 0 until max(0, count)
    }
    return 0 until len
  }

  private data class LocalUiBundle(
    val tab: AppTab,
    val studio: StudioConfig,
    val quiz: ActiveQuizState?,
    val result: QuizResultState?,
    val filter: BankFilterState,
  )
}
