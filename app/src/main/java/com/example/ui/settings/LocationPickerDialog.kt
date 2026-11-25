package com.example.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import com.example.ui.theme.DarkSurfaceBorder
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.util.CityDatabase

/**
 * Lets the user pick any city from the built-in list, or type custom coordinates.
 * [onSelect] receives (name, latitude, longitude, timezoneId); timezoneId "" = device time zone.
 */
@Composable
fun LocationPickerDialog(
    onDismiss: () -> Unit,
    onSelect: (String, Double, Double, String) -> Unit
) {
    var query by remember { mutableStateOf("") }
    var customMode by remember { mutableStateOf(false) }
    var customName by remember { mutableStateOf("") }
    var customLat by remember { mutableStateOf("") }
    var customLng by remember { mutableStateOf("") }

    val lat = customLat.toDoubleOrNull()
    val lng = customLng.toDoubleOrNull()
    val customValid = lat != null && lng != null && lat in -90.0..90.0 && lng in -180.0..180.0

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurfaceElevated,
        title = {
            Text(
                text = if (customMode) "Custom coordinates" else "Choose location",
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            if (customMode) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = customName,
                        onValueChange = { customName = it },
                        label = { Text("Place name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("custom_name_field")
                    )
                    OutlinedTextField(
                        value = customLat,
                        onValueChange = { customLat = it },
                        label = { Text("Latitude (-90 to 90, S is negative)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth().testTag("custom_lat_field")
                    )
                    OutlinedTextField(
                        value = customLng,
                        onValueChange = { customLng = it },
                        label = { Text("Longitude (-180 to 180, W is negative)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth().testTag("custom_lng_field")
                    )
                    Text(
                        text = "Times are shown in this phone's time zone.",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted
                    )
                }
            } else {
                Column {
                    OutlinedTextField(
                        value = query,
                        onValueChange = { query = it },
                        label = { Text("Search city or country") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("city_search_field")
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    val results = CityDatabase.search(query)
                    if (results.isEmpty()) {
                        Text(
                            text = "No city found. Use custom coordinates instead.",
                            color = TextSecondary,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    LazyColumn(modifier = Modifier.heightIn(max = 320.dp)) {
                        items(results, key = { "${it.name}|${it.country}" }) { city ->
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onSelect(city.displayName, city.latitude, city.longitude, city.timezoneId)
                                    }
                                    .padding(vertical = 10.dp)
                            ) {
                                Text(city.name, color = TextPrimary, fontWeight = FontWeight.SemiBold)
                                Text(city.country, color = TextSecondary, style = MaterialTheme.typography.labelSmall)
                            }
                            HorizontalDivider(color = DarkSurfaceBorder)
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (customMode) {
                TextButton(
                    onClick = {
                        val name = customName.trim().ifBlank { "%.3f°, %.3f°".format(lat, lng) }
                        onSelect(name, lat!!, lng!!, "")
                    },
                    enabled = customValid
                ) { Text("Use this location", color = if (customValid) GoldAccent else TextMuted) }
            } else {
                TextButton(onClick = { customMode = true }) { Text("Custom coordinates", color = GoldAccent) }
            }
        },
        dismissButton = {
            TextButton(onClick = { if (customMode) customMode = false else onDismiss() }) {
                Text(if (customMode) "Back" else "Cancel", color = TextSecondary)
            }
        }
    )
}
