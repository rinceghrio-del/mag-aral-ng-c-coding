package com.rustech.cpplearn.ui.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.firebase.firestore.FirebaseFirestore
import com.rustech.cpplearn.data.model.UserProfile
import com.rustech.cpplearn.data.repository.AuthRepository
import kotlinx.coroutines.tasks.await

@Composable
fun ProfileScreen(onLoggedOut: () -> Unit) {
    val authRepository = remember { AuthRepository() }
    var profile by remember { mutableStateOf<UserProfile?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        val uid = authRepository.currentUid
        if (uid != null) {
            val doc = FirebaseFirestore.getInstance().collection("users").document(uid).get().await()
            profile = doc.toObject(UserProfile::class.java)
        }
        isLoading = false
    }

    Scaffold(topBar = { TopAppBar(title = { Text("Profile") }) }) { padding ->
        if (isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        Column(modifier = Modifier.padding(padding).fillMaxSize().padding(20.dp)) {
            Text(profile?.name ?: "Guest", style = MaterialTheme.typography.headlineMedium)
            Text(profile?.email ?: "", style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(20.dp))
            Text("Lessons completed: ${profile?.lessonsCompleted ?: 0}")
            Text("Total score: ${profile?.totalScore ?: 0}")
            Spacer(Modifier.height(32.dp))
            Button(
                onClick = {
                    authRepository.logout()
                    onLoggedOut()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Logout")
            }
        }
    }
}
