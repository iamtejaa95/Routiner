package com.example.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CocoaWarm
import com.example.ui.theme.ElegantBronze
import com.example.ui.theme.EspressoBrown
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.VelvetCharcoal

// ==========================================
// LIQUID GLASS MONOCHROME BACKGROUND
// ==========================================

@Composable
fun LiquidGlassBackground(
    modifier: Modifier = Modifier,
    darkTheme: Boolean = true // Elegant Obsidian by default
) {
    // Rich monochrome shades of black and espresso brown
    val baseColor = if (darkTheme) ObsidianBlack else Color(0xFFFAF7F5)
    val bubbleColor1 = if (darkTheme) Color(0xFF2A1C12) else Color(0xFFEFE6DF) // Molten espresso
    val bubbleColor2 = if (darkTheme) Color(0xFF1E140C) else Color(0xFFF3ECE6) // Deep warm cocoa
    val bubbleColor3 = if (darkTheme) Color(0xFF352214) else Color(0xFFE8DAD0) // Rich bronze-gold aura

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(baseColor)
            .drawBehind {
                val width = size.width
                val height = size.height

                // Top left slow-molten espresso flow
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(bubbleColor1, Color.Transparent),
                        center = Offset(width * 0.15f, height * 0.2f),
                        radius = width * 0.8f
                    ),
                    radius = width * 0.8f,
                    center = Offset(width * 0.15f, height * 0.2f)
                )

                // Bottom right bronze-gold glow
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(bubbleColor3, Color.Transparent),
                        center = Offset(width * 0.85f, height * 0.85f),
                        radius = width * 0.9f
                    ),
                    radius = width * 0.9f,
                    center = Offset(width * 0.85f, height * 0.85f)
                )

                // Mid-screen faint obsidian cloud
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(bubbleColor2, Color.Transparent),
                        center = Offset(width * 0.05f, height * 0.6f),
                        radius = width * 0.6f
                    ),
                    radius = width * 0.6f,
                    center = Offset(width * 0.05f, height * 0.6f)
                )
            }
    )
}

// ==========================================
// GLASS PANELS / CARDS
// ==========================================

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    darkTheme: Boolean = true,
    testTag: String? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val glassColor = if (darkTheme) {
        Color(0x54181310) // Dark smoky liquid glass
    } else {
        Color(0xB3FFFFFF) // Lighter frosted glass
    }

    val borderGlassColor = if (darkTheme) {
        Color(0x298A6E56) // Subtle metallic bronze border
    } else {
        Color(0x4D8A6E56) // Clearer bronze border
    }

    val finalModifier = modifier
        .testTag(testTag ?: "")
        .shadow(
            elevation = 8.dp,
            shape = RoundedCornerShape(24.dp),
            clip = false,
            ambientColor = Color.Black.copy(alpha = 0.05f),
            spotColor = Color.Black.copy(alpha = 0.1f)
        )
        .background(
            color = glassColor,
            shape = RoundedCornerShape(24.dp)
        )
        .border(
            border = BorderStroke(1.dp, borderGlassColor),
            shape = RoundedCornerShape(24.dp)
        )
        .clip(RoundedCornerShape(24.dp))

    val modifierWithClick = if (onClick != null) {
        finalModifier.clickable { onClick() }
    } else {
        finalModifier
    }

    Column(
        modifier = modifierWithClick.padding(20.dp),
        content = content
    )
}

@Composable
fun LiquidGlassCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    darkTheme: Boolean = true,
    testTag: String? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val glassGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0x73201815), // High-opacity dark glass top
            Color(0x3B100B09)  // Low-opacity deep black-brown bottom
        )
    )

    val borderGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0x8CFFFFFF), // Very bright silver specular reflection top
            Color(0x1F8A6E56), // Subtle bronze base
            Color(0x0A000000)  // Fades out at bottom
        )
    )

    val finalModifier = modifier
        .testTag(testTag ?: "")
        .shadow(
            elevation = 16.dp,
            shape = RoundedCornerShape(28.dp),
            clip = false,
            ambientColor = Color.Black.copy(alpha = 0.2f),
            spotColor = Color.Black.copy(alpha = 0.35f)
        )
        .background(
            brush = glassGradient,
            shape = RoundedCornerShape(28.dp)
        )
        .border(
            border = BorderStroke(1.5.dp, borderGradient),
            shape = RoundedCornerShape(28.dp)
        )
        .border(
            border = BorderStroke(0.8.dp, Color(0x2E8A6E56)),
            shape = RoundedCornerShape(28.dp)
        )
        .clip(RoundedCornerShape(28.dp))

    val modifierWithClick = if (onClick != null) {
        finalModifier.clickable { onClick() }
    } else {
        finalModifier
    }

    Column(
        modifier = modifierWithClick.padding(24.dp),
        content = content
    )
}

