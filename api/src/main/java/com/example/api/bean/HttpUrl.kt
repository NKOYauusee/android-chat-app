package com.example.api.bean

object HttpUrl {
    private const val IP = "10.202.56.181"
    const val URL = "http://$IP:8080/nko-api/android/"
    const val WS_URL = "ws://$IP:8080/nko-api/android/websocket"

    // http://localhost:8080/nko-api/android/images/2/ai.png
    const val IMG_URL = "${URL}images/"

    // json web token
}