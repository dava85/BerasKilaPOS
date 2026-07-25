package com.example.neotokopos85.utils

data class AppUpdateState(

    val updateAvailable: Boolean = false,

    val apkUrl: String = "",

    val message: String = "",

    val downloading: Boolean = false,

    val progress: Int = 0
)