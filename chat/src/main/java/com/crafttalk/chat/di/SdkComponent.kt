package com.crafttalk.chat.di

import android.content.Context
import androidx.fragment.app.Fragment
import com.crafttalk.chat.di.modules.*
import com.crafttalk.chat.presentation.ChatView
import dagger.BindsInstance
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(
    modules = [
        NetworkModule::class,
        RepositoryModule::class,
        GsonModule::class,
        VisitorModule::class,
        UuidModule::class,
        SharedPreferencesModule::class,
        DBModule::class,
        ViewModelModule::class
    ]
)
interface SdkComponent {

    @Component.Builder
    interface Builder {
        @BindsInstance fun context(context: Context): Builder
        @BindsInstance fun chatView(view: ChatView): Builder
        @BindsInstance fun parentFragment(parentFragment: Fragment): Builder

        fun visitorModule(visitorModule: VisitorModule): Builder

        fun build(): SdkComponent
    }

    fun inject(chatView: ChatView)
}