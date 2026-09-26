package com.ahmetyildiz.quakealert.core.ui.component

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics

@Composable
fun ScreenTitle(text: String, modifier: Modifier = Modifier) {
    Text(text = text, modifier = modifier.semantics { heading() })
}
