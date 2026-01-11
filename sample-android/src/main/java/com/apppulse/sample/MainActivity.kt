package com.apppulse.sample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.apppulse.core.api.AppPulse
import com.apppulse.core.data.Attributes
import com.apppulse.core.data.EventType

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                SampleScreen()
            }
        }
    }
}

@Composable
private fun SampleScreen() {
    val counter = remember { mutableStateOf(0) }
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "AppPulse events in queue: ${AppPulse.queueSize()}")
            Button(onClick = {
                val id = counter.value + 1
                counter.value = id
                AppPulse.trackEvent(
                    type = EventType.CUSTOM,
                    attributes = Attributes(mapOf("click" to id.toString()))
                )
            }) {
                Text("Track Custom Event")
            }
        }
    }
}
