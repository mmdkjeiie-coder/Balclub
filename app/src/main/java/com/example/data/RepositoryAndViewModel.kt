package com.example.data

import android.content.Context
import androidx.room.Room
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

sealed class LoginState {
    object Idle : LoginState()
    object Loading : LoginState()
    object Success : LoginState()
    data class Error(val message: String) : LoginState()
}

class BalClubRepository(private val db: AppDatabase) {
    val users: Flow<List<User>> = db.userDao().getAllUsers()
    val minutes: Flow<List<Minute>> = db.minuteDao().getAllMinutes()
    val events: Flow<List<Event>> = db.eventDao().getAllEvents()
    val notices: Flow<List<Notice>> = db.noticeDao().getAllNotices()

    suspend fun insertUser(user: User) = db.userDao().insertUser(user)
    suspend fun deleteUser(user: User) = db.userDao().deleteUser(user)
    suspend fun insertMinute(minute: Minute) = db.minuteDao().insertMinute(minute)
    suspend fun insertEvent(event: Event) = db.eventDao().insertEvent(event)
    suspend fun insertNotice(notice: Notice) = db.noticeDao().insertNotice(notice)

    companion object {
        @Volatile
        private var INSTANCE: BalClubRepository? = null

        fun getInstance(context: Context): BalClubRepository {
            return INSTANCE ?: synchronized(this) {
                val database = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "bal_club_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                BalClubRepository(database).also { INSTANCE = it }
            }
        }
    }
}

class BalClubViewModel(private val repository: BalClubRepository) : ViewModel() {

    // For simplicity, switch between Secretary and Member manually
    private val _currentUserRole = MutableStateFlow(Role.Sadasya)
    val currentUserRole: StateFlow<Role> = _currentUserRole.asStateFlow()

