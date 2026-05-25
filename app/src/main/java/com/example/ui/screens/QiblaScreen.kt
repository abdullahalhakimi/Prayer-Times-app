package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CompassCalibration
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalContext
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

                    // Outer green boundary ring
                    drawCircle(
                        color = DeepTeal,
                        radius = radius,
                        center = center,
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 6f)
                    )

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
                        
                        // Draw North compass needle indicator
                        val northNeedlePath = Path().apply {
                            moveTo(center.x, center.y - radius + 30f) // North point
                            lineTo(center.x - 12f, center.y)             // Left Center pivot
                            lineTo(center.x + 12f, center.y)             // Right Center pivot
                            close()
                        }
                        
                        val southNeedlePath = Path().apply {
                            moveTo(center.x, center.y + radius - 30f) // South point
                            lineTo(center.x - 12f, center.y)             // Left Center pivot
                            lineTo(center.x + 12f, center.y)             // Right Center pivot
                            close()
                        }

                        drawPath(path = northNeedlePath, color = DeepTeal)
                        drawPath(path = southNeedlePath, color = Color.Gray)

                        // Draw Cardinal Markers (N, E, S, W) inside rotated compass dial
                        // N is at 0 degrees (up)
                        // E is at 90 degrees (right)
                        // S is at 180 degrees (down)
                        // W is at 270 degrees (left)
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
                        // Qibla is pointing at the absolute degree angle 'qiblaBearing' on the compass dial list.
                        val qiblaAngleRad = Math.toRadians((qiblaBearing - 90f).toDouble())
                        val targetLineX = center.x + (radius - 50f) * cos(qiblaAngleRad).toFloat()
                        val targetLineY = center.y + (radius - 50f) * sin(qiblaAngleRad).toFloat()

                        // Golden connecting line to Kaaba index
                        drawLine(
                            color = AmberAccent,
                            start = center,
                            end = Offset(targetLineX, targetLineY),
                            strokeWidth = 5f
                        )

                        // Golden target tip circle
                        drawCircle(
                            color = AmberAccent,
                            radius = 18f,
                            center = Offset(targetLineX, targetLineY)
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

                // Overlay beautiful non-rotating Kaaba Icon in middle so it stays upright in our HUD
                // Or let the system show a beautiful locator pointer badge
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "Kaaba alignment star",
                    tint = AmberAccent,
                    modifier = Modifier.size(24.dp)
                )
            }

            // Direction Help Alert instructions card
            Card(
                colors = CardDefaults.cardColors(containerColor = DeepTeal.copy(alpha = 0.08f)),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, DeepTeal.copy(alpha = 0.12f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Hold phone flat instruction",
                        tint = DeepTeal,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "To locate Qibla accurately, please hold your device completely flat and rotate until the golden line ticks straight up.",
                        fontSize = 12.sp,
                        color = Color(0xFF444444),
                        lineHeight = 16.sp
                    )
                }
            }
        }
    }
}
