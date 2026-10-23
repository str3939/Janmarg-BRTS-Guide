package com.brts.ahmedabad.ui.screens

import android.Manifest
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.*
import androidx.compose.ui.unit.*
import com.brts.ahmedabad.data.*
import com.brts.ahmedabad.ui.theme.*

// ─────────────────────────────────────────────
//  Route Planner Screen (Multi-Route, Next Bus & GPS Nearest Navigation)
// ─────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoutePlannerScreen(
    initialSourceStation: Station? = null,
    onResetInitialSource: () -> Unit = {}
) {
    val context = LocalContext.current
    var sourceStation by remember { mutableStateOf<Station?>(initialSourceStation) }
    var destStation by remember { mutableStateOf<Station?>(null) }
    var directResults by remember { mutableStateOf<List<DirectOption>>(emptyList()) }
    var transferResults by remember { mutableStateOf<List<TransferOption>>(emptyList()) }
    var showSourcePicker by remember { mutableStateOf(false) }
    var showDestPicker by remember { mutableStateOf(false) }
    var hasSearched by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Direct, 1: Transfers

    // Location detection states
    var isDetectingLocation by remember { mutableStateOf(false) }
    var detectedNearbyStation by remember { mutableStateOf<NearbyStation?>(null) }

    // React to initialSourceStation changes from other screens
    LaunchedEffect(initialSourceStation) {
        if (initialSourceStation != null) {
            sourceStation = initialSourceStation
            onResetInitialSource()
        }
    }

    fun executeSearch() {
        val src = sourceStation
        val dst = destStation
        if (src != null && dst != null && src.id != dst.id) {
            val curTime = BRTSRepository.getCurrentTimeString()
            directResults = BRTSRepository.findDirectRoutes(src.id, dst.id, curTime)
            transferResults = BRTSRepository.findTransferRoutes(src.id, dst.id, curTime)
            hasSearched = true
            selectedTab = if (directResults.isNotEmpty()) 0 else 1
        }
    }

    fun performLocationDetection() {
        isDetectingLocation = true
        LocationHelper.fetchCurrentLocation(context) { location ->
            isDetectingLocation = false
            if (location != null) {
                val nearest = LocationHelper.findNearestStations(location.latitude, location.longitude, 1).firstOrNull()
                if (nearest != null) {
                    detectedNearbyStation = nearest
                    sourceStation = nearest.station
                    Toast.makeText(context, "Nearest: ${nearest.station.name} (${nearest.formattedDistance})", Toast.LENGTH_SHORT).show()
                    if (destStation != null && destStation != nearest.station) {
                        executeSearch()
                    }
                }
            } else {
                Toast.makeText(context, "Unable to get GPS location. Please ensure Location is ON.", Toast.LENGTH_LONG).show()
            }
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                      permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            performLocationDetection()
        } else {
            Toast.makeText(context, "Location permission required to find nearest station", Toast.LENGTH_SHORT).show()
        }
    }

    fun requestNearestStation() {
        if (LocationHelper.hasLocationPermission(context)) {
            performLocationDetection()
        } else {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0A1628),
                        Color(0xFF0D1F3C),
                        Color(0xFF0A1628)
                    )
                )
            )
    ) {
        // Decorative background glows
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                color = Color(0x120D47A1),
                radius = 300.dp.toPx(),
                center = Offset(size.width * 0.8f, size.height * 0.1f)
            )
            drawCircle(
                color = Color(0x0BFFC107),
                radius = 200.dp.toPx(),
                center = Offset(size.width * 0.1f, size.height * 0.7f)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            BRTSHeader()

            // Journey Input Card (with Nearest Station Detector & Walk Navigation)
            JourneyInputCard(
                sourceStation = sourceStation,
                destStation = destStation,
                detectedNearbyStation = detectedNearbyStation,
                isDetectingLocation = isDetectingLocation,
                onDetectNearest = { requestNearestStation() },
                onOpenMapsWalking = { st -> LocationHelper.openGoogleMapsWalkingRoute(context, st) },
                onSourceClick = { showSourcePicker = true },
                onDestClick = { showDestPicker = true },
                onSwap = {
                    val tmp = sourceStation
                    sourceStation = destStation
                    destStation = tmp
                    if (sourceStation != null && destStation != null) {
                        executeSearch()
                    }
                },
                onSearch = { executeSearch() }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Search Results
            AnimatedVisibility(
                visible = hasSearched,
                enter = fadeIn() + slideInVertically(initialOffsetY = { 40 }),
                exit = fadeOut()
            ) {
                if (directResults.isEmpty() && transferResults.isEmpty()) {
                    NoRouteCard()
                } else {
                    Column(modifier = Modifier.padding(horizontal = 16.dp)) {

                        // Result count banner
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Available Travel Options",
                                style = MaterialTheme.typography.titleMedium,
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            Box(
                                modifier = Modifier
                                    .background(BRTSYellow.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                                    .border(1.dp, BRTSYellow.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    "${directResults.size} Direct • ${transferResults.size} Transfer",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = BRTSYellow,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Filter Tabs (if both direct & transfer exist)
                        if (directResults.isNotEmpty() && transferResults.isNotEmpty()) {
                            TabRow(
                                selectedTabIndex = selectedTab,
                                containerColor = SurfaceCard,
                                contentColor = BRTSYellow,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .padding(bottom = 14.dp)
                            ) {
                                Tab(
                                    selected = selectedTab == 0,
                                    onClick = { selectedTab = 0 },
                                    text = {
                                        Text(
                                            "Direct Buses (${directResults.size})",
                                            fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
                                            color = if (selectedTab == 0) BRTSYellow else TextMuted
                                        )
                                    }
                                )
                                Tab(
                                    selected = selectedTab == 1,
                                    onClick = { selectedTab = 1 },
                                    text = {
                                        Text(
                                            "With 1 Transfer (${transferResults.size})",
                                            fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
                                            color = if (selectedTab == 1) BRTSYellow else TextMuted
                                        )
                                    }
                                )
                            }
                        }

                        // Options List
                        if (selectedTab == 0 && directResults.isNotEmpty()) {
                            directResults.forEachIndexed { index, option ->
                                DirectOptionCard(
                                    option = option,
                                    isFastest = index == 0,
                                    onOpenWalkingRoute = { st -> LocationHelper.openGoogleMapsWalkingRoute(context, st) }
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                            }
                        } else {
                            transferResults.forEach { option ->
                                TransferOptionCard(option = option)
                                Spacer(modifier = Modifier.height(12.dp))
                            }
                        }
                    }
                }
            }

            // Quick Info & Features (When not searched)
            if (!hasSearched) {
                QuickInfoSection(onDetectNearest = { requestNearestStation() })
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }

    // Station Pickers
    if (showSourcePicker) {
        StationPickerSheet(
            title = "Select Origin Station",
            onDetectNearest = {
                showSourcePicker = false
                requestNearestStation()
            },
            onDismiss = { showSourcePicker = false },
            onSelect = {
                sourceStation = it
                showSourcePicker = false
            }
        )
    }
    if (showDestPicker) {
        StationPickerSheet(
            title = "Select Destination Station",
            onDetectNearest = null,
            onDismiss = { showDestPicker = false },
            onSelect = {
                destStation = it
                showDestPicker = false
            }
        )
    }
}

// ─────────────────────────────────────────────
//  BRTS Header Banner
// ─────────────────────────────────────────────

@Composable
fun BRTSHeader() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(Color(0xFF0D47A1), Color(0xFF1565C0))
                )
            )
            .padding(horizontal = 20.dp, vertical = 18.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .background(BRTSYellow, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.DirectionsBus,
                        contentDescription = null,
                        tint = Color(0xFF0D47A1),
                        modifier = Modifier.size(28.dp)
                    )
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text(
                        "Janmarg BRTS",
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Ahmedabad BRTS Bus Network & Live Navigation",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFFBBDEFB)
                    )
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                InfoChip(icon = Icons.Outlined.Route, text = "${BRTSRepository.allRoutes.size} Lines")
                InfoChip(icon = Icons.Outlined.LocationOn, text = "${BRTSRepository.allStations.size} Stations")
                InfoChip(icon = Icons.Outlined.Explore, text = "GPS Walking Maps")
            }
        }
    }
}

