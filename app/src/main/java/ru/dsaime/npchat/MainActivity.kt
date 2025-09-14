package ru.dsaime.npchat

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import org.koin.core.logger.PrintLogger
import ru.dsaime.npchat.di.koin.appModule
import ru.dsaime.npchat.screens.app.ComposeApp
import ru.dsaime.npchat.ui.theme.NPChatTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        installSplashScreen()

        val koinApp =
            startKoin {
                logger(PrintLogger(Level.DEBUG)) // ← добавь это
                androidContext(this@MainActivity)
                modules(appModule)
            }

        setContent {
            NPChatTheme {
                ComposeApp(koinApp.koin)
            }
        }
    }
}
