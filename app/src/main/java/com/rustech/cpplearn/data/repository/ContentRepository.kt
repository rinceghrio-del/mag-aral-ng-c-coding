package com.rustech.cpplearn.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.rustech.cpplearn.data.model.Lesson
import com.rustech.cpplearn.data.model.QuizQuestion
import kotlinx.coroutines.tasks.await

class ContentRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    suspend fun getLessons(): List<Lesson> {
        val snapshot = db.collection("lessons").orderBy("order").get().await()
        return snapshot.documents.mapNotNull { doc ->
            doc.toObject(Lesson::class.java)?.copy(id = doc.id)
        }
    }

    suspend fun getLesson(lessonId: String): Lesson? {
        val doc = db.collection("lessons").document(lessonId).get().await()
        return doc.toObject(Lesson::class.java)?.copy(id = doc.id)
    }

    suspend fun getQuiz(lessonId: String): List<QuizQuestion> {
        val snapshot = db.collection("quizzes").document(lessonId)
            .collection("questions").get().await()
        return snapshot.documents.mapNotNull { doc ->
            doc.toObject(QuizQuestion::class.java)?.copy(id = doc.id)
        }
    }
}
