package com.coolblue.assignment

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.coolblue.assignment.core.ui.Dutch
import com.coolblue.assignment.core.ui.theme.CoolblueTheme
import com.coolblue.assignment.search.ui.SearchRoute
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    // Only the language is overridden; plural forms and the libraries' strings then follow Dutch too.
    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(newBase)
        applyOverrideConfiguration(Configuration().apply { setLocale(Dutch) })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CoolblueTheme {
                SearchRoute()
            }
        }
    }
}
