package com.vnpttech.demo.ui.home.conversations

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.vnpttech.demo.constants.LoadingStatus
import com.vnpttech.demo.constants.UpdateType
import com.vnpttech.demo.data.AppContainer
import com.vnpttech.demo.model.Conversation
import com.vnpttech.demo.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class ConversationViewModel(private val container: AppContainer) : ViewModel() {
    private val _uiState = MutableStateFlow(ConversationState(status = LoadingStatus.Loading))
    val uiState: StateFlow<ConversationState> = _uiState.asStateFlow()

    private var _conversations = mutableStateListOf<Conversation>()

    val conversations: List<Conversation>
        get() {
            return _conversations
        }

    suspend fun loadConversation() {
        _uiState.update {
            it.copy(
                status = LoadingStatus.Loading
            )
        }
        try {
            _conversations.clear()
            addListener()
            _uiState.update {
                it.copy(
                    status = LoadingStatus.Loaded
                )
            }
        } catch (e: Exception) {
            _uiState.update {
                it.copy(status = LoadingStatus.Error)
            }
        }
    }

    suspend fun onBack() {
        container.conversations.cancelListener()
    }

    suspend fun getUser(email: String): User {
        return container.users.getUserFromEmail(email)!!
    }

    private suspend fun addListener() {
        container.conversations.conversationsListener { conversation, type ->
            _conversations.removeIf { c ->
                c.id == conversation.id
            }
            _uiState.update {
                when (type) {
                    UpdateType.Deleted -> {}
                    UpdateType.Changed, UpdateType.Added -> {
                        _conversations.add(0, conversation)
                    }
                }
                it.copy(
                    update = type, updatedValue = conversation.id + conversation.latestMessage
                )
            }
        }
    }
}