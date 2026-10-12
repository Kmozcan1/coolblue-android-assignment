package com.coolblue.assignment.search.data.di

import com.coolblue.assignment.search.data.api.SearchApi
import com.coolblue.assignment.search.data.repository.SearchRepositoryImpl
import com.coolblue.assignment.search.domain.repository.SearchRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.create
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal abstract class SearchDataModule {

    @Binds
    @Singleton
    abstract fun bindSearchRepository(impl: SearchRepositoryImpl): SearchRepository

    companion object {
        @Provides
        @Singleton
        fun provideSearchApi(retrofit: Retrofit): SearchApi = retrofit.create()
    }
}
