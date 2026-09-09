package com.rustech.cpplearn.data.model

/**
 * Firestore collection: lessons/{lessonId}
 */
data class Lesson(
    val id: String = "",
    val order: Int = 0,
    val title: String = "",
    val summary: String = "",
    // Markdown-ish plain text broken into "slides" for the animated tutorial view
    val contentSlides: List<String> = emptyList(),
    // Optional Lottie animation JSON URL (Firebase Storage). Empty = use default code-reveal animation.
    val animationUrl: String = "",
    // Voice-over audio file URL (Firebase Storage). Only plays when the user taps the button.
    val voiceoverUrl: String = ""
)

/**
 * Firestore collection: quizzes/{lessonId}
 */
data class QuizQuestion(
    val id: String = "",
    val question: String = "",
    val choices: List<String> = emptyList(),
    val correctIndex: Int = 0
)

/**
 * Firestore collection: users/{uid}
 */
data class UserProfile(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val dateRegistered: Long = 0L,
    val lastLogin: Long = 0L,
    val totalScore: Int = 0,
    val lessonsCompleted: Int = 0
)

/**
 * Firestore collection: scores/{uid}_{lessonId}
 */
data class LessonScore(
    val uid: String = "",
    val lessonId: String = "",
    val score: Int = 0,
    val totalQuestions: Int = 0,
    val dateCompleted: Long = 0L
)

/**
 * Leaderboard row shown in LeaderboardScreen (derived from UserProfile).
 */
data class LeaderboardEntry(
    val uid: String = "",
    val name: String = "",
    val totalScore: Int = 0,
    val lessonsCompleted: Int = 0
)
