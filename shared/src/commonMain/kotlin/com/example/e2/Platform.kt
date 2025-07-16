package com.example.e2

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform