/**
 * Alternatibong paraan para i-seed ang Firestore, HINDI gumagamit ng
 * service account key (kasi naka-restrict 'yun sa project mo dahil sa
 * organization policy). Gumagamit ito ng parehong client SDK na gamit
 * ng Android app mismo.
 *
 * Gumagana ito dahil "test mode" pa ang Firestore rules mo (bukas ang
 * access nang walang login, hanggang sa expiration date nito na
 * awtomatikong nakalagay noong ginawa mo ang database).
 *
 * Setup:
 *   npm install firebase
 *   node seed_client.js
 */

const { initializeApp } = require("firebase/app");
const {
  getFirestore,
  doc,
  setDoc,
  collection,
  addDoc,
} = require("firebase/firestore");
const fs = require("fs");
const path = require("path");

// Values galing mismo sa google-services.json mo (rustech-cpp-academy project)
const firebaseConfig = {
  apiKey: "AIzaSyDf-Jn6BH04pH5PVGyWOGVkBLutrGIRcp0",
  authDomain: "rustech-cpp-academy.firebaseapp.com",
  projectId: "rustech-cpp-academy",
  storageBucket: "rustech-cpp-academy.firebasestorage.app",
};

const app = initializeApp(firebaseConfig);
const db = getFirestore(app);

const dataPath = path.join(__dirname, "lessons_seed.json");
const data = JSON.parse(fs.readFileSync(dataPath, "utf-8"));

async function seed() {
  for (const lesson of data.lessons) {
    await setDoc(doc(db, "lessons", lesson.id), lesson);
    console.log(`Na-upload ang lesson: ${lesson.id}`);
  }

  for (const [lessonId, questions] of Object.entries(data.quizzes)) {
    // Placeholder parent doc para may laman ang quizzes/{lessonId}
    await setDoc(doc(db, "quizzes", lessonId), { lessonId });

    for (const q of questions) {
      await addDoc(collection(db, "quizzes", lessonId, "questions"), q);
    }
    console.log(
      `Na-upload ang quiz para sa: ${lessonId} (${questions.length} questions)`
    );
  }

  console.log("Tapos na! Naka-seed na ang Firestore.");
  process.exit(0);
}

seed().catch((err) => {
  console.error("May error:", err.message);
  process.exit(1);
});