@Composable
fun InfoChip(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Row(
        modifier = Modifier
            .background(Color(0x25FFFFFF), RoundedCornerShape(20.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(icon, contentDescription = null, tint = BRTSYellow, modifier = Modifier.size(13.dp))
        Text(text, style = MaterialTheme.typography.labelSmall, color = Color.White)
    }
}

// ─────────────────────────────────────────────
//  Journey Input Card with GPS Nearest Station
// ─────────────────────────────────────────────

@Composable
fun JourneyInputCard(
    sourceStation: Station?,
    destStation: Station?,
    detectedNearbyStation: NearbyStation?,
    isDetectingLocation: Boolean,
    onDetectNearest: () -> Unit,
    onOpenMapsWalking: (Station) -> Unit,
    onSourceClick: () -> Unit,
    onDestClick: () -> Unit,
    onSwap: () -> Unit,
    onSearch: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header with GPS auto-detect button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Plan Your Journey",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextSecondary
                )

                // GPS Nearest Station button
                Surface(
                    onClick = onDetectNearest,
                    shape = RoundedCornerShape(20.dp),
                    color = if (isDetectingLocation) BRTSYellow.copy(alpha = 0.2f) else SurfaceElevated,
                    border = BorderStroke(1.dp, if (isDetectingLocation) BRTSYellow else BRTSYellow.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (isDetectingLocation) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(13.dp),
                                color = BRTSYellow,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Locating...", style = MaterialTheme.typography.labelSmall, color = BRTSYellow)
                        } else {
                            Icon(Icons.Filled.MyLocation, contentDescription = null, tint = BRTSYellow, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(5.dp))
                            Text("Nearest to Me", style = MaterialTheme.typography.labelSmall, color = BRTSYellow, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Origin
            StationSelectorButton(
                label = "Departure Station",
                station = sourceStation,
                placeholder = "Choose departure or tap Nearest to Me...",
                icon = Icons.Filled.TripOrigin,
                iconTint = SuccessGreen,
                onClick = onSourceClick
            )

            // Nearest Station Walking Navigation Banner (if detected)
            if (detectedNearbyStation != null && sourceStation?.id == detectedNearbyStation.station.id) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = SuccessGreen.copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, SuccessGreen.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.NearMe, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Nearest Station: ${detectedNearbyStation.formattedDistance} away",
                                style = MaterialTheme.typography.labelMedium,
                                color = SuccessGreen,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "~${detectedNearbyStation.walkingTimeMinutes} min walk from current location",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary
                            )
                        }
                        Button(
                            onClick = { onOpenMapsWalking(detectedNearbyStation.station) },
                            colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Filled.DirectionsWalk, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Walk in Maps", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Swap Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .padding(start = 20.dp)
                        .width(2.dp)
                        .height(24.dp)
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(SuccessGreen, BRTSRed)
                            ),
                            shape = RoundedCornerShape(1.dp)
                        )
                )
                Spacer(modifier = Modifier.weight(1f))
                IconButton(
                    onClick = onSwap,
                    modifier = Modifier
                        .size(38.dp)
                        .background(SurfaceElevated, CircleShape)
                ) {
                    Icon(
                        Icons.Filled.SwapVert,
                        contentDescription = "Swap Origin and Destination",
                        tint = BRTSYellow,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            // Destination
            StationSelectorButton(
                label = "Destination Station",
                station = destStation,
                placeholder = "Choose where to alight...",
                icon = Icons.Filled.Place,
                iconTint = BRTSRed,
                onClick = onDestClick
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Search Button
            Button(
                onClick = onSearch,
                enabled = sourceStation != null && destStation != null && sourceStation.id != destStation.id,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = BRTSYellow,
                    contentColor = Color(0xFF0A1628),
                    disabledContainerColor = SurfaceElevated,
                    disabledContentColor = TextMuted
                )
            ) {
                Icon(Icons.Filled.Search, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Search Available Buses",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun StationSelectorButton(
    label: String,
    station: Station?,
    placeholder: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = SurfaceElevated,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(label, style = MaterialTheme.typography.labelSmall, color = TextMuted)
                Text(
                    text = station?.name ?: placeholder,
                    style = MaterialTheme.typography.titleMedium,
                    color = if (station != null) TextPrimary else TextMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = FontWeight.SemiBold
                )
                if (station != null && station.servingLines.isNotEmpty()) {
                    Text(
                        "Lines: " + station.servingLines.take(5).joinToString(", ") + if (station.servingLines.size > 5) "..." else "",
                        style = MaterialTheme.typography.labelSmall,
                        color = BRTSYellow.copy(alpha = 0.8f),
                        maxLines = 1
                    )
                }
            }
            Icon(
                Icons.Filled.ChevronRight,
                contentDescription = null,
                tint = TextMuted,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

// ─────────────────────────────────────────────
//  Direct Bus Option Card (with Google Maps walking link)
// ─────────────────────────────────────────────

@Composable
fun DirectOptionCard(
    option: DirectOption,
    isFastest: Boolean,
    onOpenWalkingRoute: (Station) -> Unit
) {
    var expandedStops by remember { mutableStateOf(false) }
    val route = option.route
    val routeColor = Color(route.color)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        elevation = CardDefaults.cardElevation(if (isFastest) 8.dp else 4.dp),
        border = BorderStroke(
            1.dp,
            if (isFastest) BRTSYellow.copy(alpha = 0.5f) else routeColor.copy(alpha = 0.25f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            // Header: Line Badge, Badges, Fares
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Line Number Badge
                Box(
                    modifier = Modifier
                        .background(routeColor.copy(alpha = 0.18f), RoundedCornerShape(8.dp))
                        .border(1.5.dp, routeColor, RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        "Line ${route.shortName}",
                        style = MaterialTheme.typography.titleMedium,
                        color = routeColor,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Line type chip (Down, Up, Circular)
                Box(
                    modifier = Modifier
                        .background(SurfaceElevated, RoundedCornerShape(6.dp))
                        .padding(horizontal = 6.dp, vertical = 3.dp)
                ) {
                    Text(
                        route.lineType,
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                // Official Fare Badge
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        "₹${option.adultFare}",
                        style = MaterialTheme.typography.titleLarge,
                        color = SuccessGreen,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Child: ₹${option.childFare}",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Route Destination subtitle
            Text(
                "Towards: ${route.destinationName}",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(12.dp))

            // NEXT BUS BANNER (with Countdown & Missed-Bus Schedule!)
            val nextBus = option.nextBuses.firstOrNull()
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = if (nextBus != null && nextBus.minutesAway <= 10)
                            Color(0xFF1B5E20).copy(alpha = 0.35f)
                        else
                            SurfaceElevated,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .border(
                        1.dp,
                        if (nextBus != null && nextBus.minutesAway <= 10) SuccessGreen.copy(alpha = 0.5f) else DividerColor,
                        RoundedCornerShape(12.dp)
                    )
                    .padding(12.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Filled.Schedule,
                            contentDescription = null,
                            tint = if (nextBus != null && nextBus.minutesAway <= 10) SuccessGreen else BRTSYellow,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))

                        if (nextBus != null) {
                            Text(
                                if (nextBus.minutesAway == 0) "Arriving NOW"
                                else "Next bus in ${nextBus.minutesAway} min (${nextBus.departureTime})",
                                style = MaterialTheme.typography.titleSmall,
                                color = if (nextBus.minutesAway <= 10) SuccessGreen else TextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        } else {
                            Text(
                                "Operating Hours: ${route.firstBus} – ${route.lastBus}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextMuted
                            )
                        }
                    }

                    // Missed bus next schedule
                    if (option.nextBuses.size > 1) {
                        Spacer(modifier = Modifier.height(6.dp))
                        val laterTimes = option.nextBuses.drop(1).take(3).map { it.departureTime }
                        Text(
                            "If you miss this bus: ${laterTimes.joinToString(" • ")}",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Stats row (Stops, Duration, Timeline expander)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.PinDrop, contentDescription = null, tint = TextMuted, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("${option.stopsCount} stops", style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
                }

                Spacer(modifier = Modifier.width(14.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.Timer, contentDescription = null, tint = TextMuted, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("~${option.estimatedTimeMin} min", style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
                }

                Spacer(modifier = Modifier.weight(1f))

                // Toggle intermediate stops
                TextButton(
                    onClick = { expandedStops = !expandedStops },
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        if (expandedStops) "Hide Stops" else "View Stops",
                        style = MaterialTheme.typography.labelLarge,
                        color = BRTSYellow
                    )
                    Icon(
                        if (expandedStops) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                        contentDescription = null,
                        tint = BRTSYellow,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Google Maps walking link to origin station
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpenWalkingRoute(option.boardAt) }
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Filled.DirectionsWalk, contentDescription = null, tint = InfoBlue, modifier = Modifier.size(15.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    "Walking directions to ${option.boardAt.name} in Google Maps",
                    style = MaterialTheme.typography.labelSmall,
                    color = InfoBlue
                )
            }

            // Expanded Intermediate Stops Timeline
            AnimatedVisibility(visible = expandedStops) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                        .background(SurfaceElevated, RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Text(
                        "Route Station Timeline:",
                        style = MaterialTheme.typography.labelMedium,
                        color = TextMuted,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    option.intermediateStops.forEachIndexed { idx, st ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Timeline dot
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(
                                        when (idx) {
                                            0 -> SuccessGreen
                                            option.intermediateStops.lastIndex -> BRTSRed
                                            else -> TextMuted
                                        },
                                        CircleShape
                                    )
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                st.name,
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (idx == 0 || idx == option.intermediateStops.lastIndex) TextPrimary else TextSecondary,
                                fontWeight = if (idx == 0 || idx == option.intermediateStops.lastIndex) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────
//  Transfer Option Card
// ─────────────────────────────────────────────

@Composable
fun TransferOptionCard(option: TransferOption) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        elevation = CardDefaults.cardElevation(4.dp),
        border = BorderStroke(1.dp, WarningAmber.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .background(WarningAmber.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        "1 Transfer Needed",
                        style = MaterialTheme.typography.labelMedium,
                        color = WarningAmber,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                // Fares
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        "₹${option.adultFare}",
                        style = MaterialTheme.typography.titleLarge,
                        color = SuccessGreen,
                        fontWeight = FontWeight.Bold
                    )
                    Text("Child: ₹${option.childFare}", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Leg 1
            TransferLegView(
                legNumber = 1,
                route = option.firstLeg.route,
                board = option.firstLeg.boardAt.name,
                alight = option.firstLeg.alightAt.name,
                stops = option.firstLeg.stopsCount,
                nextBus = option.firstLeg.nextBuses.firstOrNull()?.departureTime
            )

            // Transfer Hub Point
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
                    .background(SurfaceElevated, RoundedCornerShape(8.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Filled.Sync, contentDescription = null, tint = WarningAmber, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Change bus at: ${option.transferStation.name}",
                    style = MaterialTheme.typography.labelLarge,
                    color = WarningAmber,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // Leg 2
            TransferLegView(
                legNumber = 2,
                route = option.secondLeg.route,
                board = option.secondLeg.boardAt.name,
                alight = option.secondLeg.alightAt.name,
                stops = option.secondLeg.stopsCount,
                nextBus = null
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Total journey summary
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Total: ${option.totalStops} stops",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
                Text(
                    "Est. Travel Time: ~${option.estimatedTimeMin} min",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun TransferLegView(
    legNumber: Int,
    route: BusRoute,
    board: String,
    alight: String,
    stops: Int,
    nextBus: String?
) {
    val routeColor = Color(route.color)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .background(routeColor.copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                .border(1.dp, routeColor, RoundedCornerShape(6.dp))
                .padding(horizontal = 8.dp, vertical = 3.dp)
        ) {
            Text(
                "Line ${route.shortName}",
                style = MaterialTheme.typography.labelMedium,
                color = routeColor,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                "$board ➔ $alight",
                style = MaterialTheme.typography.bodyMedium,
                color = TextPrimary,
                fontWeight = FontWeight.Medium
            )
            Row {
                Text(
                    "$stops stops",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted
                )
                if (nextBus != null) {
                    Text(
                        " • Next: $nextBus",
                        style = MaterialTheme.typography.labelSmall,
                        color = SuccessGreen
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────
//  No Route Card
// ─────────────────────────────────────────────

@Composable
fun NoRouteCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Outlined.DirectionsBus,
                contentDescription = null,
                tint = TextMuted,
                modifier = Modifier.size(56.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                "No Direct Route Found",
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                "Try selecting a nearby major terminal or junction.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )
        }
    }
}

// ─────────────────────────────────────────────
//  Quick Info Section with GPS Station Finder trigger
// ─────────────────────────────────────────────

@Composable
fun QuickInfoSection(onDetectNearest: () -> Unit) {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {

        // Quick GPS Banner
        Surface(
            onClick = onDetectNearest,
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFF0D47A1).copy(alpha = 0.2f),
            border = BorderStroke(1.dp, BRTSBlueLight.copy(alpha = 0.4f)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 14.dp)
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .background(BRTSYellow, RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.NearMe, contentDescription = null, tint = Color(0xFF0D47A1), modifier = Modifier.size(24.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Standing in Ahmedabad?", style = MaterialTheme.typography.titleSmall, color = TextPrimary, fontWeight = FontWeight.Bold)
                    Text("Auto-detect nearest BRTS stop & walk via Google Maps", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                }
                Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = BRTSYellow, modifier = Modifier.size(20.dp))
            }
        }

        Text(
            "BRTS Network At A Glance",
            style = MaterialTheme.typography.titleMedium,
            color = TextSecondary,
            modifier = Modifier.padding(bottom = 10.dp)
        )

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            QuickInfoCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Outlined.Schedule,
                title = "Hours",
                subtitle = "05:30 AM – 11:00 PM",
                color = InfoBlue
            )
            QuickInfoCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Outlined.CurrencyRupee,
                title = "Fare Scale",
                subtitle = "₹5 – ₹30 official",
                color = SuccessGreen
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            QuickInfoCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Outlined.Speed,
                title = "Total Lines",
                subtitle = "149 Official Routes",
                color = WarningAmber
            )
            QuickInfoCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Outlined.DirectionsBus,
                title = "Network Stations",
                subtitle = "193 BRTS Stops",
                color = BRTSYellow
            )
        }
    }
}

@Composable
fun QuickInfoCard(
    modifier: Modifier = Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    color: Color
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(22.dp))
            Spacer(modifier = Modifier.height(6.dp))
            Text(title, style = MaterialTheme.typography.labelSmall, color = TextMuted)
            Text(subtitle, style = MaterialTheme.typography.labelLarge, color = TextPrimary)
        }
    }
}

// ─────────────────────────────────────────────
//  Station Picker Bottom Sheet (With GPS Auto-Detect Option)
// ─────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StationPickerSheet(
    title: String,
    onDetectNearest: (() -> Unit)?,
    onDismiss: () -> Unit,
    onSelect: (Station) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val allStations = BRTSRepository.allStations
    val filtered = remember(searchQuery) {
        if (searchQuery.isBlank()) allStations
        else allStations.filter { st ->
            st.name.contains(searchQuery, ignoreCase = true) ||
            st.nameGuj.contains(searchQuery, ignoreCase = true) ||
            st.servingLines.any { it.contains(searchQuery, ignoreCase = true) }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = SurfaceCard,
        dragHandle = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp, bottom = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .width(36.dp)
                        .height(4.dp)
                        .background(TextMuted, RoundedCornerShape(2.dp))
                )
            }
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
        ) {
            Text(
                title,
                style = MaterialTheme.typography.headlineMedium,
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
            )

            // Auto-detect closest station button (if origin picker)
            if (onDetectNearest != null) {
                Surface(
                    onClick = onDetectNearest,
                    shape = RoundedCornerShape(12.dp),
                    color = BRTSBlue.copy(alpha = 0.2f),
                    border = BorderStroke(1.dp, BRTSBlueLight.copy(alpha = 0.4f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.MyLocation, contentDescription = null, tint = BRTSYellow, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Use Current Location (GPS)", style = MaterialTheme.typography.titleSmall, color = TextPrimary, fontWeight = FontWeight.Bold)
                            Text("Auto-select closest station and show walking route", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                        }
                        Icon(Icons.Filled.ArrowForward, contentDescription = null, tint = BRTSYellow, modifier = Modifier.size(18.dp))
                    }
                }
            }

            // Search bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                placeholder = { Text("Search station name or line (e.g. 1D, RTO)...", color = TextMuted) },
                leadingIcon = {
                    Icon(Icons.Filled.Search, contentDescription = null, tint = TextSecondary)
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Filled.Clear, contentDescription = null, tint = TextMuted)
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = BRTSYellow,
                    unfocusedBorderColor = DividerColor,
                    cursorColor = BRTSYellow,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedContainerColor = SurfaceElevated,
                    unfocusedContainerColor = SurfaceElevated
                )
            )

            Text(
                "${filtered.size} stations found",
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
            )

            // Station list
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 450.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(filtered, key = { it.id }) { station ->
                    StationPickerItem(station = station, onClick = { onSelect(station) })
                }
                item { Spacer(modifier = Modifier.height(14.dp)) }
            }
        }
    }
}

@Composable
fun StationPickerItem(station: Station, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = SurfaceElevated,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(BRTSBlue.copy(alpha = 0.2f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Outlined.DirectionsBus,
                    contentDescription = null,
                    tint = BRTSBlueLight,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    station.name,
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextPrimary,
                    fontWeight = FontWeight.Medium
                )
                if (station.nameGuj.isNotEmpty()) {
                    Text(
                        station.nameGuj,
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                }
                if (station.servingLines.isNotEmpty()) {
                    Text(
                        "Lines: " + station.servingLines.take(4).joinToString(", ") + if (station.servingLines.size > 4) "..." else "",
                        style = MaterialTheme.typography.labelSmall,
                        color = BRTSYellow.copy(alpha = 0.75f)
                    )
                }
            }
            Icon(
                Icons.Filled.ChevronRight,
                contentDescription = null,
                tint = TextMuted,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
