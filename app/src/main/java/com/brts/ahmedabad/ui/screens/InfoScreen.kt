package com.brts.ahmedabad.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import com.brts.ahmedabad.ui.theme.*

// ─────────────────────────────────────────────
//  Info / About Screen
// ─────────────────────────────────────────────

@Composable
fun InfoScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceDark)
            .verticalScroll(rememberScrollState())
    ) {
        // Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(Color(0xFF4A148C), Color(0xFF6A1B9A))
                    )
                )
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Column {
                Text(
                    "About Janmarg",
                    style = MaterialTheme.typography.headlineLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Ahmedabad BRTS Information Guide",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFFE1BEE7)
                )
            }
        }

        Column(modifier = Modifier.padding(16.dp)) {

            // Fare Structure
            InfoSection(
                title = "Fare Structure",
                icon = Icons.Outlined.CurrencyRupee,
                color = SuccessGreen
            ) {
                FareTable()
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Bus Timings
            InfoSection(
                title = "Bus Timings",
                icon = Icons.Outlined.Schedule,
                color = InfoBlue
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    TimingRow("Service Hours", "6:00 AM – 11:00 PM daily")
                    TimingRow("Peak Hours", "8–10 AM & 5–8 PM")
                    TimingRow("Peak Frequency", "Every 2–5 minutes")
                    TimingRow("Off-Peak Frequency", "Every 5–15 minutes")
                    TimingRow("First Bus", "~5:30 AM (some routes)")
                    TimingRow("Last Bus", "~11:00 PM")
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Payment
            InfoSection(
                title = "Payment Methods",
                icon = Icons.Outlined.CreditCard,
                color = BRTSYellow
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    PaymentCard(
                        title = "Cash Ticket",
                        desc = "Purchase at station counter before boarding. Exact change preferred.",
                        icon = Icons.Filled.Money
                    )
                    PaymentCard(
                        title = "Janmitra Smart Card",
                        desc = "Prepaid smart card. Purchase at any BRTS station. Recharge in ₹50 multiples. Offers convenience and faster boarding.",
                        icon = Icons.Filled.CreditCard
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Network Stats
            InfoSection(
                title = "Network Overview",
                icon = Icons.Outlined.Map,
                color = BRTSRed
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatRow("Total Routes", "149 (Lines: 1D, 1U, 15D, 101-C, etc.)")
                    StatRow("Total Stations", "193 BRTS stations & cabins")
                    StatRow("Fare System", "Official GTFS Distance Matrix (₹5 – ₹30)")
                    StatRow("Fleet Size", "380+ buses (incl. electric)")
                    StatRow("Daily Ridership", "~1.5 lakh passengers")
                    StatRow("Operator", "Ahmedabad Janmarg Limited (AJL)")
                    StatRow("Launched", "October 14, 2009")
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Tips
            InfoSection(
                title = "Travel Tips",
                icon = Icons.Outlined.Lightbulb,
                color = WarningAmber
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    TipItem("🎫 Buy your ticket before boarding – tickets are purchased at the station counter, not inside the bus.")
                    TipItem("🚌 BRTS runs on dedicated lanes – avoid traffic jams!")
                    TipItem("♿ All BRTS stations have ramps for accessibility.")
                    TipItem("📱 This app works 100% offline – no internet needed.")
                    TipItem("💳 Janmitra card saves time at busy stations.")
                    TipItem("🔄 For transfer journeys, alight at the transfer station and wait for the connecting bus on the same platform or across.")
                    TipItem("⏱ Allow 5–10 extra minutes for transfers.")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // App info
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceCard)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Filled.DirectionsBus,
                        contentDescription = null,
                        tint = BRTSYellow,
                        modifier = Modifier.size(40.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Janmarg BRTS Guide",
                        style = MaterialTheme.typography.titleLarge,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        "Version 1.0 • Offline Route Planner",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        "All data is embedded in the app and works without internet connectivity.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun InfoSection(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        border = BorderStroke(1.dp, color.copy(alpha = 0.2f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(22.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    title,
                    style = MaterialTheme.typography.titleMedium,
                    color = color,
                    fontWeight = FontWeight.SemiBold
                )
            }
            content()
        }
    }
}

@Composable
fun FareTable() {
    val rows = listOf(
        Triple("0–2 km", "₹5", "Minimum fare"),
        Triple("2–5 km", "₹8–12", "Short trip"),
        Triple("5–10 km", "₹12–18", "Medium trip"),
        Triple("10–15 km", "₹18–25", "Long trip"),
        Triple("15+ km", "₹25–30", "Maximum fare")
    )

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(SuccessGreen.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                .padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            Text("Distance", Modifier.weight(1f), style = MaterialTheme.typography.labelLarge, color = TextMuted)
            Text("Fare", Modifier.weight(0.7f), style = MaterialTheme.typography.labelLarge, color = TextMuted)
            Text("Type", Modifier.weight(1f), style = MaterialTheme.typography.labelLarge, color = TextMuted)
        }
        rows.forEach { (dist, fare, type) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(dist, Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                Text(fare, Modifier.weight(0.7f), style = MaterialTheme.typography.bodyLarge,
                    color = SuccessGreen, fontWeight = FontWeight.Bold)
                Text(type, Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium, color = TextMuted)
            }
        }
    }
}

@Composable
fun TimingRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(SurfaceElevated, RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
        Text(value, style = MaterialTheme.typography.labelLarge, color = InfoBlue, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun PaymentCard(title: String, desc: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(SurfaceElevated, RoundedCornerShape(10.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(icon, contentDescription = null, tint = BRTSYellow, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(title, style = MaterialTheme.typography.labelLarge, color = TextPrimary, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(2.dp))
            Text(desc, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
        }
    }
}

@Composable
fun StatRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(SurfaceElevated, RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
        Text(value, style = MaterialTheme.typography.labelLarge, color = TextPrimary, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun TipItem(text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(SurfaceElevated, RoundedCornerShape(10.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text,
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary
        )
    }
}
