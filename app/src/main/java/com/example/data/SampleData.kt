package com.example.data

import com.example.data.model.Difficulty
import com.example.data.model.QuizQuestion
import java.util.UUID

object SampleData {
  fun newId(): String = "q" + UUID.randomUUID().toString().replace("-", "").take(12)

  fun pakStudiesPack(): List<QuizQuestion> =
    listOf(
      QuizQuestion(
        id = newId(),
        subject = "Pak Studies",
        difficulty = Difficulty.MEDIUM,
        question = "What is Pakistan's recent $7 billion IMF loan package (approved in September 2024) officially called?",
        options = listOf(
          "Stand-By Arrangement (SBA)",
          "Extended Fund Facility (EFF)",
          "Rapid Financing Instrument (RFI)",
          "Poverty Reduction and Growth Facility (PRGF)",
        ),
        correctIndex = 1,
        explanation = "In September 2024 the IMF Executive Board approved a 37-month Extended Fund Facility for Pakistan.",
      ),
      QuizQuestion(
        id = newId(),
        subject = "Pak Studies",
        difficulty = Difficulty.MEDIUM,
        question = "Who presented the Lahore Resolution in 1940?",
        options = listOf(
          "Allama Iqbal",
          "A.K. Fazlul Huq",
          "Liaquat Ali Khan",
          "Chaudhry Rahmat Ali",
        ),
        correctIndex = 1,
        explanation = "A.K. Fazlul Huq (Sher-e-Bengal) moved the resolution on 23 March 1940.",
      ),
      QuizQuestion(
        id = newId(),
        subject = "Pak Studies",
        difficulty = Difficulty.EASY,
        question = "Who is the founder of Pakistan?",
        options = listOf(
          "Allama Iqbal",
          "Liaquat Ali Khan",
          "Muhammad Ali Jinnah",
          "Sir Syed Ahmad Khan",
        ),
        correctIndex = 2,
        explanation = "Quaid-e-Azam Muhammad Ali Jinnah founded Pakistan.",
      ),
      QuizQuestion(
        id = newId(),
        subject = "Pak Studies",
        difficulty = Difficulty.EASY,
        question = "What was the capital of Pakistan before Islamabad?",
        options = listOf(
          "Lahore",
          "Karachi",
          "Rawalpindi",
          "Peshawar",
        ),
        correctIndex = 1,
        explanation = "Karachi was the first capital; Rawalpindi served temporarily before Islamabad.",
      ),
      QuizQuestion(
        id = newId(),
        subject = "Pak Studies",
        difficulty = Difficulty.MEDIUM,
        question = "In which year did Pakistan become an Islamic Republic with its first constitution?",
        options = listOf("1947", "1950", "1956", "1962"),
        correctIndex = 2,
        explanation = "The 1956 constitution declared Pakistan an Islamic Republic on 23 March 1956.",
      ),
      QuizQuestion(
        id = newId(),
        subject = "Pak Studies",
        difficulty = Difficulty.EASY,
        question = "Which is the longest river of Pakistan?",
        options = listOf("Jhelum", "Indus", "Ravi", "Sutlej"),
        correctIndex = 1,
        explanation = "The Indus is the longest river, flowing about 3,180 km.",
      ),
      QuizQuestion(
        id = newId(),
        subject = "Pak Studies",
        difficulty = Difficulty.EASY,
        question = "K2, in Pakistan, is the world's ___ highest mountain.",
        options = listOf("First", "Second", "Third", "Fourth"),
        correctIndex = 1,
        explanation = "K2 (8,611 m) is second only to Mount Everest.",
      ),
      QuizQuestion(
        id = newId(),
        subject = "Pak Studies",
        difficulty = Difficulty.MEDIUM,
        question = "The Objectives Resolution was passed in which year?",
        options = listOf("1947", "1949", "1951", "1956"),
        correctIndex = 1,
        explanation = "The Constituent Assembly passed it on 12 March 1949.",
      ),
      QuizQuestion(
        id = newId(),
        subject = "Pak Studies",
        difficulty = Difficulty.HARD,
        question = "The Khyber Pass connects Pakistan with which country?",
        options = listOf("Iran", "China", "Afghanistan", "India"),
        correctIndex = 2,
        explanation = "The Khyber Pass links Peshawar (KP) to Jalalabad in Afghanistan.",
      ),
      QuizQuestion(
        id = newId(),
        subject = "Pak Studies",
        difficulty = Difficulty.EASY,
        question = "What is the national language of Pakistan?",
        options = listOf("Punjabi", "Urdu", "Sindhi", "Pashto"),
        correctIndex = 1,
        explanation = "Urdu is the national language; English and Urdu are official.",
      ),
    )

  val SAMPLE_JSON_TEMPLATE = """
[
  {
    "subject": "Pakistan Studies",
    "difficulty": "Medium",
    "question": "Who presented the Lahore Resolution?",
    "options": [
      "Allama Iqbal",
      "A.K. Fazlul Huq",
      "Liaquat Ali Khan",
      "M.A. Jinnah"
    ],
    "correct_index": 1,
    "explanation": "A.K. Fazlul Huq moved the resolution in 1940."
  }
]
  """.trimIndent()
}
