package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.FinanceDashboard
import com.example.ui.FinanceViewModel
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme(darkTheme = false) { // Force Light High Density Theme for Premium Aesthetic
        val viewModel: FinanceViewModel = viewModel()
        FinanceDashboard(viewModel = viewModel)
      }
    }
  }
}