// ==========================================
// GLASS BUTTONS
// ==========================================

@Composable
fun GlassButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    text: String,
    icon: ImageVector? = null,
    darkTheme: Boolean = true,
    testTag: String = "glass_button"
) {
    val buttonColor = if (enabled) {
        if (darkTheme) Color(0xFF8A6E56) else Color(0xFF6E5643) // Elegant Bronze / Espresso Accent
    } else {
        if (darkTheme) Color(0x1F8A6E56) else Color(0x1A000000)
    }

    val textColor = if (enabled) Color.White else Color.Gray

    Box(
        modifier = modifier
            .testTag(testTag)
            .shadow(
                elevation = if (enabled) 6.dp else 0.dp,
                shape = RoundedCornerShape(20.dp),
                clip = false,
                ambientColor = buttonColor.copy(alpha = 0.15f),
                spotColor = buttonColor.copy(alpha = 0.25f)
            )
            .background(
                color = buttonColor,
                shape = RoundedCornerShape(20.dp)
            )
            .border(
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)),
                shape = RoundedCornerShape(20.dp)
            )
            .clip(RoundedCornerShape(20.dp))
            .clickable(enabled = enabled) { onClick() }
            .padding(vertical = 14.dp, horizontal = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = textColor,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = text,
                color = textColor,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
        }
    }
}

// ==========================================
// GLASS ICON BUTTONS
// ==========================================

@Composable
fun GlassIconButton(
    onClick: () -> Unit,
    icon: ImageVector,
    contentDescription: String,
    modifier: Modifier = Modifier,
    containerColor: Color? = null,
    darkTheme: Boolean = true
) {
    val color = containerColor ?: if (darkTheme) Color(0x1F8A6E56) else Color(0x1FFFFFFF)
    val strokeColor = if (darkTheme) Color(0x1F8A6E56) else Color(0x4D8A6E56)

    Box(
        modifier = modifier
            .shadow(2.dp, CircleShape)
            .size(44.dp)
            .background(color, CircleShape)
            .border(BorderStroke(1.dp, strokeColor), CircleShape)
            .clip(CircleShape)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = if (darkTheme) Color.White else Color(0xFF13100E),
            modifier = Modifier.size(20.dp)
        )
    }
}

// ==========================================
// GLASS TEXT FIELDS
// ==========================================

@Composable
fun GlassTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    singleLine: Boolean = true,
    darkTheme: Boolean = true,
    testTag: String = "glass_text_field"
) {
    val containerColor = if (darkTheme) Color(0x1F000000) else Color(0x0F000000)
    val strokeColor = if (darkTheme) Color(0x1A8A6E56) else Color(0x338A6E56)

    Box(
        modifier = modifier
            .testTag(testTag)
            .fillMaxWidth()
            .background(containerColor, RoundedCornerShape(18.dp))
            .border(BorderStroke(1.dp, strokeColor), RoundedCornerShape(18.dp))
            .padding(horizontal = 4.dp, vertical = 2.dp)
    ) {
        TextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = {
                Text(
                    text = placeholder,
                    color = if (darkTheme) Color.Gray else Color.DarkGray.copy(alpha = 0.5f),
                    fontSize = 15.sp
                )
            },
            leadingIcon = leadingIcon?.let {
                {
                    Icon(
                        imageVector = it,
                        contentDescription = null,
                        tint = if (darkTheme) Color(0xFF8A6E56) else Color.DarkGray
                    )
                }
            },
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            singleLine = singleLine,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent,
                focusedTextColor = if (darkTheme) Color.White else Color.Black,
                unfocusedTextColor = if (darkTheme) Color.White else Color.Black
            ),
            textStyle = LocalTextStyle.current.copy(fontSize = 15.sp),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

// ==========================================
// GLASS SWITCHES
// ==========================================

@Composable
fun GlassSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    darkTheme: Boolean = true
) {
    val trackWidth = 50.dp
    val trackHeight = 28.dp
    val thumbSize = 22.dp

    val activeColor = if (darkTheme) Color(0xFF8A6E56) else Color(0xFF6E5643)
    val inactiveColor = if (darkTheme) Color(0x1AFFFFFF) else Color(0x1F000000)

    val thumbOffset by animateFloatAsState(
        targetValue = if (checked) 24f else 2f,
        animationSpec = spring(stiffness = 500f, dampingRatio = 0.8f),
        label = "switch_thumb"
    )

    Box(
        modifier = modifier
            .shadow(1.dp, CircleShape)
            .width(trackWidth)
            .height(trackHeight)
            .background(if (checked) activeColor else inactiveColor, CircleShape)
            .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)), CircleShape)
            .clip(CircleShape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onCheckedChange(!checked) }
            .padding(2.dp)
    ) {
        Box(
            modifier = Modifier
                .padding(start = thumbOffset.dp)
                .size(thumbSize)
                .shadow(2.dp, CircleShape)
                .background(Color.White, CircleShape)
        )
    }
}

