package com.example

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import com.example.data.AppDatabase
import com.example.data.AppRepository
import com.example.ui.AuraViewModel
import com.example.ui.AuraViewModelFactory
import com.example.ui.MainScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
  private var isActivityInForeground = false
  private lateinit var viewModel: AuraViewModel

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    // 1. Initialize local Room database and Repository
    val database = AppDatabase.getDatabase(applicationContext)
    val repository = AppRepository(database)

    // 2. Initialize the view model using the custom factory
    val factory = AuraViewModelFactory(application, repository)
    viewModel = ViewModelProvider(this, factory)[AuraViewModel::class.java]

    // Active screen locking enforcement loop: relaunch app if minimized during active focus mode
    lifecycleScope.launch {
      while (true) {
        kotlinx.coroutines.delay(500)
        if (::viewModel.isInitialized && viewModel.isFocusModeActive.value && !isActivityInForeground) {
          val relaunchIntent = android.content.Intent(this@MainActivity, MainActivity::class.java).apply {
            addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_REORDER_TO_FRONT or android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP)
          }
          startActivity(relaunchIntent)
        }
      }
    }

    // Natively block home and recents buttons via Android Lock Task (Screen Pinning)
    lifecycleScope.launch {
      viewModel.isFocusModeActive.collect { isActive ->
        if (isActive) {
          try {
            startLockTask()
            android.util.Log.d("MainActivity", "Successfully requested startLockTask")
          } catch (e: Exception) {
            android.util.Log.e("MainActivity", "Failed to start LockTask mode: ${e.message}", e)
          }
        } else {
          try {
            stopLockTask()
            android.util.Log.d("MainActivity", "Successfully requested stopLockTask")
          } catch (e: Exception) {
            android.util.Log.e("MainActivity", "Failed to stop LockTask mode: ${e.message}", e)
          }
        }
      }
    }

    setContent {
      MyApplicationTheme {
        // Dynamic permission requesting for local notification reminders (Android 13+)
        val permissionLauncher = rememberLauncherForActivityResult(
          contract = ActivityResultContracts.RequestPermission()
        ) { /* no-op */ }

        LaunchedEffect(Unit) {
          if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
          }
        }

        MainScreen(
          viewModel = viewModel,
          modifier = Modifier.fillMaxSize()
        )
      }
    }
  }

  override fun onResume() {
    super.onResume()
    isActivityInForeground = true
  }

  override fun onPause() {
    super.onPause()
    isActivityInForeground = false
    // Promptly return to lock screen overlay if focus mode is active
    if (::viewModel.isInitialized && viewModel.isFocusModeActive.value) {
      val relaunchIntent = android.content.Intent(this, MainActivity::class.java).apply {
        addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_REORDER_TO_FRONT or android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP)
      }
      startActivity(relaunchIntent)
    }
  }

  override fun onUserLeaveHint() {
    super.onUserLeaveHint()
    if (::viewModel.isInitialized && viewModel.isFocusModeActive.value) {
      val relaunchIntent = android.content.Intent(this, MainActivity::class.java).apply {
        addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_REORDER_TO_FRONT or android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP)
      }
      startActivity(relaunchIntent)
    }
  }
}
