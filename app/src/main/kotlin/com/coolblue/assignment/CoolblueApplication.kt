package com.coolblue.assignment

import android.app.Application
import coil3.Extras
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import coil3.network.NetworkHeaders
import coil3.network.httpHeaders
import coil3.network.okhttp.OkHttpNetworkFetcherFactory
import dagger.hilt.android.HiltAndroidApp
import okhttp3.OkHttpClient
import javax.inject.Inject

@HiltAndroidApp
class CoolblueApplication : Application(), SingletonImageLoader.Factory {

    @Inject
    lateinit var okHttpClient: OkHttpClient

    /** Product images go through the same OkHttp client as the API: one connection pool, one set of timeouts. */
    override fun newImageLoader(context: PlatformContext): ImageLoader = ImageLoader.Builder(context)
        .components { add(OkHttpNetworkFetcherFactory(callFactory = okHttpClient)) }
        .apply { extras[Extras.Key.httpHeaders] = AskForWebp }
        .build()
}

// The CDN sends JPEG unless asked; WebP is about half the bytes. Not AVIF: Android decodes it only from API 31.
private val AskForWebp = NetworkHeaders.Builder().set("Accept", "image/webp,image/*").build()
