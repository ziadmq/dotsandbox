package com.ziadmq.dotsandbox.view

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
    var selectedMode by remember { mutableStateOf(GameMode.PvE) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp).padding(bottom = 60.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_logo_neon),
                contentDescription = "Logo",
                modifier = Modifier.size(180.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(stringResource(R.string.app_name).uppercase(), fontSize = 36.sp, fontWeight = FontWeight.Black, color = Color.White)
            Text(stringResource(R.string.cyber_edition), fontSize = 14.sp, color = Player1Color, letterSpacing = 8.sp)

            Spacer(modifier = Modifier.height(48.dp))

            GlassCard {
                Text(stringResource(R.string.grid_size), color = Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    listOf(3, 4, 5).forEach { size ->
                        SelectableButton(
                            text = "${size}x${size}",
                            isSelected = selectedSize == size,
                            onClick = { selectedSize = size }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                Text(stringResource(R.string.opponent), color = Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    SelectableButton(stringResource(R.string.ai_bot), selectedMode == GameMode.PvE) { selectedMode = GameMode.PvE }
                    SelectableButton(stringResource(R.string.human), selectedMode == GameMode.PvP) { selectedMode = GameMode.PvP }
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            Button(
                onClick = {
                    onShowInterstitial()
                    onStartGame(selectedSize, selectedMode)
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Player1Color),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(stringResource(R.string.start_game), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.Black)
            }
        }

        AdBanner(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 8.dp)
        )
    }
}
@Composable
fun SelectableButton(text: String, isSelected: Boolean, onClick: () -> Unit) {
    val bgColor = if (isSelected) Player1Color else Color.Transparent
    val border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, Color.Gray)
    val textColor = if (isSelected) Color.Black else Color.White

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = bgColor,
        border = border,
        modifier = Modifier.height(45.dp).width(100.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(text, color = textColor, fontWeight = FontWeight.Bold)
        }
    }
}
