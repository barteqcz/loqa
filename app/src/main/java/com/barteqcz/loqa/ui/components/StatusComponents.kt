package com.barteqcz.loqa.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.barteqcz.loqa.R
import com.barteqcz.loqa.ui.theme.CardBackground
import com.barteqcz.loqa.ui.theme.TextGrey

@Composable
fun ServerNapContainer(
    modifier: Modifier = Modifier,
    onRetry: (() -> Unit)? = null,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp, vertical = 64.dp),
        contentAlignment = Alignment.TopCenter,
    ) {
        Surface(
            color = CardBackground.copy(alpha = 0.5f),
            shape = RoundedCornerShape(24.dp),
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                MaterialTheme.colorScheme.error.copy(alpha = 0.2f)
            )
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 32.dp, vertical = 40.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(
                    Icons.Default.CloudOff,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            MaterialTheme.colorScheme.error.copy(alpha = 0.1f),
                            CircleShape
                        )
                        .padding(12.dp)
                )

                Text(
                    text = stringResource(R.string.error_server_nap),
                    color = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    lineHeight = 24.sp
                )

                onRetry?.let {
                    Button(
                        onClick = it,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.1f),
                            contentColor = MaterialTheme.colorScheme.error,
                        ),
                        shape = RoundedCornerShape(12.dp),
                    ) {
                        Text(stringResource(R.string.retry), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun StatusContainer(
    message: String,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
    onRetry: (() -> Unit)? = null,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp, vertical = 64.dp),
        contentAlignment = Alignment.TopCenter,
    ) {
        Surface(
            color = CardBackground.copy(alpha = 0.5f),
            shape = RoundedCornerShape(24.dp),
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                if (isError) MaterialTheme.colorScheme.error.copy(alpha = 0.2f)
                else Color.White.copy(alpha = 0.05f),
            )
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 32.dp, vertical = 40.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (isError) {
                    Icon(
                        Icons.Default.WifiOff,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier
                            .size(48.dp)
                            .background(
                                MaterialTheme.colorScheme.error.copy(alpha = 0.1f),
                                CircleShape
                            )
                            .padding(12.dp)
                    )
                }

                Text(
                    text = message,
                    color = if (isError) MaterialTheme.colorScheme.error.copy(alpha = 0.8f) else TextGrey,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    lineHeight = 24.sp
                )

                onRetry?.let {
                    Button(
                        onClick = it,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            contentColor = MaterialTheme.colorScheme.primary,
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(stringResource(R.string.retry), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
