# RUSTECH C++ Academy — Android App (MVP)

Android app na nagtuturo ng C++ coding lesson by lesson, may animated
tutorial slides, click-to-play voice-over, quiz kada lesson, leaderboard,
at login/register gamit ang Firebase.

## Ano ang laman ng project na ito

```
CppLearnApp/
  app/src/main/java/com/rustech/cpplearn/
    MainActivity.kt              -> entry point, hosts ang navigation
    RustechCppLearnApp.kt        -> nag-i-initialize ng Firebase
    navigation/NavGraph.kt       -> lahat ng screens at routes
    data/model/Models.kt         -> Lesson, QuizQuestion, UserProfile, atbp.
    data/repository/             -> Auth, Content (lessons/quiz), Score/Leaderboard
    ui/screen/                   -> Splash, Login, Register, Home, Lesson, Quiz,
                                     Leaderboard, Profile
    ui/components/VoiceOverButton.kt -> click-to-play audio button (hindi autoplay)
    ui/theme/                    -> RUSTECH color palette + typography
  firebase_seed/
    lessons_seed.json            -> 10 sample C++ lessons + quiz questions (Taglish)
    seed_firestore.py            -> script para i-upload ang seed data
    firestore.rules              -> draft security rules
```

## Setup steps

1. **Gawin ang Firebase project**
   - Pumunta sa https://console.firebase.google.com
   - Gumawa ng bagong project (halimbawa: "rustech-cpp-academy")
   - I-enable ang **Authentication > Email/Password**
   - I-enable ang **Cloud Firestore** (start sa test mode muna, palitan ng
     `firestore.rules` na nasa `firebase_seed/` bago mag-launch publicly)
   - I-enable ang **Storage** (para sa voice-over audio files at animation assets)

2. **Ikonekta ang Android app sa Firebase**
   - Sa Firebase Console, magdagdag ng Android app gamit ang package name
     `com.rustech.cpplearn`
   - I-download ang `google-services.json`
   - Ilagay ito sa `CppLearnApp/app/google-services.json` (kasabay ng
     `build.gradle.kts` ng app module)

3. **I-open sa Android Studio**
   - Open `CppLearnApp/` bilang existing project
   - Hintayin mag-sync ang Gradle (kukunin nito lahat ng dependencies)
   - Run sa emulator o physical device

4. **I-seed ang 10 lessons papunta sa Firestore**
   - Sa Firebase Console > Project Settings > Service Accounts >
     "Generate new private key" — i-save bilang
     `firebase_seed/serviceAccountKey.json`
   - `pip install firebase-admin`
   - `cd firebase_seed && python seed_firestore.py`
   - Ito ang mag-a-upload ng 10 lessons at kanya-kanyang quiz questions

5. **Voice-over audio files (optional pero recommended)**
   - Mag-record o gumawa ng short audio (MP3) per lesson
   - I-upload sa Firebase Storage, kunin ang public URL
   - I-update ang `voiceoverUrl` field ng bawat lesson doc sa Firestore
     (o dagdagan ang `lessons_seed.json` bago mag-seed)

6. **Animations**
   - Kasalukuyan, ang "animated tutorial" ay animated slide transitions
     (fade + slide) sa pagitan ng content slides — gumagana agad, walang
     kailangang asset.
   - Kung gusto mo ng mas advanced na Lottie animations (mascot,
     character, atbp.), maglagay ng `.json` Lottie file sa Firebase
     Storage at i-set ang `animationUrl` field — konektado na ang
     Lottie library sa dependencies, susunod na step na lang ang
     pag-render nito sa LessonScreen.

## GitHub Actions CI (auto-build ng APK)

May `.github/workflows/main.yml` na ito na awtomatikong bumubuild ng debug
APK tuwing may push sa `main` branch. Kailangan mo munang i-set up ang
Firebase secret nang tama, dahil ang direktang pag-paste ng buong JSON
bilang secret ay madalas masira (naiiba ang quotes/newlines pagdaan sa
YAML). Gamit natin dito ang **base64-encoded** na bersyon para siguradong
hindi masisira.

### Paano gumawa ng GOOGLE_SERVICES_JSON_B64 secret

**Sa Mac/Linux:**
```
base64 -w 0 app/google-services.json
```
(kung walang `-w 0` flag sa Mac, gamitin: `base64 app/google-services.json | tr -d '\n'`)

**Sa Windows (PowerShell):**
```
[Convert]::ToBase64String([IO.File]::ReadAllBytes("app/google-services.json"))
```

Kopyahin ang buong output (isang mahabang linya, walang spaces/newlines).

1. Sa GitHub repo mo, pumunta sa **Settings → Secrets and variables → Actions**
2. **New repository secret**
3. Name: `GOOGLE_SERVICES_JSON_B64`
4. Value: i-paste ang base64 output na kinopya mo
5. **Add secret**

Kapag may push ka sa `main`, awtomatiko nang bubuo ang APK — makikita mo
sa **Actions tab → (latest run) → Artifacts → cpplearn-debug-apk**.

## Admin Panel

May `admin_panel/index.html` na — isang single-file na web dashboard (walang
build tools, walang npm), gamit ang parehong Firebase project. Tatlong tabs:
Users, Leaderboard, Login Activity. Naka-lock ito sa isang admin email lang
(nakalagay na sa loob ng file, sa `ADMIN_EMAILS` array).

### Paano i-deploy (Firebase Hosting)

## Susunod na dapat gawin (hindi pa kasama dito)

- **Admin Panel** (web dashboard para makita ang users, login activity,
  at leaderboard) — hiwalay na web app ito, gagawin natin next.
- Pag-upload ng aktwal na voice-over recordings at Lottie animations
- App icon at splash graphics na naka-brand sa RUSTECH
- Push notifications (reminders para mag-aral)

## Tungkol sa design

Gamit dito ang isang RUSTECH-themed dark palette (deep navy background,
electric cyan accent, amber para sa leaderboard/highlights) para
maramdaman ng users na "techy" at masaya ang app — nasa
`ui/theme/Color.kt` kung gusto mo baguhin ang mga kulay.
