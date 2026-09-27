package com.yunusemre.m3ustream

import android.app.UiModeManager
import android.content.Context
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.yunusemre.m3ustream.ui.navigation.NavGraph
import com.yunusemre.m3ustream.ui.theme.M3uStreamTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val uiModeManager = getSystemService(Context.UI_MODE_SERVICE) as? UiModeManager
        val isTv = uiModeManager?.currentModeType == Configuration.UI_MODE_TYPE_TELEVISION
        if (isTv) {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        }

        enableEdgeToEdge()
        setContent {
            val updateRelease = androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf<com.yunusemre.m3ustream.util.GitHubRelease?>(null) }
            val currentVersion = try {
                packageManager.getPackageInfo(packageName, 0).versionName ?: "0.1"
            } catch (e: Exception) {
                "0.1"
            }
            
            androidx.compose.runtime.LaunchedEffect(Unit) {
                val updateManager = com.yunusemre.m3ustream.util.UpdateManager(this@MainActivity)
                updateRelease.value = updateManager.checkForUpdate(currentVersion)
            }

            M3uStreamTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    NavGraph()
                    
                    updateRelease.value?.let { release ->
                        androidx.compose.material3.AlertDialog(
                            onDismissRequest = { updateRelease.value = null },
                            title = { androidx.compose.material3.Text("Yeni Güncelleme Mevcut") },
                            text = { androidx.compose.material3.Text("IPTV Player'ın yeni bir sürümü (${release.tagName}) çıktı. Güncellemek ister misiniz?") },
                            confirmButton = {
                                androidx.compose.material3.Button(
                                    onClick = {
                                        val updateManager = com.yunusemre.m3ustream.util.UpdateManager(this@MainActivity)
                                        updateManager.downloadAndInstall(release)
                                        updateRelease.value = null
                                    }
                                ) {
                                    androidx.compose.material3.Text("Güncelle")
                                }
                            },
                            dismissButton = {
                                androidx.compose.material3.TextButton(
                                    onClick = { updateRelease.value = null }
                                ) {
                                    androidx.compose.material3.Text("İptal")
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}
