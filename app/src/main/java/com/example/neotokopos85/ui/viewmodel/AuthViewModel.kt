package com.example.neotokopos85.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import androidx.compose.runtime.mutableStateOf

val Context.dataStore by preferencesDataStore("user_prefs")
private val ROLE_KEY = stringPreferencesKey("role")

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val context = application.applicationContext

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    private val _userRole = MutableStateFlow<String?>(null)
    val userRole: StateFlow<String?> = _userRole

    var isLoggedIn = mutableStateOf(false)
        private set

    init {
        viewModelScope.launch {

            val savedRole = context.dataStore.data.map { prefs ->
                prefs[ROLE_KEY]
            }.first()

            _userRole.value = savedRole
            isLoggedIn.value = !savedRole.isNullOrEmpty()
        }
    }

    // =========================
    // LOGIN
    // =========================

    fun login(
        email: String,
        password: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {

        auth.signInWithEmailAndPassword(email, password)

            .addOnSuccessListener { result ->

                val uid = result.user?.uid

                if (uid != null) {
                    loadRole(uid, onSuccess, onError)
                } else {
                    onError("User tidak ditemukan")
                }
            }

            .addOnFailureListener { e ->

                val message = when {

                    e.message?.contains("password", true) == true ->
                        "Password salah"

                    e.message?.contains("no user", true) == true ->
                        "Email tidak ditemukan"

                    else ->
                        "Login gagal"
                }

                onError(message)
            }
    }

    // =========================
    // LOAD ROLE USER
    // =========================

    private fun loadRole(
        uid: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {

        firestore.collection("users")
            .document(uid)
            .get()

            .addOnSuccessListener { document ->

                val role = document.getString("role")

                if (role == null) {

                    onError("Role user tidak ditemukan")
                    return@addOnSuccessListener
                }

                _userRole.value = role
                isLoggedIn.value = true

                viewModelScope.launch {

                    context.dataStore.edit { prefs ->
                        prefs[ROLE_KEY] = role
                    }
                }

                onSuccess()
            }

            .addOnFailureListener {

                onError("Gagal mengambil data user")
            }
    }

    // =========================
    // LOGOUT
    // =========================

    fun logout() {

        auth.signOut()

        _userRole.value = null
        isLoggedIn.value = false

        viewModelScope.launch {

            context.dataStore.edit { prefs ->
                prefs.remove(ROLE_KEY)
            }
        }
    }
}