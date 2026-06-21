package com.helloandroid.async.ui

sealed interface SyncEvent {
    data class ShowMessage(val text: String) : SyncEvent
}
