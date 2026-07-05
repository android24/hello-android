package com.helloandroid.binder

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.Message
import android.os.Messenger
import android.os.RemoteException
import android.os.SystemClock
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

class MainActivity : ComponentActivity() {
    private val viewModel: BinderLabViewModel by viewModels()
    private val replyMessenger = Messenger(ReplyHandler())
    private var remoteMessenger: Messenger? = null
    private var bound = false

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            if (service == null) {
                viewModel.onRemoteError("远程 Service 返回了空 Binder。")
                return
            }
            remoteMessenger = Messenger(service)
            bound = true
            viewModel.onRemoteServiceBound()
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            remoteMessenger = null
            bound = false
            viewModel.onRemoteServiceDisconnected()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val app = application as BinderLabApplication
        viewModel.recordProcessSnapshot(
            processAgeMs = SystemClock.elapsedRealtime() - app.processCreateElapsedRealtime,
            packageName = packageName
        )

        setContent {
            MaterialTheme(
                colorScheme = lightColorScheme(
                    primary = Color(0xFF146C78),
                    secondary = Color(0xFF8A5A28),
                    tertiary = Color(0xFF525E87),
                    surface = Color.White,
                    background = Color(0xFFF7F8FA)
                )
            ) {
                BinderLabRoute(
                    viewModel = viewModel,
                    onBindRemoteService = ::bindRemoteService,
                    onUnbindRemoteService = ::unbindRemoteService,
                    onSendBinderMessage = ::sendBinderMessage
                )
            }
        }
    }

    override fun onDestroy() {
        if (bound) {
            unbindService(connection)
            bound = false
        }
        super.onDestroy()
    }

    private fun bindRemoteService() {
        if (bound) {
            viewModel.onRemoteError("远程 Service 已经绑定，无需重复绑定。")
            return
        }
        viewModel.onBindRequested()
        val intent = Intent(this, RemoteEchoService::class.java)
        bindService(intent, connection, Context.BIND_AUTO_CREATE)
    }

    private fun unbindRemoteService() {
        if (!bound) {
            viewModel.onRemoteError("远程 Service 尚未绑定，无法解绑。")
            return
        }
        unbindService(connection)
        remoteMessenger = null
        bound = false
        viewModel.onUnbindRequested()
    }

    private fun sendBinderMessage() {
        val messenger = remoteMessenger
        if (!bound || messenger == null) {
            viewModel.onRemoteError("请先绑定远程 Service，再发送 Binder 消息。")
            return
        }

        val requestId = viewModel.nextRequestId()
        val payload = "Hello Binder #$requestId"
        val sentAt = SystemClock.uptimeMillis()
        val message = Message.obtain(null, RemoteEchoService.MSG_ECHO_REQUEST).apply {
            replyTo = replyMessenger
            data = Bundle().apply {
                putInt(RemoteEchoService.KEY_REQUEST_ID, requestId)
                putString(RemoteEchoService.KEY_PAYLOAD, payload)
                putLong(RemoteEchoService.KEY_SENT_AT, sentAt)
            }
        }

        try {
            messenger.send(message)
            viewModel.onMessageSent(requestId = requestId, payload = payload)
        } catch (exception: RemoteException) {
            viewModel.onRemoteError("发送失败：${exception.message.orEmpty()}")
        }
    }

    private inner class ReplyHandler : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            if (msg.what != RemoteEchoService.MSG_ECHO_REPLY) {
                super.handleMessage(msg)
                return
            }

            viewModel.onRemoteReply(
                requestId = msg.data.getInt(RemoteEchoService.KEY_REQUEST_ID),
                payload = msg.data.getString(RemoteEchoService.KEY_PAYLOAD).orEmpty(),
                remotePid = msg.data.getInt(RemoteEchoService.KEY_REMOTE_PID),
                remoteThread = msg.data.getString(RemoteEchoService.KEY_REMOTE_THREAD).orEmpty(),
                sentAt = msg.data.getLong(RemoteEchoService.KEY_SENT_AT),
                handledAt = msg.data.getLong(RemoteEchoService.KEY_HANDLED_AT)
            )
        }
    }
}
