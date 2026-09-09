package com.rustech.cpplearn

import android.app.Application
import com.google.firebase.FirebaseApp

/**
 * Application class. Initializes Firebase once when the app starts.
 * Requires app/google-services.json (downloaded from the Firebase console)
 * to be placed in the app/ module folder before building.
 */
class RustechCppLearnApp : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
    }
}
