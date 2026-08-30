package com.helloandroid.security

import android.os.Bundle
import androidx.activity.ComponentActivity

class SafeInternalActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        finish()
    }
}
