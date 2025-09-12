package ru.dsaime.npchat

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import ru.dsaime.npchat.di.koin.appModule
import ru.dsaime.npchat.screens.app.ComposeApp
import ru.dsaime.npchat.ui.theme.NPChatTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        installSplashScreen()

        val koinApp =
            startKoin {
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
