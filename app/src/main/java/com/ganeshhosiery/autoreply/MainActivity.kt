package com.ganeshhosiery.autoreply

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.ganeshhosiery.autoreply.ui.screens.AppSettingsScreen
import com.ganeshhosiery.autoreply.ui.screens.BackgroundReliabilityGuideScreen
import com.ganeshhosiery.autoreply.ui.screens.CallHistoryScreen
import com.ganeshhosiery.autoreply.ui.screens.DashboardScreen
import com.ganeshhosiery.autoreply.ui.screens.ExcludedContactsScreen
import com.ganeshhosiery.autoreply.ui.screens.MessageSettingsScreen
import com.ganeshhosiery.autoreply.ui.screens.SetupWizardScreen
import com.ganeshhosiery.autoreply.ui.screens.ShopDetailsScreen
import com.ganeshhosiery.autoreply.ui.screens.SystemStatusScreen
import com.ganeshhosiery.autoreply.ui.screens.TestModeScreen
import com.ganeshhosiery.autoreply.ui.screens.WhatsAppSetupScreen
import com.ganeshhosiery.autoreply.ui.theme.GaneshHosieryTheme

private object Routes {
    const val SETUP = "setup"
    const val DASHBOARD = "dashboard"
    const val EXCLUDED = "excluded"
    const val SHOP = "shop"
    const val MESSAGES = "messages"
    const val WHATSAPP = "whatsapp"
    const val HISTORY = "history"
    const val TEST_MODE = "test_mode"
    const val STATUS = "status"
    const val SETTINGS = "settings"
    const val BACKGROUND_GUIDE = "background_guide"
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GaneshHosieryTheme {
                Surface(modifier = Modifier) {
                    AppNavHost()
                }
            }
        }
    }
}

@Composable
private fun AppNavHost() {
    val navController = rememberNavController()
    val app = GaneshApp.from(androidx.compose.ui.platform.LocalContext.current)

    var startDestination by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(Unit) {
        val settings = app.database.settingsDao().get()
        startDestination = if (settings?.setupCompleted == true) Routes.DASHBOARD else Routes.SETUP
    }

    val destination = startDestination ?: return

    NavHost(navController = navController, startDestination = destination) {
        composable(Routes.SETUP) {
            SetupWizardScreen(onFinished = {
                navController.navigate(Routes.DASHBOARD) { popUpTo(Routes.SETUP) { inclusive = true } }
            })
        }
        composable(Routes.DASHBOARD) {
            DashboardScreen(
                onOpenExcluded = { navController.navigate(Routes.EXCLUDED) },
                onOpenShop = { navController.navigate(Routes.SHOP) },
                onOpenMessages = { navController.navigate(Routes.MESSAGES) },
                onOpenWhatsApp = { navController.navigate(Routes.WHATSAPP) },
                onOpenHistory = { navController.navigate(Routes.HISTORY) },
                onOpenTestMode = { navController.navigate(Routes.TEST_MODE) },
                onOpenStatus = { navController.navigate(Routes.STATUS) },
                onOpenSettings = { navController.navigate(Routes.SETTINGS) },
                onOpenBackgroundGuide = { navController.navigate(Routes.BACKGROUND_GUIDE) }
            )
        }
        composable(Routes.EXCLUDED) { ExcludedContactsScreen(onBack = { navController.popBackStack() }) }
        composable(Routes.SHOP) { ShopDetailsScreen(onBack = { navController.popBackStack() }) }
        composable(Routes.MESSAGES) { MessageSettingsScreen(onBack = { navController.popBackStack() }) }
        composable(Routes.WHATSAPP) { WhatsAppSetupScreen(onBack = { navController.popBackStack() }) }
        composable(Routes.HISTORY) { CallHistoryScreen(onBack = { navController.popBackStack() }) }
        composable(Routes.TEST_MODE) { TestModeScreen(onBack = { navController.popBackStack() }) }
        composable(Routes.STATUS) {
            SystemStatusScreen(
                onBack = { navController.popBackStack() },
                onOpenBackgroundGuide = { navController.navigate(Routes.BACKGROUND_GUIDE) }
            )
        }
        composable(Routes.SETTINGS) { AppSettingsScreen(onBack = { navController.popBackStack() }) }
        composable(Routes.BACKGROUND_GUIDE) { BackgroundReliabilityGuideScreen(onBack = { navController.popBackStack() }) }
    }
}
