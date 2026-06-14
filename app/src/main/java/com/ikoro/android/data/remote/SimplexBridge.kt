package com.ikoro.android.data.remote

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import timber.log.Timber

interface SimplexEventListener {
    fun onEvent(event: String)
}

class SimplexBridge {
    private val _events = MutableStateFlow("")
    val events: StateFlow<String> = _events

    private var listener: SimplexEventListener? = null

    fun setListener(l: SimplexEventListener?) {
        listener = l
    }

    fun connect(serverUri: String) {
        Timber.i("Connecting to SimpleX server: %s", serverUri)
        _events.value = "connected"
    }

    fun disconnect() {
        Timber.i("Disconnecting from SimpleX server")
        _events.value = "disconnected"
    }

    fun createProfile(displayName: String) {
        Timber.i("Creating profile: %s", displayName)
        emit("profile_created:$displayName")
    }

    fun startChat() {
        Timber.i("Starting chat listener")
        emit("chat_started")
    }

    fun sendMessage(contact: String, text: String) {
        Timber.i("Sending message to %s", contact)
        emit("sent:$contact:$text")
    }

    fun createGroup(groupName: String) {
        Timber.i("Creating group: %s", groupName)
        emit("group_created:$groupName")
    }

    fun joinGroup(groupLink: String) {
        Timber.i("Joining group: %s", groupLink)
        emit("group_joined:$groupLink")
    }

    fun sendFile(contact: String, filePath: String) {
        Timber.i("Sending file %s to %s", filePath, contact)
        emit("file_sent:$contact:$filePath")
    }

    fun acceptInvitation(connReq: String) {
        Timber.i("Accepting invitation: %s", connReq)
        emit("invitation_accepted:$connReq")
    }

    private fun emit(event: String) {
        _events.value = event
        listener?.onEvent(event)
    }
}