    private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)
    val loginState: StateFlow<LoginState> = _loginState.asStateFlow()

    fun loginWithCode(code: String) {
        viewModelScope.launch {
            _loginState.value = LoginState.Loading
            try {
                // Try Firestore first
                val db = FirebaseFirestore.getInstance()
                val snapshot = db.collection("inviteCodes").document(code).get().await()
                if (snapshot.exists()) {
                    val roleString = snapshot.getString("role")
                    val role = Role.values().find { it.name.equals(roleString, ignoreCase = true) } ?: Role.Sadasya
                    _currentUserRole.value = role
                    _loginState.value = LoginState.Success
                    return@launch
                }
            } catch (e: Exception) {
                // Firebase not initialized or network error
                e.printStackTrace()
            }

            // Fallback for demonstration since Firebase might not be configured
            if (code == "ADMIN123") {
                _currentUserRole.value = Role.Admin
                _loginState.value = LoginState.Success
            } else if (code == "SEC456") {
                _currentUserRole.value = Role.Sachiv
                _loginState.value = LoginState.Success
            } else if (code == "MEMBER123") {
                _currentUserRole.value = Role.Sadasya
                _loginState.value = LoginState.Success
            } else {
                _loginState.value = LoginState.Error("Invalid Invite Code. Try ADMIN123 or MEMBER123.")
            }
        }
    }

    fun switchRole() {
        _currentUserRole.value = if (_currentUserRole.value == Role.Sachiv) Role.Sadasya else Role.Sachiv
    }
    
    fun logout() {
        _loginState.value = LoginState.Idle
    }

    private val _users = MutableStateFlow<List<User>>(emptyList())
    val users: StateFlow<List<User>> = _users.asStateFlow()

    private val _minutes = MutableStateFlow<List<Minute>>(emptyList())
    val minutes: StateFlow<List<Minute>> = _minutes.asStateFlow()

    private val _events = MutableStateFlow<List<Event>>(emptyList())
    val events: StateFlow<List<Event>> = _events.asStateFlow()

    private val _notices = MutableStateFlow<List<Notice>>(emptyList())
    val notices: StateFlow<List<Notice>> = _notices.asStateFlow()

    private val _suggestions = MutableStateFlow<List<Suggestion>>(emptyList())
    val suggestions: StateFlow<List<Suggestion>> = _suggestions.asStateFlow()

    private val _photos = MutableStateFlow<List<PhotoAlbumUrl>>(emptyList())
    val photos: StateFlow<List<PhotoAlbumUrl>> = _photos.asStateFlow()

    init {
        // Fallback or seed data initially
        viewModelScope.launch {
            repository.users.collect { _users.value = it }
        }
        viewModelScope.launch {
            repository.minutes.collect { _minutes.value = it }
        }
        viewModelScope.launch {
            repository.events.collect { _events.value = it }
        }
        viewModelScope.launch {
            repository.notices.collect { _notices.value = it }
        }
        
        setupFirebaseListeners()
    }

    private fun setupFirebaseListeners() {
        try {
            val db = FirebaseFirestore.getInstance()
            
            db.collection("users").addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    val firebaseUsers = snapshot.documents.mapNotNull { doc ->
                        try {
                            val roleString = doc.getString("role") ?: return@mapNotNull null
                            User(
                                id = doc.getLong("id")?.toInt() ?: 0,
                                name = doc.getString("name") ?: "",
                                role = Role.values().find { it.name == roleString } ?: Role.Sadasya,
                                isActive = doc.getBoolean("isActive") ?: true
                            )
                        } catch (e: Exception) { null }
                    }
                    if (firebaseUsers.isNotEmpty()) _users.value = firebaseUsers
                }
            }

            db.collection("minutes").addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    val firebaseMinutes = snapshot.documents.mapNotNull { doc ->
                        try {
                            Minute(
                                id = doc.getLong("id")?.toInt() ?: 0,
                                template = MinuteTemplate.values().find { it.name == doc.getString("template") } ?: MinuteTemplate.Standard,
                                title = doc.getString("title") ?: "",
                                date = doc.getLong("date") ?: 0L,
                                chairperson = doc.getString("chairperson") ?: "",
                                attendees = doc.getString("attendees") ?: "",
                                agenda = doc.getString("agenda") ?: "",
                                discussion = doc.getString("discussion") ?: "",
                                decisions = doc.getString("decisions") ?: "",
                                summary = doc.getString("summary") ?: ""
                            )
                        } catch (e: Exception) { null }
                    }
                    if (firebaseMinutes.isNotEmpty()) _minutes.value = firebaseMinutes
                }
            }

            db.collection("events").addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    val firebaseEvents = snapshot.documents.mapNotNull { doc ->
                        try {
                            Event(
                                id = doc.getLong("id")?.toInt() ?: 0,
                                title = doc.getString("title") ?: "",
                                date = doc.getLong("date") ?: 0L,
                                status = EventStatus.values().find { it.name == doc.getString("status") } ?: EventStatus.Pending,
                                assignedTo = doc.getString("assignedTo") ?: ""
                            )
                        } catch (e: Exception) { null }
                    }
                    if (firebaseEvents.isNotEmpty()) _events.value = firebaseEvents
                }
            }
            
            db.collection("notices").addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    val firebaseNotices = snapshot.documents.mapNotNull { doc ->
                        try {
                            Notice(
                                id = doc.getLong("id")?.toInt() ?: 0,
                                title = doc.getString("title") ?: "",
                                content = doc.getString("content") ?: "",
                                date = doc.getLong("date") ?: 0L,
                                isPinned = doc.getBoolean("isPinned") ?: false
                            )
                        } catch (e: Exception) { null }
                    }
                    if (firebaseNotices.isNotEmpty()) _notices.value = firebaseNotices
                }
            }

            db.collection("suggestions").addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    val firebaseSuggestions = snapshot.documents.mapNotNull { doc ->
                        try {
                            Suggestion(
                                id = doc.id,
                                text = doc.getString("text") ?: "",
                                author = doc.getString("author") ?: "",
                                date = doc.getLong("date") ?: 0L,
                                upvotes = doc.getLong("upvotes")?.toInt() ?: 0
                            )
                        } catch (e: Exception) { null }
                    }
                    _suggestions.value = firebaseSuggestions.sortedByDescending { it.upvotes }
                }
            }

            db.collection("photos").addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    val firebasePhotos = snapshot.documents.mapNotNull { doc ->
                        try {
                            PhotoAlbumUrl(
                                id = doc.id,
                                url = doc.getString("url") ?: "",
                                caption = doc.getString("caption") ?: "",
                                uploadedBy = doc.getString("uploadedBy") ?: "",
                                date = doc.getLong("date") ?: 0L
                            )
                        } catch (e: Exception) { null }
                    }
                    _photos.value = firebasePhotos.sortedByDescending { it.date }
                }
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private val _inviteCodes = MutableStateFlow<List<Pair<String, Role>>>(listOf())
    val inviteCodes: StateFlow<List<Pair<String, Role>>> = _inviteCodes.asStateFlow()

    fun fetchInviteCodes() {
        viewModelScope.launch {
            try {
                val db = FirebaseFirestore.getInstance()
                val snapshot = db.collection("inviteCodes").get().await()
                val codes = snapshot.documents.mapNotNull { doc ->
                    val roleString = doc.getString("role") ?: return@mapNotNull null
                    val role = Role.values().find { it.name.equals(roleString, ignoreCase = true) } ?: return@mapNotNull null
                    Pair(doc.id, role)
                }
                if (codes.isNotEmpty()) {
                    _inviteCodes.value = codes
                }
            } catch (e: Exception) {
                e.printStackTrace()
                if (_inviteCodes.value.isEmpty()) {
                    _inviteCodes.value = listOf(
                        Pair("ADMIN123", Role.Admin),
                        Pair("SEC456", Role.Sachiv),
                        Pair("MEMBER123", Role.Sadasya)
                    )
                }
            }
        }
    }

    fun addInviteCode(code: String, role: Role) {
        viewModelScope.launch {
            try {
                val db = FirebaseFirestore.getInstance()
                val data = hashMapOf("role" to role.name)
                db.collection("inviteCodes").document(code).set(data).await()
                fetchInviteCodes()
            } catch (e: Exception) {
                e.printStackTrace()
                _inviteCodes.value = _inviteCodes.value + Pair(code, role)
            }
        }
    }

    fun deleteInviteCode(code: String) {
        viewModelScope.launch {
            try {
                val db = FirebaseFirestore.getInstance()
                db.collection("inviteCodes").document(code).delete().await()
                fetchInviteCodes()
            } catch (e: Exception) {
                e.printStackTrace()
                _inviteCodes.value = _inviteCodes.value.filter { it.first != code }
            }
        }
    }

    fun addUser(name: String, role: Role) {
        viewModelScope.launch {
            val user = User(name = name, role = role)
            repository.insertUser(user)
            try {
                val db = FirebaseFirestore.getInstance()
                val data = hashMapOf(
                    "id" to System.currentTimeMillis(),
                    "name" to name,
                    "role" to role.name,
                    "isActive" to true
                )
                db.collection("users").add(data).await()
            } catch (e: Exception) { e.printStackTrace() }
        }
    }
    
    fun deleteUser(user: User) {
        viewModelScope.launch {
            repository.deleteUser(user)
            try {
                // Approximate delete by name, since we might not have the correct Firebase document ID
                val db = FirebaseFirestore.getInstance()
                val snapshot = db.collection("users").whereEqualTo("name", user.name).get().await()
                for (doc in snapshot.documents) {
                    doc.reference.delete().await()
                }
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    fun addMinute(minute: Minute) {
        viewModelScope.launch {
            repository.insertMinute(minute)
            try {
                val db = FirebaseFirestore.getInstance()
                val data = hashMapOf(
                    "id" to System.currentTimeMillis(),
                    "template" to minute.template.name,
                    "title" to minute.title,
                    "date" to minute.date,
                    "chairperson" to minute.chairperson,
                    "attendees" to minute.attendees,
                    "agenda" to minute.agenda,
                    "discussion" to minute.discussion,
                    "decisions" to minute.decisions,
                    "summary" to minute.summary
                )
                db.collection("minutes").add(data).await()
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    fun addEvent(title: String, status: EventStatus, assignedTo: String) {
        viewModelScope.launch {
            val date = System.currentTimeMillis()
            repository.insertEvent(Event(title = title, date = date, status = status, assignedTo = assignedTo))
            try {
                val db = FirebaseFirestore.getInstance()
                val data = hashMapOf(
                    "id" to System.currentTimeMillis(),
                    "title" to title,
                    "date" to date,
                    "status" to status.name,
                    "assignedTo" to assignedTo
                )
                db.collection("events").add(data).await()
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    fun addNotice(title: String, content: String, isPinned: Boolean) {
        viewModelScope.launch {
            val date = System.currentTimeMillis()
            repository.insertNotice(Notice(title = title, content = content, date = date, isPinned = isPinned))
            try {
                val db = FirebaseFirestore.getInstance()
                val data = hashMapOf(
                    "id" to System.currentTimeMillis(),
                    "title" to title,
                    "content" to content,
                    "date" to date,
                    "isPinned" to isPinned
                )
                db.collection("notices").add(data).await()
            } catch (e: Exception) { e.printStackTrace() }
        }
    }
    
    fun deleteNotice(notice: Notice) {
        viewModelScope.launch {
            try {
                // Delete from Room is not implemented for Notice in DAO but let's add try-catch just in case or just firebase delete
                val db = FirebaseFirestore.getInstance()
                val snapshot = db.collection("notices").whereEqualTo("title", notice.title).whereEqualTo("date", notice.date).get().await()
                for (doc in snapshot.documents) {
                    doc.reference.delete().await()
                }
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    fun addSuggestion(text: String, author: String) {
        viewModelScope.launch {
            try {
                val db = FirebaseFirestore.getInstance()
                val data = hashMapOf(
                    "text" to text,
                    "author" to author,
                    "date" to System.currentTimeMillis(),
                    "upvotes" to 0
                )
                db.collection("suggestions").add(data).await()
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    fun upvoteSuggestion(id: String, currentUpvotes: Int) {
        viewModelScope.launch {
            try {
                val db = FirebaseFirestore.getInstance()
                db.collection("suggestions").document(id).update("upvotes", currentUpvotes + 1).await()
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    fun addPhotoUrl(url: String, caption: String, uploadedBy: String) {
        viewModelScope.launch {
            try {
                val db = FirebaseFirestore.getInstance()
                val data = hashMapOf(
                    "url" to url,
                    "caption" to caption,
                    "uploadedBy" to uploadedBy,
                    "date" to System.currentTimeMillis()
                )
                db.collection("photos").add(data).await()
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    // Seed some initial data
    fun seedData() {
        viewModelScope.launch {
            repository.insertUser(User(name = "Alice (Admin)", role = Role.Sachiv))
            repository.insertUser(User(name = "Bob", role = Role.Adhyakshya))
            repository.insertNotice(Notice(title = "Welcome", content = "Welcome to the Digital Bal Club system.", date = System.currentTimeMillis(), isPinned = true))
        }
    }
}
