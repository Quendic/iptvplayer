package com.yunusemre.m3ustream.ui.settings

import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.yunusemre.m3ustream.R
import com.yunusemre.m3ustream.ui.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // Focus state'leri: Çizgi yerine üzerine gelindiğinde yazı rengi kırmızıya döner
    var isFileFocused by remember { mutableStateOf(false) }
    var isUrlFocused by remember { mutableStateOf(false) }
    var isImportFocused by remember { mutableStateOf(false) }
    var isHomeFocused by remember { mutableStateOf(false) }
    var isClearFocused by remember { mutableStateOf(false) }

    val fileTextColor by animateColorAsState(
        targetValue = if (isFileFocused) Color(0xFFE50914) else Color.White,
        animationSpec = tween(180, easing = FastOutSlowInEasing),
        label = "file_color"
    )
    val urlActiveColor by animateColorAsState(
        targetValue = if (isUrlFocused) Color(0xFFE50914) else MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = tween(180, easing = FastOutSlowInEasing),
        label = "url_color"
    )

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            var fileName = "playlist.m3u"
            try {
                context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                    val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (nameIndex != -1 && cursor.moveToFirst()) {
                        fileName = cursor.getString(nameIndex)
                    }
                }
            } catch (_: Exception) {}
            viewModel.selectFile(uri, fileName)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings), fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = stringResource(R.string.import_m3u),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(14.dp))

            // 1. Cihazdan M3U Dosyası Seç (Çizgisiz, üzerine gelindiğinde yazı ve ikon kırmızıya döner)
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .scale(if (isFileFocused) 1.02f else 1f)
                    .onFocusChanged { isFileFocused = it.isFocused }
                    .clickable(
                        enabled = !uiState.isParsing,
                        onClick = { filePickerLauncher.launch(arrayOf("*/*")) }
                    )
                    .focusable(),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.FileOpen, contentDescription = null, tint = fileTextColor)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = uiState.selectedFileName ?: "📁 Cihazdan M3U Dosyası Seç (.m3u / .m3u8)",
                        color = fileTextColor,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (uiState.selectedFileName != null) {
                Text(
                    text = "Seçilen dosya: ${uiState.selectedFileName}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFE50914),
                    modifier = Modifier.padding(top = 4.dp, start = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HorizontalDivider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.surfaceVariant)
                Text(
                    text = " VEYA ",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
                HorizontalDivider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.surfaceVariant)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 2. M3U Web Bağlantısı (URL) (Çizgi hover'ı yok, odaklandığında yazısı kırmızıya döner)
            TextField(
                value = uiState.m3uUrl,
                onValueChange = { viewModel.updateM3uUrl(it) },
                label = { Text("M3U Web Bağlantısı (URL)", color = urlActiveColor) },
                placeholder = { Text("http://.../playlist.m3u", color = urlActiveColor) },
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged { isUrlFocused = it.isFocused }
                    .focusable(),
                shape = RoundedCornerShape(12.dp),
                colors = TextFieldDefaults.colors(
                    focusedTextColor = Color(0xFFE50914),
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    cursorColor = Color(0xFFE50914),
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent
                ),
                singleLine = true,
                enabled = !uiState.isParsing
            )

            Spacer(modifier = Modifier.height(20.dp))

            // 3. Kaydet ve İçe Aktar (Çizgisiz, üzerine gelindiğinde kırmızıya döner)
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .scale(if (isImportFocused) 1.02f else 1f)
                    .onFocusChanged { isImportFocused = it.isFocused }
                    .clickable(
                        enabled = !uiState.isParsing,
                        onClick = { viewModel.importM3u() }
                    )
                    .focusable(),
                shape = RoundedCornerShape(12.dp),
                color = if (isImportFocused) Color(0xFFE50914) else MaterialTheme.colorScheme.surfaceVariant
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    if (uiState.isParsing) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("İçe Aktarılıyor...", fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Filled.Refresh,
                                contentDescription = null,
                                tint = if (isImportFocused) Color.White else Color(0xFFE50914)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(R.string.save_and_import),
                                fontWeight = FontWeight.Bold,
                                color = if (isImportFocused) Color.White else Color(0xFFE50914)
                            )
                        }
                    }
                }
            }

            // Durum / Hata mesajı
            if (uiState.error != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.Error, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = uiState.error ?: "",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            } else if (uiState.statusMessage != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = Color(0xFFE50914))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = uiState.statusMessage ?: "",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (uiState.statusMessage?.contains("Başarıyla") == true || uiState.totalCount > 0) {
                    Spacer(modifier = Modifier.height(14.dp))
                    // 4. Ana Sayfaya Git (Çizgisiz, üzerine gelindiğinde kırmızıya döner)
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .scale(if (isHomeFocused) 1.02f else 1f)
                            .onFocusChanged { isHomeFocused = it.isFocused }
                            .clickable {
                                navController.navigate(Screen.Home.route) {
                                    popUpTo(Screen.Home.route) { inclusive = false }
                                    launchSingleTop = true
                                }
                            }
                            .focusable(),
                        shape = RoundedCornerShape(12.dp),
                        color = if (isHomeFocused) Color(0xFFE50914) else MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                Icons.Filled.Home,
                                contentDescription = null,
                                tint = if (isHomeFocused) Color.White else Color(0xFFE50914)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "🎬 Ana Sayfaya Git",
                                fontWeight = FontWeight.Bold,
                                color = if (isHomeFocused) Color.White else Color(0xFFE50914)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // İstatistik Kartı
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Veritabanı İstatistikleri",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFE50914)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("${stringResource(R.string.total_movies)}:", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${uiState.movieCount}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("${stringResource(R.string.total_series)}:", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${uiState.seriesCount}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Toplam Öğe:", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${uiState.totalCount}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("${stringResource(R.string.last_updated)}:", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(uiState.lastUpdate, color = MaterialTheme.colorScheme.onSurface)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 5. Verileri Temizle (Çizgisiz, üzerine gelindiğinde canlı kırmızıya döner)
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .scale(if (isClearFocused) 1.02f else 1f)
                    .onFocusChanged { isClearFocused = it.isFocused }
                    .clickable(
                        enabled = !uiState.isParsing,
                        onClick = { viewModel.clearData() }
                    )
                    .focusable(),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Text(
                        text = stringResource(R.string.clear_data),
                        color = if (isClearFocused) Color(0xFFE50914) else MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
