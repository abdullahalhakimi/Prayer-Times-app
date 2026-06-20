package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CompassCalibration
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.CompassViewModel
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.DeepTeal
import com.example.ui.theme.WarmCreame
import com.example.ui.theme.BorderColor
import kotlin.math.cos
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QiblaScreen(
    viewModel: CompassViewModel,
    modifier: Modifier = Modifier
) {
    val azimuth by viewModel.azimuth.collectAsState()
    val qiblaBearing by viewModel.qiblaBearing.collectAsState()
    val magneticStrength by viewModel.magneticField.collectAsState()

    // Calculate angle difference and alignment status
    val angleDiff = Math.abs((qiblaBearing - azimuth + 180) % 360 - 180)
    val isAligned = angleDiff < 3f
    val signedDiff = ((qiblaBearing - azimuth + 180) % 360 - 180)

    // Trigger haptic vibration when entering alignment state
    val haptic = LocalHapticFeedback.current
    LaunchedEffect(isAligned) {
        if (isAligned) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }

    // Alignment animations (color and pulsing scale/alpha)
    val compassGlowColor by animateColorAsState(
        targetValue = if (isAligned) AmberAccent else DeepTeal,
        animationSpec = tween(400),
        label = "glowColor"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val glowScale by if (isAligned) {
        infiniteTransition.animateFloat(
            initialValue = 0.98f,
            targetValue = 1.05f,
            animationSpec = infiniteRepeatable(
                animation = tween(1200, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "scale"
        )
    } else {
        remember { mutableStateOf(1f) }
    }

    val glowAlpha by if (isAligned) {
        infiniteTransition.animateFloat(
            initialValue = 0.08f,
            targetValue = 0.22f,
            animationSpec = infiniteRepeatable(
                animation = tween(1200, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "alpha"
        )
    } else {
        remember { mutableStateOf(0f) }
    }

    val guideText = if (isAligned) {
        "Qibla Aligned! You are facing the Kaaba."
    } else {
        val degrees = Math.round(Math.abs(signedDiff))
        if (signedDiff > 0) {
            "Rotate $degrees° Right to align with Kaaba"
        } else {
            "Rotate $degrees° Left to align with Kaaba"
        }
    }

    val guideColor = if (isAligned) Color(0xFF2E7D32) else Color(0xFF1E4646)
    val guideBg = if (isAligned) Color(0xFFE8F5E9) else DeepTeal.copy(alpha = 0.08f)
    val guideBorder = if (isAligned) Color(0xFFC8E6C9) else DeepTeal.copy(alpha = 0.12f)

    val guideBgAnimated by animateColorAsState(targetValue = guideBg, animationSpec = tween(400), label = "guideBg")
    val guideBorderAnimated by animateColorAsState(targetValue = guideBorder, animationSpec = tween(400), label = "guideBorder")

    // Register/unregister sensor listeners on composition lifecycle
    DisposableEffect(key1 = viewModel) {
        viewModel.registerListeners()
        onDispose {
            viewModel.unregisterListeners()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Qibla Finder", fontWeight = FontWeight.Bold, color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DeepTeal),
                actions = {
                    Icon(
                        imageVector = Icons.Default.CompassCalibration,
                        contentDescription = "Sensor Status",
                        tint = AmberAccent,
                        modifier = Modifier.padding(end = 16.dp)
                    )
                }
            )
        },
        containerColor = WarmCreame,
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            
            // Header Info cards
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // True North Degree Info
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("True North", fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Medium)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${azimuth.toInt()}° N",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = DeepTeal
                        )
                    }
                }

                // Kaaba absolute bearing Target Info
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Kaaba Angle", fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Medium)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${qiblaBearing.toInt()}°",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = AmberAccent
                        )
                    }
                }

                // Magnetic Field Strength Info
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Magnetic Field", fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Medium)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${magneticStrength.toInt()} µT",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = DeepTeal
                        )
                    }
                }
            }

            // Central Compass Canvas
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .weight(1.5f)
                    .fillMaxWidth()
                    .testTag("qibla_compass_box")
            ) {
                // Circle outline backdrop
                Canvas(
                    modifier = Modifier
                        .size(310.dp)
                        .testTag("compass_canvas")
                ) {
                    val center = Offset(size.width / 2, size.height / 2)
                    val radius = size.width / 2 - 16f

                    // Pulsing glow backdrop when aligned
                    if (isAligned) {
                        drawCircle(
                            color = AmberAccent.copy(alpha = glowAlpha),
                            radius = radius * glowScale,
                            center = center
                        )
                    }

                    // Outer boundary ring (glow color)
                    drawCircle(
                        color = compassGlowColor,
                        radius = radius,
                        center = center,
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = if (isAligned) 8f else 6f)
                    )

                    // Secondary thin outer ring when aligned for extra flare
                    if (isAligned) {
                        drawCircle(
                            color = AmberAccent.copy(alpha = 0.3f),
                            radius = radius + 8f,
                            center = center,
                            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 4f)
                        )
                    }

                    // Card ticks and degree rings
                    drawCircle(
                        color = Color.LightGray.copy(alpha = 0.5f),
                        radius = radius - 15f,
                        center = center,
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2f)
                    )

                    // Inner decorative circle
                    drawCircle(
                        color = DeepTeal.copy(alpha = 0.04f),
                        radius = radius - 16f,
                        center = center
                    )

                    // Rotate entire canvas to reflect physical navigation
                    // Azimuth points to north, so -azimuth rotates north to point up!
                    rotate(degrees = -azimuth, pivot = center) {
                        
                        // Draw detailed compass ticks and numbers
                        for (angle in 0 until 360 step 5) {
                            val angleRad = Math.toRadians(angle.toDouble())
                            val isMajor = angle % 30 == 0
                            val isCardinal = angle % 90 == 0
                            
                            if (isCardinal) continue
                            
                            val tickLength = if (isMajor) 24f else 12f
                            val strokeWidth = if (isMajor) 4f else 2f
                            val tickColor = if (isMajor) DeepTeal.copy(alpha = 0.6f) else Color.Gray.copy(alpha = 0.3f)
                            
                            val radialAngleRad = angleRad - Math.PI / 2
                            val startX = center.x + radius * cos(radialAngleRad).toFloat()
                            val startY = center.y + radius * sin(radialAngleRad).toFloat()
                            val endX = center.x + (radius - tickLength) * cos(radialAngleRad).toFloat()
                            val endY = center.y + (radius - tickLength) * sin(radialAngleRad).toFloat()
                            
                            drawLine(
                                color = tickColor,
                                start = Offset(startX, startY),
                                end = Offset(endX, endY),
                                strokeWidth = strokeWidth
                            )
                            
                            if (isMajor) {
                                drawContext.canvas.nativeCanvas.apply {
                                    val paint = android.graphics.Paint().apply {
                                        color = android.graphics.Color.parseColor("#64748B")
                                        textSize = 24f
                                        textAlign = android.graphics.Paint.Align.CENTER
                                    }
                                    val textDist = radius - 48f
                                    val textX = center.x + textDist * cos(radialAngleRad).toFloat()
                                    val textY = center.y + textDist * sin(radialAngleRad).toFloat() + 8f
                                    
                                    drawText("$angle", textX, textY, paint)
                                }
                            }
                        }

                        // Draw North/South needles (Two-toned for modern 3D look)
                        val northLeftPath = Path().apply {
                            moveTo(center.x, center.y - radius + 30f)
                            lineTo(center.x - 12f, center.y)
                            lineTo(center.x, center.y)
                            close()
                        }
                        val northRightPath = Path().apply {
                            moveTo(center.x, center.y - radius + 30f)
                            lineTo(center.x + 12f, center.y)
                            lineTo(center.x, center.y)
                            close()
                        }
                        val southLeftPath = Path().apply {
                            moveTo(center.x, center.y + radius - 30f)
                            lineTo(center.x - 12f, center.y)
                            lineTo(center.x, center.y)
                            close()
                        }
                        val southRightPath = Path().apply {
                            moveTo(center.x, center.y + radius - 30f)
                            lineTo(center.x + 12f, center.y)
                            lineTo(center.x, center.y)
                            close()
                        }

                        drawPath(path = northLeftPath, color = DeepTeal)
                        drawPath(path = northRightPath, color = DeepTeal.copy(alpha = 0.8f))
                        drawPath(path = southLeftPath, color = Color.Gray)
                        drawPath(path = southRightPath, color = Color.LightGray)

                        // Draw Cardinal Markers (N, E, S, W) inside rotated compass dial
                        drawContext.canvas.nativeCanvas.apply {
                            val paint = android.graphics.Paint().apply {
                                color = android.graphics.Color.parseColor("#1E4646")
                                textSize = 44f
                                isFakeBoldText = true
                                textAlign = android.graphics.Paint.Align.CENTER
                            }
                            
                            val textDist = radius - 60f
                            
                            // N
                            drawText("N", center.x, center.y - textDist + 15f, paint)
                            
                            // S
                            paint.color = android.graphics.Color.GRAY
                            drawText("S", center.x, center.y + textDist + 15f, paint)
                            
                            // E
                            paint.color = android.graphics.Color.parseColor("#1E4646")
                            drawText("E", center.x + textDist, center.y + 15f, paint)
                            
                            // W
                            drawText("W", center.x - textDist, center.y + 15f, paint)
                        }

                        // Drawing Qibla Direction Kaaba Highlighter line & icon indicator!
                        val qiblaAngleRad = Math.toRadians((qiblaBearing - 90f).toDouble())
                        val targetLineX = center.x + (radius - 50f) * cos(qiblaAngleRad).toFloat()
                        val targetLineY = center.y + (radius - 50f) * sin(qiblaAngleRad).toFloat()

                        // Golden connecting line to Kaaba
                        drawLine(
                            color = AmberAccent,
                            start = center,
                            end = Offset(targetLineX, targetLineY),
                            strokeWidth = 6f
                        )

                        // Draw a beautiful miniature Kaaba at the target tip!
                        val cubeSize = 36f
                        val halfCube = cubeSize / 2f

                        // Outer glowing circle badge
                        drawCircle(
                            color = if (isAligned) Color.White else AmberAccent,
                            radius = 24f,
                            center = Offset(targetLineX, targetLineY)
                        )

                        // Draw Kaaba body (Black cube)
                        drawRect(
                            color = Color(0xFF121212),
                            topLeft = Offset(targetLineX - halfCube, targetLineY - halfCube),
                            size = androidx.compose.ui.geometry.Size(cubeSize, cubeSize)
                        )

                        // Draw Kiswah Gold Band (upper third of Kaaba)
                        drawRect(
                            color = AmberAccent,
                            topLeft = Offset(targetLineX - halfCube, targetLineY - halfCube + 6f),
                            size = androidx.compose.ui.geometry.Size(cubeSize, 6f)
                        )
                    }

                    // Inner central core pin
                    drawCircle(
                        color = Color.White,
                        radius = 8f,
                        center = center
                    )
                    drawCircle(
                        color = DeepTeal,
                        radius = 4f,
                        center = center
                    )
                }

                // Dynamic center icon (Green checkmark when aligned, Amber star otherwise)
                Icon(
                    imageVector = if (isAligned) Icons.Default.CheckCircle else Icons.Default.Star,
                    contentDescription = "Kaaba alignment status",
                    tint = if (isAligned) Color(0xFF2E7D32) else AmberAccent,
                    modifier = Modifier
                        .size(if (isAligned) 36.dp else 24.dp)
                        .graphicsLayer(scaleX = if (isAligned) glowScale else 1f, scaleY = if (isAligned) glowScale else 1f)
                )
            }

            // Guidance Panel
            Card(
                colors = CardDefaults.cardColors(containerColor = guideBgAnimated),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, guideBorderAnimated),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (isAligned) Icons.Default.CheckCircle else Icons.Default.LocationOn,
                        contentDescription = "Guidance instruction",
                        tint = guideColor,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = guideText,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = guideColor
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Hold your device completely flat for maximum accuracy.",
                            fontSize = 11.sp,
                            color = if (isAligned) Color(0xFF558B2F) else Color(0xFF64748B)
                        )
                    }
                }
            }
        }
    }
}
