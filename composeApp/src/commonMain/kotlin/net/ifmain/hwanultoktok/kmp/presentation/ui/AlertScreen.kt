package net.ifmain.hwanultoktok.kmp.presentation.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.koinInject
import net.ifmain.hwanultoktok.kmp.domain.model.AlertType
import net.ifmain.hwanultoktok.kmp.domain.model.ExchangeRateAlert
import net.ifmain.hwanultoktok.kmp.presentation.viewmodel.AlertViewModel
import net.ifmain.hwanultoktok.kmp.util.format

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlertScreen(
    modifier: Modifier = Modifier,
    viewModel: AlertViewModel = koinInject()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showAddAlertDialog by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxSize()) {
        // Error handling
        uiState.errorMessage?.let { error ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "오류",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                    Text(
                        text = error,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                    Button(
                        onClick = { viewModel.clearError() },
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        Text("확인")
                    }
                }
            }
        }

        // Header with add button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "환율 알림",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            
            FloatingActionButton(
                onClick = { showAddAlertDialog = true },
                modifier = Modifier.size(48.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "알림 추가")
            }
        }

        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            
            uiState.alerts.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "설정된 알림이 없습니다",
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "+ 버튼을 눌러 알림을 추가해보세요",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            }
            
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp)
                ) {
                    items(uiState.alerts) { alert ->
                        AlertCard(
                            alert = alert,
                            onDeleteClick = { viewModel.deleteAlert(alert.id) },
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }

    if (showAddAlertDialog) {
        AddAlertDialog(
            onDismiss = { showAddAlertDialog = false },
            onConfirm = { currencyCode, alertType, targetRate ->
                viewModel.addAlert(currencyCode, alertType, targetRate)
                showAddAlertDialog = false
            }
        )
    }
}

@Composable
fun AlertCard(
    alert: ExchangeRateAlert,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ElevatedCard(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = alert.currencyCode,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                val alertText = when (alert.alertType) {
                    AlertType.ABOVE -> "${alert.targetRate.format(2)} 이상일 때"
                    AlertType.BELOW -> "${alert.targetRate.format(2)} 이하일 때"
                }
                
                Text(
                    text = alertText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
                
                Text(
                    text = if (alert.isEnabled) "활성" else "비활성",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (alert.isEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            
            IconButton(onClick = onDeleteClick) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "삭제",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAlertDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, AlertType, Double) -> Unit
) {
    var selectedCurrency by remember { mutableStateOf("USD") }
    var selectedAlertType by remember { mutableStateOf(AlertType.ABOVE) }
    var targetRateText by remember { mutableStateOf("") }
    
    val currencies = listOf("USD", "EUR", "JPY", "CNH")
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("알림 추가") },
        text = {
            Column {
                Text("통화 선택")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    currencies.forEach { currency ->
                        FilterChip(
                            selected = selectedCurrency == currency,
                            onClick = { selectedCurrency = currency },
                            label = { Text(currency) }
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text("알림 조건")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedAlertType == AlertType.ABOVE,
                        onClick = { selectedAlertType = AlertType.ABOVE },
                        label = { Text("이상") }
                    )
                    FilterChip(
                        selected = selectedAlertType == AlertType.BELOW,
                        onClick = { selectedAlertType = AlertType.BELOW },
                        label = { Text("이하") }
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = targetRateText,
                    onValueChange = { targetRateText = it },
                    label = { Text("목표 환율") },
                    placeholder = { Text("예: 1350.00") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val targetRate = targetRateText.toDoubleOrNull()
                    if (targetRate != null) {
                        onConfirm(selectedCurrency, selectedAlertType, targetRate)
                    }
                },
                enabled = targetRateText.toDoubleOrNull() != null
            ) {
                Text("추가")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("취소")
            }
        }
    )
}