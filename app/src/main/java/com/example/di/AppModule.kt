package com.example.di

import com.example.data.local.QuickNestDatabase
import com.example.data.remote.FirestoreService
import com.example.data.repository.AuthRepository
import com.example.data.repository.AuthRepositoryImpl
import com.example.data.repository.PropertyRepository
import com.example.data.repository.PropertyRepositoryImpl
import com.example.domain.usecase.FilterPropertiesUseCase
import com.example.domain.usecase.GetPropertiesUseCase
import com.example.domain.usecase.ObserveUserProfileUseCase
import com.example.domain.usecase.PostListingUseCase
import com.example.domain.usecase.ScheduleVisitUseCase
import com.example.domain.usecase.SendChatMessageUseCase
import com.example.domain.usecase.SmartMatchUseCase
import com.example.domain.usecase.ToggleSavePropertyUseCase
import com.example.viewmodel.AuthViewModel
import com.example.viewmodel.ExploreViewModel
import com.example.viewmodel.HomeViewModel
import com.example.viewmodel.MainViewModel
import com.example.viewmodel.PostPropertyViewModel
import com.example.viewmodel.ProfileViewModel
import com.example.viewmodel.QuickNestViewModel
import com.example.viewmodel.SavedViewModel
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val databaseModule = module {
    single { QuickNestDatabase.getInstance(androidContext()) }
    single { get<QuickNestDatabase>().propertyDao() }
}

val networkModule = module {
    single { FirestoreService() }
}

val repositoryModule = module {
    single<PropertyRepository> { PropertyRepositoryImpl(get(), get()) }
    single<AuthRepository> { AuthRepositoryImpl() }
}

val useCaseModule = module {
    factory { GetPropertiesUseCase(get()) }
    factory { PostListingUseCase(get(), get()) }
    factory { ScheduleVisitUseCase(get(), get()) }
    factory { SmartMatchUseCase(get()) }
    factory { ToggleSavePropertyUseCase(get()) }
    factory { FilterPropertiesUseCase() }
    factory { ObserveUserProfileUseCase(get()) }
    factory { SendChatMessageUseCase(get(), get()) }
}

val viewModelModule = module {
    // MainViewModel: propertyRepository, authRepository, sendChatMessageUseCase (scheduleVisitUseCase defaulted)
    viewModel { MainViewModel(get(), get(), get()) }
    // AuthViewModel: application + authRepository
    viewModel { AuthViewModel(androidApplication(), get()) }
    // HomeViewModel: propertyRepository, getPropertiesUseCase (rest defaulted from repository)
    viewModel { HomeViewModel(get(), get()) }
    // ExploreViewModel: getPropertiesUseCase, filterPropertiesUseCase (defaulted), toggleSaveUseCase
    viewModel { ExploreViewModel(get(), get(), get()) }
    // PostPropertyViewModel: postListingUseCase, authRepository
    viewModel { PostPropertyViewModel(get(), get()) }
    // SavedViewModel: propertyRepository, getPropertiesUseCase, toggleSaveUseCase
    viewModel { SavedViewModel(get(), get(), get()) }
    // ProfileViewModel: authRepository, propertyRepository
    viewModel { ProfileViewModel(get(), get()) }
    // Legacy — kept for backward compatibility with existing Robolectric CUJ tests
    viewModel { QuickNestViewModel(androidApplication()) }
}

val appModules = listOf(
    databaseModule,
    networkModule,
    repositoryModule,
    useCaseModule,
    viewModelModule
)