// ==========================================
// GLASS FLOATING TAB NAVIGATION BAR
// ==========================================

@Composable
fun GlassTabBar(
    modifier: Modifier = Modifier,
    selectedIndex: Int,
    darkTheme: Boolean = true,
    content: @Composable RowScope.() -> Unit
) {
    val glassColor = if (darkTheme) Color(0xE013100E) else Color(0xF2FAF7F5)
    val strokeColor = if (darkTheme) Color(0x298A6E56) else Color(0x4D8A6E56)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .shadow(
                elevation = 16.dp,
                shape = RoundedCornerShape(32.dp),
                clip = false,
                ambientColor = Color.Black.copy(alpha = 0.1f),
                spotColor = Color.Black.copy(alpha = 0.2f)
            )
            .background(glassColor, RoundedCornerShape(32.dp))
            .border(BorderStroke(1.5.dp, strokeColor), RoundedCornerShape(32.dp))
            .clip(RoundedCornerShape(32.dp))
            .padding(vertical = 10.dp, horizontal = 12.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        // Sliding Liquid Glass Layer
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val totalWidth = maxWidth
            val tabWidth = totalWidth / 4

            val animatedOffset by animateDpAsState(
                targetValue = tabWidth * selectedIndex,
                animationSpec = spring(
                    dampingRatio = 0.75f,
                    stiffness = Spring.StiffnessLow
                ),
                label = "nav_slide"
            )

            Box(
                modifier = Modifier
                    .offset(x = animatedOffset)
                    .width(tabWidth)
                    .height(48.dp)
                    .padding(horizontal = 4.dp)
                    .shadow(
                        elevation = 6.dp,
                        shape = RoundedCornerShape(18.dp),
                        clip = false,
                        ambientColor = Color.White.copy(alpha = 0.05f),
                        spotColor = Color.White.copy(alpha = 0.1f)
                    )
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.15f),
                                Color.White.copy(alpha = 0.02f)
                            )
                        ),
                        shape = RoundedCornerShape(18.dp)
                    )
                    .border(
                        BorderStroke(
                            width = 1.dp,
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0.22f),
                                    Color.White.copy(alpha = 0.04f)
                                )
                            )
                        ),
                        shape = RoundedCornerShape(18.dp)
                    )
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            content()
        }
    }
}

@Composable
fun RowScope.GlassTabItem(
    selected: Boolean,
    onClick: () -> Unit,
    icon: ImageVector,
    text: String,
    modifier: Modifier = Modifier,
    darkTheme: Boolean = true
) {
    val activeColor = if (darkTheme) Color(0xFFCDBEB2) else Color(0xFF5C4736)
    val inactiveColor = if (darkTheme) Color.Gray.copy(alpha = 0.5f) else Color.DarkGray.copy(alpha = 0.4f)

    val scale by animateFloatAsState(
        targetValue = if (selected) 1.05f else 1f,
        animationSpec = spring(stiffness = 300f, dampingRatio = 0.8f),
        label = "tab_scale"
    )

    Column(
        modifier = modifier
            .weight(1f)
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = text,
            tint = if (selected) activeColor else inactiveColor,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = text,
            fontSize = 11.sp,
            color = if (selected) activeColor else inactiveColor,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
        )
    }
}
