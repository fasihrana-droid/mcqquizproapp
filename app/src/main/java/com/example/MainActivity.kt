package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.AppTab
import com.example.ui.McqViewModel
import com.example.ui.components.McqBottomNavBar
import com.example.ui.components.McqHeader
import com.example.ui.components.McqNavigationRail
import com.example.ui.screens.AnalyticsScreen
import com.example.ui.screens.QuestionBankScreen
import com.example.ui.screens.QuizScreen
import com.example.ui.screens.ResultsScreen
import com.example.ui.screens.SavedScreen
import com.example.ui.screens.TestStudioScreen
import com.example.ui.screens.UploadScreen
import com.example.ui.theme.LocalMcqColors
import com.example.ui.theme.McqQuizTheme
import kotlinx.coroutines.flow.collectLatest

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge(
      statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
      navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
    )
    setContent {
      McqQuizTheme {
        McqQuizApp()
      }
    }
  }
}

@Composable
fun McqQuizApp(viewModel: McqViewModel = viewModel()) {
  val state by viewModel.uiState.collectAsStateWithLifecycle()
  val colors = LocalMcqColors.current
  val snackbarHostState = remember { SnackbarHostState() }

  LaunchedEffect(viewModel) {
    viewModel.messages.collectLatest { message ->
      snackbarHostState.showSnackbar(message)
    }
  }

  val isQuizActive = state.activeQuiz != null

  BoxWithConstraints(
    modifier = Modifier
      .fillMaxSize()
      .background(colors.bg),
  ) {
    val useNavRail = maxWidth >= 720.dp

    Row(modifier = Modifier.fillMaxSize()) {
      if (useNavRail && !isQuizActive) {
        McqNavigationRail(
          currentTab = state.currentTab,
          isQuizActive = false,
          onSelectTab = { viewModel.selectTab(it) },
        )
      }

      Scaffold(
        modifier = Modifier.weight(1f),
        containerColor = Color.Transparent,
        snackbarHost = {
          SnackbarHost(hostState = snackbarHostState) { data ->
            Snackbar(
              snackbarData = data,
              containerColor = colors.card,
              contentColor = colors.textPrimary,
              actionColor = colors.emeraldLight,
            )
          }
        },
        topBar = {
          Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.TopCenter,
          ) {
            McqHeader(modifier = Modifier.widthIn(max = 600.dp))
          }
        },
        bottomBar = {
          if (!useNavRail && !isQuizActive) {
            Box(
              modifier = Modifier.fillMaxWidth(),
              contentAlignment = Alignment.BottomCenter,
            ) {
              McqBottomNavBar(
                currentTab = state.currentTab,
                isQuizActive = false,
                onSelectTab = { viewModel.selectTab(it) },
                modifier = Modifier.widthIn(max = 600.dp),
              )
            }
          }
        },
      ) { innerPadding ->
        Box(
          modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding),
          contentAlignment = Alignment.TopCenter,
        ) {
          Column(
            modifier = Modifier
              .fillMaxSize()
              .widthIn(max = 600.dp),
          ) {
            val activeQuiz = state.activeQuiz
            val quizResult = state.quizResult
            when {
              activeQuiz != null -> {
                QuizScreen(
                  quiz = activeQuiz,
                  bank = state.bank,
                  savedIds = state.savedIds,
                  viewModel = viewModel,
                )
              }
              quizResult != null -> {
                ResultsScreen(
                  result = quizResult,
                  bank = state.bank,
                  savedIds = state.savedIds,
                  viewModel = viewModel,
                )
              }
              else -> {
                when (state.currentTab) {
                  AppTab.STUDIO -> TestStudioScreen(state = state, viewModel = viewModel)
                  AppTab.BANK -> QuestionBankScreen(state = state, viewModel = viewModel)
                  AppTab.SAVED -> SavedScreen(state = state, viewModel = viewModel)
                  AppTab.UPLOAD -> UploadScreen(viewModel = viewModel)
                  AppTab.STATS -> AnalyticsScreen(state = state, viewModel = viewModel)
                }
              }
            }
          }
        }
      }
    }
  }
}
