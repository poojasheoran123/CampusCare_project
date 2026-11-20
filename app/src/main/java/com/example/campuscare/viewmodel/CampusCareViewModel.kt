package com.example.campuscare.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import com.example.campuscare.data.PostRepository
import com.example.campuscare.data.SampleData
import com.example.campuscare.model.Comment
import com.example.campuscare.model.PhotoCategory
import com.example.campuscare.model.PhotoPost
import com.example.campuscare.model.ReportStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.util.UUID

class CampusCareViewModel : ViewModel() {

    private var appContext: Context? = null

    private val _posts = MutableStateFlow<List<PhotoPost>>(SampleData.samplePosts)
    val posts: StateFlow<List<PhotoPost>> = _posts.asStateFlow()

    private val _selectedCategory = MutableStateFlow(PhotoCategory.ALL)
    val selectedCategory: StateFlow<PhotoCategory> = _selectedCategory.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _activeCommentsPostId = MutableStateFlow<String?>(null)
    val activeCommentsPostId: StateFlow<String?> = _activeCommentsPostId.asStateFlow()

    val filteredPosts: StateFlow<List<PhotoPost>> = combine(
        _posts,
        _selectedCategory,
        _searchQuery
    ) { allPosts, category, query ->
        allPosts.filter { post ->
            val matchesCategory = category == PhotoCategory.ALL || post.category == category
            val matchesQuery = query.isBlank() ||
                    post.title.contains(query, ignoreCase = true) ||
                    post.description.contains(query, ignoreCase = true) ||
                    post.location.contains(query, ignoreCase = true)
            matchesCategory && matchesQuery
        }
    }.stateIn(
        scope = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Default),
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SampleData.samplePosts
    )

    fun initContext(context: Context) {
        if (appContext == null) {
            val app = context.applicationContext
            appContext = app
            val loaded = PostRepository.loadPosts(app)
            _posts.value = loaded
        }
    }

    private fun persistPosts() {
        appContext?.let { ctx ->
            PostRepository.savePosts(ctx, _posts.value)
        }
    }

    fun selectCategory(category: PhotoCategory) {
        _selectedCategory.value = category
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun openComments(post: PhotoPost) {
        _activeCommentsPostId.value = post.id
    }

    fun closeComments() {
        _activeCommentsPostId.value = null
    }

    fun getPostById(postId: String): PhotoPost? {
        return _posts.value.find { it.id == postId }
    }

    fun addComment(postId: String, text: String, authorName: String = "You", authorRole: String = "Student") {
        if (text.isBlank()) return
        val newComment = Comment(
            id = UUID.randomUUID().toString(),
            authorName = authorName,
            authorRole = authorRole,
            text = text.trim(),
            timestamp = "Just now",
            likesCount = 0,
            isLiked = false
        )

        _posts.value = _posts.value.map { post ->
            if (post.id == postId) {
                post.copy(comments = post.comments + newComment)
            } else {
                post
            }
        }
        persistPosts()
    }

    fun toggleLikeComment(postId: String, commentId: String) {
        _posts.value = _posts.value.map { post ->
            if (post.id == postId) {
                val updatedComments = post.comments.map { comment ->
                    if (comment.id == commentId) {
                        val newLiked = !comment.isLiked
                        val newCount = if (newLiked) comment.likesCount + 1 else (comment.likesCount - 1).coerceAtLeast(0)
                        comment.copy(isLiked = newLiked, likesCount = newCount)
                    } else {
                        comment
                    }
                }
                post.copy(comments = updatedComments)
            } else {
                post
            }
        }
        persistPosts()
    }

    fun toggleLikePost(postId: String) {
        _posts.value = _posts.value.map { post ->
            if (post.id == postId) {
                val newLiked = !post.isLiked
                val newCount = if (newLiked) post.likesCount + 1 else (post.likesCount - 1).coerceAtLeast(0)
                post.copy(isLiked = newLiked, likesCount = newCount)
            } else {
                post
            }
        }
        persistPosts()
    }

    fun addPost(
        title: String,
        description: String,
        category: PhotoCategory,
        imageUrl: String,
        location: String,
        authorName: String = "You"
    ) {
        val newPost = PhotoPost(
            id = UUID.randomUUID().toString(),
            title = title.trim(),
            description = description.trim(),
            category = category,
            imageUrl = imageUrl.trim(),
            authorName = authorName,
            location = location.ifBlank { "Campus Main Ground" },
            timestamp = "Just now",
            status = ReportStatus.PENDING,
            likesCount = 0,
            isLiked = false,
            comments = emptyList()
        )
        // Add to top of feed
        _posts.value = listOf(newPost) + _posts.value
        persistPosts()
    }

    fun updatePostStatus(postId: String, newStatus: ReportStatus) {
        _posts.value = _posts.value.map { post ->
            if (post.id == postId) {
                post.copy(status = newStatus)
            } else {
                post
            }
        }
        persistPosts()
    }
}
