package com.coolblue.assignment

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.coolblue.assignment.core.ui.theme.CoolblueTheme
import com.coolblue.assignment.search.ui.SearchRoute
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CoolblueTheme {
                // AppCompat stores the pick and recreates the screen in that language.
                SearchRoute(onLanguageSelected = { tag -> AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(tag)) })
            }
        }
    }
}
