package com.example.livebus

import android.annotation.SuppressLint
import android.util.Log
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import ua.naiksoftware.stomp.Stomp
import ua.naiksoftware.stomp.StompClient

class WebSocketClient {
    private var stompClient: StompClient? = null
    private val compositeDisposable = CompositeDisposable()

    @SuppressLint("CheckResult")
    fun connect(url: String) {
        try {
            stompClient = Stomp.over(Stomp.ConnectionProvider.OKHTTP, url).apply {
                withClientHeartbeat(10000)
                withServerHeartbeat(10000)
            }

            val lifecycleDisposable = stompClient?.lifecycle()
                ?.subscribeOn(Schedulers.io())
                ?.observeOn(AndroidSchedulers.mainThread())
                ?.subscribe({ lifecycleEvent ->
                    when (lifecycleEvent.type) {
                        ua.naiksoftware.stomp.dto.LifecycleEvent.Type.OPENED -> {
                            Log.d("WebSocketClient", "STOMP connection opened")
                        }
                        ua.naiksoftware.stomp.dto.LifecycleEvent.Type.ERROR -> {
                            Log.e("WebSocketClient", "STOMP connection error", lifecycleEvent.exception)
                        }
                        ua.naiksoftware.stomp.dto.LifecycleEvent.Type.CLOSED -> {
                            Log.d("WebSocketClient", "STOMP connection closed")
                        }
                        else -> {}
                    }
                }, { error ->
                    Log.e("WebSocketClient", "Lifecycle subscribe error", error)
                })

            if (lifecycleDisposable != null) {
                compositeDisposable.add(lifecycleDisposable)
            }

            stompClient?.connect()
        } catch (e: Exception) {
            Log.e("WebSocketClient", "Failed to connect to STOMP server", e)
        }
    }

    @SuppressLint("CheckResult")
    fun subscribeToTopic(topic: String, onMessageReceived: (String) -> Unit) {
        val topicDisposable = stompClient?.topic(topic)
            ?.subscribeOn(Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe({ topicMessage ->
                onMessageReceived(topicMessage.payload)
            }, { error ->
                Log.e("WebSocketClient", "Error subscribing to topic $topic", error)
            })

        if (topicDisposable != null) {
            compositeDisposable.add(topicDisposable)
        }
    }

    fun disconnect() {
        compositeDisposable.clear()
        stompClient?.disconnect()
        stompClient = null
    }
}
