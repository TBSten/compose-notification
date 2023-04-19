package me.tbsten.notificationpractice.setup

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat

private const val NOTIFICATION_PERMISSION = Manifest.permission.POST_NOTIFICATIONS

enum class SetupStatus {
    Loading,
    Granted,
    Pending,
    Denied,
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetupScreen(
    gotoNotifyScreen: () -> Unit,
) {
    val setupState = rememberSetupState()

    LaunchedEffect(Unit) {
        if (setupState.status == SetupStatus.Granted)
            gotoNotifyScreen()
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("設定") })
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
            Text("${setupState.status}")
            if (setupState.status == SetupStatus.Loading)
                CircularProgressIndicator()
            if (setupState.status == SetupStatus.Granted)
                Text("通知 ON")
            if (setupState.status == SetupStatus.Pending) {
                Text("通知はまだONになっていません")
                Button(onClick = { setupState.requestPermissions() }) {
                    Text("ONにする")
                }
            }
            if (setupState.status == SetupStatus.Denied) {
                Text("通知がOFFになっています")
                Button(onClick = { setupState.gotoSetting() }) {
                    Text("ONにする")
                }
            }

            Button(onClick = { gotoNotifyScreen() }) {
                Text("通知送信ページへ")
            }
        }
    }
}

interface SetupState {
    val status: SetupStatus
    fun requestPermissions()
    fun gotoSetting()
}

@Composable
fun rememberSetupState(): SetupState {
    val context = LocalContext.current
    var status by remember {
        mutableStateOf(SetupStatus.Loading)
    }
    val permissionRequester =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
            status = getCurrentStatus(context)
        }
    val gotoSetting =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            status = getCurrentStatus(context)
        }

    LaunchedEffect(Unit) {
        status = getCurrentStatus(context)

    }
    return object : SetupState {
        override val status: SetupStatus
            get() = status

        override fun requestPermissions() {
            permissionRequester.launch(NOTIFICATION_PERMISSION)
        }

        override fun gotoSetting() {
            val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
            }
            gotoSetting.launch(intent)
        }

    }
}


fun getCurrentStatus(context: Context): SetupStatus {
    val checkPermissionResult = ActivityCompat.checkSelfPermission(
        context,
        NOTIFICATION_PERMISSION,
    )
    if (checkPermissionResult == PackageManager.PERMISSION_GRANTED)
        return SetupStatus.Granted
    val shouldShowRequestPermissionRationale =
        ActivityCompat.shouldShowRequestPermissionRationale(
            context as Activity,
            NOTIFICATION_PERMISSION,
        )
    return if (shouldShowRequestPermissionRationale)
        SetupStatus.Pending
    else
        SetupStatus.Denied
}
