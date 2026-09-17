package com.bazyak.walletplus.wear

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.lifecycleScope
import androidx.wear.ambient.AmbientLifecycleObserver
import com.bazyak.walletplus.wear.data.WearPassRepository
import com.bazyak.walletplus.wear.sync.WearPhonePassOpener
import com.bazyak.walletplus.wear.sync.WearPhoneSyncRequester
import com.bazyak.walletplus.wear.ui.WearWalletApp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class MainActivity : ComponentActivity() {
    private val repository: WearPassRepository by inject()
    private val syncRequester: WearPhoneSyncRequester by inject()
    private val phonePassOpener: WearPhonePassOpener by inject()

    private val ambientCallback = object : AmbientLifecycleObserver.AmbientLifecycleCallback {
        override fun onEnterAmbient(ambientDetails: AmbientLifecycleObserver.AmbientDetails) {
            leaveAmbient()
        }

        override fun onExitAmbient() = Unit

        override fun onUpdateAmbient() = Unit
    }

    private val ambientObserver by lazy { AmbientLifecycleObserver(this, ambientCallback) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Registering an ambient observer is what routes wrist-down through our callback instead
        // of letting the system swap in its own watch face.
        lifecycle.addObserver(ambientObserver)

        lifecycleScope.launch(Dispatchers.IO) {
            syncRequester.requestSync()
        }

        setContent {
            WearWalletApp(
                repository = repository,
                onOpenPassOnPhone = { passId ->
                    lifecycleScope.launch(Dispatchers.IO) {
                        phonePassOpener.openPassOnPhone(passId)
                    }
                },
                onExitApp = { finishAndRemoveTask() },
            )
        }
    }

    /**
     * Closes rather than backgrounds the app when it leaves the foreground.
     *
     * The stem button sends a HOME intent, which an app cannot intercept, so the only way to make
     * it quit is to finish once we are no longer visible. `onStop` rather than `onPause` because
     * ambient transitions pause the activity while it is still on screen.
     */
    override fun onStop() {
        super.onStop()

        if (!isChangingConfigurations && !isFinishing) {
            finishAndRemoveTask()
        }
    }

    /**
     * Turns the display back on as the ambient transition starts.
     *
     * There is no API to decline ambient, so the display is turned back on instead. The window
     * flags are only honoured while the activity is being brought to the front, hence the
     * self-relaunch — `singleTop`, so no second instance is created.
     */
    private fun leaveAmbient() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setTurnScreenOn(true)
        }
        window.addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON)

        startActivity(
            Intent(this, MainActivity::class.java).addFlags(
                Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_REORDER_TO_FRONT,
            ),
        )
    }
}
