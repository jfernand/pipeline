package org.cr.pipeline

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import org.cr.pipeline.data.db.AndroidDatabaseContext

class MainActivity : ComponentActivity() {
    private var navController: NavHostController? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        AndroidDatabaseContext.init(this)

        setContent {
            App(
                initialDeepLink = intent.data?.toString(),
                onNavHostReady = { controller -> navController = controller },
            )
        }
    }

    // Called for a "warm" start (activity already running); the NavController's graph is
    // guaranteed to already be set by this point, unlike on cold start (see the comment in
    // PipelineTabletApp/PipelinePhoneApp on why that case needs initialDeepLink instead).
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        navController?.handleDeepLink(intent)
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}
