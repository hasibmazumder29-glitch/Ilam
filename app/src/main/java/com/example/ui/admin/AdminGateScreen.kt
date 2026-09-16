package com.example.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.common.AppLanguage
import com.example.ui.common.Strings
import com.example.ui.theme.*
import com.example.ui.viewmodel.AppDestination
import com.example.ui.viewmodel.IlamViewModel

@Composable
fun AdminGateScreen(
    viewModel: IlamViewModel,
    modifier: Modifier = Modifier
) {
    val language by viewModel.language.collectAsState()
    var pinInput by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isPasswordVisible by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(IlamBlack)
            .statusBarsPadding()
            .padding(24.dp)
            .testTag("admin_gate_screen"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { viewModel.navigateTo(AppDestination.CUSTOMER_HOME) },
                modifier = Modifier.testTag("admin_gate_back_button")
            ) {
                Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = IlamWhite)
            }
            Spacer(modifier = Modifier.weight(1f))
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = IlamSurfaceDark,
                modifier = Modifier.clickable {
                    viewModel.setLanguage(if (language == AppLanguage.EN) AppLanguage.BN else AppLanguage.EN)
                }
            ) {
                Text(
                    text = if (language == AppLanguage.EN) "বাংলা" else "EN",
                    color = IlamGoldLight,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                )
            }
        }

        // Center Content
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(IlamCardDark)
                    .border(1.5.dp, IlamGold, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = IlamGold,
                    modifier = Modifier.size(36.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "ILAM OWNER PORTAL",
                color = IlamWhite,
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Serif,
                letterSpacing = 3.sp
            )

            Text(
                text = if (language == AppLanguage.BN) "স্টোর মালিক ও প্রশাসনিক নিয়ন্ত্রণ কক্ষ" else "Authorized Store Owner Management Console",
                color = IlamTextMuted,
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 6.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // PIN Field
            OutlinedTextField(
                value = pinInput,
                onValueChange = {
                    pinInput = it
                    errorMessage = null
                },
                placeholder = {
                    Text(
                        text = Strings.passcodeInput(language),
                        color = IlamTextMuted,
                        fontSize = 13.sp
                    )
                },
                visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                        Icon(
                            imageVector = if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = null,
                            tint = IlamTextMuted
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("admin_pin_input"),
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = IlamSurfaceDark,
                    unfocusedContainerColor = IlamSurfaceDark,
                    focusedTextColor = IlamWhite,
                    unfocusedTextColor = IlamWhite,
                    focusedBorderColor = IlamGold,
                    unfocusedBorderColor = IlamCardDark
                ),
                singleLine = true
            )

            errorMessage?.let { err ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = err,
                    color = IlamCrimson,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
                    errorMessage = null
                    viewModel.unlockAdminWithVerification(pinInput) { success ->
                        if (!success) {
                            errorMessage = Strings.incorrectPasscode(language)
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = IlamGold,
                    contentColor = IlamBlack
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("admin_unlock_submit_button")
            ) {
                Text(
                    text = Strings.unlockAdmin(language),
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    letterSpacing = 1.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Surface(
                color = IlamSurfaceDark,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = Strings.defaultPinNotice(language),
                    color = IlamGoldLight,
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(10.dp)
                )
            }
        }

        // Footer Note
        Text(
            text = "Role-Based Access Control Protected • ILAM Security Architecture",
            color = IlamTextMuted,
            fontSize = 10.sp,
            textAlign = TextAlign.Center
        )
    }
}
