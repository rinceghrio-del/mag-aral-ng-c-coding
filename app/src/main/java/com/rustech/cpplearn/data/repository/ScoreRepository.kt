package com.rustech.cpplearn.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.rustech.cpplearn.data.model.LeaderboardEntry
import com.rustech.cpplearn.data.model.LessonScore
import kotlinx.coroutines.tasks.await

class ScoreRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    /** Saves a completed quiz score and updates the user's running total. */
    suspend fun submitScore(uid: String, lessonId: String, score: Int, totalQuestions: Int) {
        val docId = "${uid}_$lessonId"
        val lessonScore = LessonScore(
            uid = uid,
            lessonId = lessonId,
            score = score,
            totalQuestions = totalQuestions,
            dateCompleted = System.currentTimeMillis()
        )
        db.collection("scores").document(docId).set(lessonScore).await()

        // Recompute totals for this user from all their saved scores.
        val allScores = db.collection("scores")
            .whereEqualTo("uid", uid).get().await()
            .toObjects(LessonScore::class.java)

        val totalScore = allScores.sumOf { it.score }
        val lessonsCompleted = allScores.size

        db.collection("users").document(uid).update(
            mapOf(
                "totalScore" to totalScore,
                "lessonsCompleted" to lessonsCompleted
            )
        ).await()
    }

    /** Top users ranked by total score, for the Leaderboard screen. */
    suspend fun getLeaderboard(limit: Long = 50): List<LeaderboardEntry> {
        val snapshot = db.collection("users")
            .orderBy("totalScore", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .limit(limit)
            .get().await()

        return snapshot.documents.map { doc ->
            LeaderboardEntry(
                uid = doc.id,
                name = doc.getString("name") ?: "Anonymous",
                totalScore = (doc.getLong("totalScore") ?: 0L).toInt(),
                lessonsCompleted = (doc.getLong("lessonsCompleted") ?: 0L).toInt()
            )
        }
    }
}
