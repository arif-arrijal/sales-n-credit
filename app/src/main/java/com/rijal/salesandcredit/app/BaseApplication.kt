package com.rijal.salesandcredit.app

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.rijal.salesandcredit.db.AppDatabase
import com.rijal.salesandcredit.di.appModules
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import timber.log.Timber

class BaseApplication: Application() {
    private val applicationScope = CoroutineScope(SupervisorJob())
    val database by lazy { AppDatabase.getDatabase(this, applicationScope) }

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@BaseApplication)
            modules(appModules)
        }

        Timber.plant(object : Timber.DebugTree() {
            override fun createStackElementTag(element: StackTraceElement): String? {
                return String.format(
                    "%s:%s:%s ",
                    super.createStackElementTag(element),
                    element.methodName,
                    element.lineNumber
                )
            }
        })

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
    }
}