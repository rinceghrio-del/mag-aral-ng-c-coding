"""
Isang beses lang patakbuhin ito para i-upload ang 10 lessons + quizzes
papunta sa Firestore.

Setup:
    pip install firebase-admin
    1. Sa Firebase Console > Project Settings > Service Accounts > Generate new private key
    2. I-save ang na-download na JSON bilang "serviceAccountKey.json" sa parehong folder nito
    3. Patakbuhin: python seed_firestore.py
"""

import json
import firebase_admin
from firebase_admin import credentials, firestore

cred = credentials.Certificate("serviceAccountKey.json")
firebase_admin.initialize_app(cred)
db = firestore.client()

with open("lessons_seed.json", "r", encoding="utf-8") as f:
    data = json.load(f)

# Upload lessons
for lesson in data["lessons"]:
    lesson_id = lesson["id"]
    db.collection("lessons").document(lesson_id).set(lesson)
    print(f"Na-upload ang lesson: {lesson_id}")

# Upload quizzes (subcollection: quizzes/{lessonId}/questions/{autoId})
for lesson_id, questions in data["quizzes"].items():
    quiz_doc = db.collection("quizzes").document(lesson_id)
    quiz_doc.set({"lessonId": lesson_id})  # placeholder parent doc
    for q in questions:
        quiz_doc.collection("questions").add(q)
    print(f"Na-upload ang quiz para sa: {lesson_id} ({len(questions)} questions)")

print("Tapos na! Naka-seed na ang Firestore.")
