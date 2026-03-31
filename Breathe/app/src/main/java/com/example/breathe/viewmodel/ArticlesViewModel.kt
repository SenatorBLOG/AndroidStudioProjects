package com.example.breathe.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.breathe.data.api.ApiService
import com.example.breathe.data.models.CreatePostRequest
import com.example.breathe.data.models.PostDto
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

// ── Status ────────────────────────────────────────────────────────────────────

sealed interface PostsStatus {
    data object Loading : PostsStatus
    data object Empty   : PostsStatus
    data object Success : PostsStatus
    data class  Error(val message: String) : PostsStatus
}

// ── UI state ──────────────────────────────────────────────────────────────────

data class PostsUiState(
    val posts:         List<PostDto> = emptyList(),
    val status:        PostsStatus   = PostsStatus.Loading,
    val selectedCat:   String        = "all",
    val currentPage:   Int           = 1,
    val totalPages:    Int           = 1,
    val isLoadingMore: Boolean       = false,
    val isPosting:     Boolean       = false,
)

// ── ViewModel ─────────────────────────────────────────────────────────────────

@HiltViewModel
class PostsViewModel @Inject constructor(
    private val apiService: ApiService,
) : ViewModel() {

    private val _state = MutableStateFlow(PostsUiState())
    val state: StateFlow<PostsUiState> = _state.asStateFlow()

    init { load() }

    fun load() {
        viewModelScope.launch {
            _state.update { it.copy(status = PostsStatus.Loading, currentPage = 1) }
            fetchPage(1, reset = true)
        }
    }

    fun loadMore() {
        val s = _state.value
        if (s.isLoadingMore || s.currentPage >= s.totalPages) return
        viewModelScope.launch {
            _state.update { it.copy(isLoadingMore = true) }
            fetchPage(s.currentPage + 1, reset = false)
        }
    }

    fun setCategory(cat: String) {
        if (_state.value.selectedCat == cat) return
        _state.update { it.copy(selectedCat = cat) }
        load()
    }

    fun toggleLike(postId: String) {
        val original = _state.value.posts.find { it.id == postId } ?: return
        _state.update { s ->
            s.copy(posts = s.posts.map { p ->
                if (p.id == postId) p.copy(
                    likedByMe = !p.likedByMe,
                    likeCount = if (!p.likedByMe) p.likeCount + 1 else p.likeCount - 1,
                ) else p
            })
        }
        viewModelScope.launch {
            runCatching { apiService.togglePostLike(postId) }
                .onSuccess { resp ->
                    if (resp.isSuccessful) {
                        val body = resp.body() ?: return@onSuccess
                        _state.update { s ->
                            s.copy(posts = s.posts.map { p ->
                                if (p.id == postId) p.copy(likedByMe = body.likedByMe, likeCount = body.likeCount)
                                else p
                            })
                        }
                    }
                }
                .onFailure {
                    _state.update { s ->
                        s.copy(posts = s.posts.map { p -> if (p.id == postId) original else p })
                    }
                }
        }
    }

    fun createPost(text: String, category: String) {
        viewModelScope.launch {
            _state.update { it.copy(isPosting = true) }
            runCatching {
                apiService.createPost(CreatePostRequest(text = text.trim(), category = category))
            }
                .onSuccess { resp ->
                    if (resp.isSuccessful) {
                        val post = resp.body()
                        _state.update {
                            it.copy(
                                posts     = listOfNotNull(post) + it.posts,
                                status    = PostsStatus.Success,
                                isPosting = false,
                            )
                        }
                    } else {
                        _state.update { it.copy(isPosting = false) }
                    }
                }
                .onFailure {
                    _state.update { it.copy(isPosting = false) }
                }
        }
    }

    // ── Internal ──────────────────────────────────────────────────────────────

    private suspend fun fetchPage(page: Int, reset: Boolean) {
        val cat = _state.value.selectedCat.takeIf { it != "all" }
        runCatching { apiService.getPosts(category = cat, page = page) }
            .onSuccess { resp ->
                if (resp.isSuccessful) {
                    val body = resp.body()
                    if (body != null) {
                        val merged = if (reset) body.posts else _state.value.posts + body.posts
                        _state.update {
                            it.copy(
                                posts         = merged,
                                status        = if (merged.isEmpty()) PostsStatus.Empty else PostsStatus.Success,
                                currentPage   = body.page,
                                totalPages    = body.pages,
                                isLoadingMore = false,
                            )
                        }
                    }
                } else {
                    _state.update {
                        it.copy(
                            status        = if (reset) PostsStatus.Error("Server error ${resp.code()}") else it.status,
                            isLoadingMore = false,
                        )
                    }
                }
            }
            .onFailure { err ->
                _state.update {
                    it.copy(
                        status        = if (reset) PostsStatus.Error(err.message ?: "Network error") else it.status,
                        isLoadingMore = false,
                    )
                }
            }
    }
}
