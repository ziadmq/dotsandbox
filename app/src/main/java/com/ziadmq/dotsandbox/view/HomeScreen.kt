package com.ziadmq.dotsandbox.view

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ziadmq.dotsandbox.AdBanner
import com.ziadmq.dotsandbox.R
import com.ziadmq.dotsandbox.model.GameMode
import com.ziadmq.dotsandbox.ui.theme.Player1Color

@Composable
fun HomeScreen(onShowInterstitial: () -> Unit, onStartGame: (Int, GameMode) -> Unit) {
    var selectedSize by remember { mutableStateOf(4) }
    var isHackerMode by remember { mutableStateOf(false) }
    var isAgainstAi by remember { mutableStateOf(true) }

    val selectedMode = when {
        isHackerMode && isAgainstAi -> GameMode.HACKER_PvE
        isHackerMode && !isAgainstAi -> GameMode.HACKER_PvP
        !isHackerMode && isAgainstAi -> GameMode.PvE
        else -> GameMode.PvP
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_logo_neon),
                contentDescription = "Logo",
                modifier = Modifier.size(180.dp)
            )
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    stringResource(R.string.app_name).uppercase(),
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    letterSpacing = 2.sp
                )
                Text(
                    stringResource(R.string.cyber_edition),
                    fontSize = 12.sp,
                    color = Player1Color,
                    letterSpacing = 6.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            GlassCard(modifier = Modifier.fillMaxWidth()) {
                // حجم الشبكة
                SectionTitle(stringResource(R.string.grid_size))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    listOf(3, 4, 5).forEach { size ->
                        CyberToggleButton(
                            text = "${size}x${size}",
                            isSelected = selectedSize == size,
                            modifier = Modifier.weight(1f).padding(horizontal = 4.dp)
                        ) { selectedSize = size }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                // نمط اللعبة
                SectionTitle(stringResource(R.string.opponent))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    CyberToggleButton(
                        text = stringResource(R.string.classic),
                        icon = Icons.Default.Terminal,
                        isSelected = !isHackerMode,
                        modifier = Modifier.weight(1f).padding(end = 4.dp)
                    ) { isHackerMode = false }
                    CyberToggleButton(
                        text = stringResource(R.string.hacker),
                        icon = Icons.Default.Terminal,
                        isSelected = isHackerMode,
                        modifier = Modifier.weight(1f).padding(start = 4.dp)
                    ) { isHackerMode = true }
                }
                Spacer(modifier = Modifier.height(10.dp))
                // الخصم
                SectionTitle(stringResource(R.string.opponent))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    CyberToggleButton(
                        text = stringResource(R.string.ai_bot),
                        icon = Icons.Default.Computer,
                        isSelected = isAgainstAi,
                        modifier = Modifier.weight(1f).padding(end = 4.dp)
                    ) { isAgainstAi = true }
                    CyberToggleButton(
                        text = stringResource(R.string.human),
                        icon = Icons.Default.Group,
                        isSelected = !isAgainstAi,
                        modifier = Modifier.weight(1f).padding(start = 4.dp)
                    ) { isAgainstAi = false }
                }
            }

            // زر البداية
            Button(
                onClick = {
                    onShowInterstitial()
                    onStartGame(selectedSize, selectedMode)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Player1Color),
                shape = RoundedCornerShape(12.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
            ) {
                Text(
                    stringResource(R.string.start_game).uppercase(),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.Black
                )
            }
        }


        AdBanner(modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 8.dp))
    }
}
@Composable
fun SectionTitle(text: String) {
    Text(
        text = text.uppercase(),
        color = Color.White.copy(alpha = 0.5f),
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(bottom = 12.dp)
    )
}

@Composable
fun CyberToggleButton(
    text: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    onClick: () -> Unit
) {
    val alpha by animateFloatAsState(if (isSelected) 1f else 0.3f, label = "alpha")
    val scale by animateFloatAsState(if (isSelected) 1.05f else 1f, label = "scale")

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        color = if (isSelected) Player1Color.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.05f),
        modifier = modifier.height(48.dp).border(
            width = 1.dp,
            color = if (isSelected) Player1Color else Color.White.copy(alpha = 0.1f),
            shape = RoundedCornerShape(8.dp)
        )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 4.dp)
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = if (isSelected) Player1Color else Color.Gray
                )
                Spacer(modifier = Modifier.width(6.dp))
            }
            Text(
                text = text,
                color = if (isSelected) Player1Color else Color.Gray,
                fontSize = 13.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
            )
        }
    }
}