package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.ui.components.*
import com.example.ui.screens.*
import com.example.ui.theme.RozgarKashmirTheme
import com.example.viewmodel.RozgarViewModel
import com.example.viewmodel.Screen
import kotlinx.coroutines.flow.collectLatest

class MainActivity : ComponentActivity() {

    private val viewModel: RozgarViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            RozgarKashmirTheme {
                val currentScreen by viewModel.currentScreen.collectAsState()
                val snackbarHostState = remember { SnackbarHostState() }
                var showAuthModal by remember { mutableStateOf(false) }
                var showVoiceDialog by remember { mutableStateOf(false) }

                // Listen for Toast/SnackBar events from ViewModel
                LaunchedEffect(Unit) {
                    viewModel.toastEvent.collectLatest { event ->
                        snackbarHostState.showSnackbar(
                            message = event.message,
                            duration = SnackbarDuration.Short
                        )
                    }
                }

                // Hardware & Gesture Back Button Handling
                BackHandler(enabled = currentScreen != Screen.HOME) {
                    viewModel.navigateBack()
                }

                val showTopBar = currentScreen in listOf(
                    Screen.HOME,
                    Screen.FIND_JOBS,
                    Screen.FIND_WORKERS,
                    Screen.MARKETPLACE,
                    Screen.SECOND_HAND,
                    Screen.FOOD,
                    Screen.PROPERTY,
                    Screen.SEASONAL_WORK,
                    Screen.MESSAGES,
                    Screen.MY_PROFILE,
                    Screen.KASHMIRI_LITERATURE
                )

                val showBottomBar = currentScreen in listOf(
                    Screen.HOME,
                    Screen.FIND_JOBS,
                    Screen.FIND_WORKERS,
                    Screen.POST_JOB,
                    Screen.MARKETPLACE,
                    Screen.SECOND_HAND,
                    Screen.FOOD,
                    Screen.PROPERTY,
                    Screen.SEASONAL_WORK,
                    Screen.MESSAGES,
                    Screen.MY_PROFILE,
                    Screen.KASHMIRI_LITERATURE,
                    Screen.SELLER_DASHBOARD,
                    Screen.MY_ORDERS
                )

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
                    topBar = {
                        if (showTopBar) {
                            RozgarTopBar(
                                viewModel = viewModel,
                                onOpenAuth = { viewModel.navigateTo(Screen.AUTH) },
                                onOpenNotifications = { viewModel.navigateTo(Screen.NOTIFICATIONS) },
                                onOpenAdmin = { viewModel.navigateTo(Screen.ADMIN_DASHBOARD) },
                                onOpenDonate = { viewModel.navigateTo(Screen.DONATE) },
                                onOpenVoice = { showVoiceDialog = true }
                            )
                        }
                    },
                    bottomBar = {
                        if (showBottomBar) {
                            RozgarBottomBar(
                                currentScreen = currentScreen,
                                onNavigate = { targetScreen ->
                                    viewModel.navigateTo(targetScreen)
                                }
                            )
                        }
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(
                                top = if (showTopBar) innerPadding.calculateTopPadding() else androidx.compose.ui.unit.Dp(0f),
                                bottom = if (showBottomBar) innerPadding.calculateBottomPadding() else androidx.compose.ui.unit.Dp(0f)
                            )
                    ) {
                        AnimatedContent(
                            targetState = currentScreen,
                            label = "ScreenTransition"
                        ) { screen ->
                            when (screen) {
                                Screen.HOME -> HomeScreen(viewModel = viewModel)
                                Screen.FIND_JOBS -> FindJobsScreen(viewModel = viewModel)
                                Screen.FIND_WORKERS -> FindWorkersScreen(viewModel = viewModel)
                                Screen.POST_JOB -> PostJobScreen(viewModel = viewModel)
                                Screen.MARKETPLACE, Screen.SECOND_HAND -> MarketplaceScreen(viewModel = viewModel)
                                Screen.FOOD -> RestaurantFoodScreen(viewModel = viewModel)
                                Screen.PROPERTY -> PropertyScreen(viewModel = viewModel)
                                Screen.SEASONAL_WORK -> SeasonalWorkScreen(viewModel = viewModel)
                                Screen.MESSAGES -> MessagesScreen(viewModel = viewModel)
                                Screen.CHAT_DETAIL -> ChatDetailScreen(viewModel = viewModel)
                                Screen.WORKER_DETAIL -> WorkerDetailScreen(viewModel = viewModel)
                                Screen.JOB_DETAIL -> JobDetailScreen(viewModel = viewModel)
                                Screen.PRODUCT_DETAIL -> ProductDetailScreen(viewModel = viewModel)
                                Screen.MY_PROFILE, Screen.MY_APPLICATIONS, Screen.MY_LISTINGS, Screen.MY_ORDERS, Screen.PAYOUTS_SETTINGS -> ProfileScreen(
                                    viewModel = viewModel,
                                    onOpenAuth = { viewModel.navigateTo(Screen.AUTH) }
                                )
                                Screen.SELLER_DASHBOARD -> SellerDashboardScreen(viewModel = viewModel)
                                Screen.CREATE_PROFILE -> CreateProfileScreen(viewModel = viewModel)
                                Screen.ADMIN_DASHBOARD -> AdminDashboardScreen(viewModel = viewModel)
                                Screen.DONATE -> DonationScreen(viewModel = viewModel)
                                Screen.NOTIFICATIONS -> NotificationsScreen(viewModel = viewModel)
                                Screen.SAVED_ITEMS -> SavedItemsScreen(viewModel = viewModel)
                                Screen.AUTH -> AuthScreen(viewModel = viewModel)
                                Screen.LOGIN -> LoginScreen(viewModel = viewModel)
                                Screen.KASHMIRI_LITERATURE, Screen.DAILY_KASHMIRI_WORDS -> KashmiriLiteratureScreen(viewModel = viewModel)
                                Screen.POLICIES -> PoliciesAndLegalScreen(viewModel = viewModel)
                            }
                        }
                    }
                }

                // Auth Dialog Modal
                if (showAuthModal) {
                    AuthDialog(
                        viewModel = viewModel,
                        onDismiss = { showAuthModal = false }
                    )
                }

                // Voice Interaction Modal
                if (showVoiceDialog) {
                    VoiceInteractionDialog(
                        viewModel = viewModel,
                        onDismiss = { showVoiceDialog = false }
                    )
                }
            }
        }
    }
}
