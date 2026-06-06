package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.batoulapps.adhan.CalculationMethod
import com.example.data.City
import com.example.data.Country
import com.example.data.CountryCityProvider
import com.example.ui.theme.DeepTeal
import com.example.ui.theme.WarmCreame

enum class ManualStep {
    SELECT_COUNTRY,
    SELECT_CITY,
    SUMMARY
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManualAddLocationScreen(
    onDismiss: () -> Unit,
    onAdd: (String, Double, Double, CalculationMethod) -> Unit
) {
    var currentStep by remember { mutableStateOf(ManualStep.SELECT_COUNTRY) }
    var selectedCountry by remember { mutableStateOf<Country?>(null) }
    var selectedCity by remember { mutableStateOf<City?>(null) }
    var selectedMethod by remember { mutableStateOf(CalculationMethod.MOONSIGHTING_COMMITTEE) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = when (currentStep) {
                            ManualStep.SELECT_COUNTRY -> "Select Country"
                            ManualStep.SELECT_CITY -> "Select City"
                            ManualStep.SUMMARY -> "Location Summary"
                        },
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        when (currentStep) {
                            ManualStep.SELECT_COUNTRY -> onDismiss()
                            ManualStep.SELECT_CITY -> currentStep = ManualStep.SELECT_COUNTRY
                            ManualStep.SUMMARY -> currentStep = ManualStep.SELECT_CITY
                        }
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DeepTeal)
            )
        },
        containerColor = WarmCreame
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            when (currentStep) {
                ManualStep.SELECT_COUNTRY -> CountryList {
                    selectedCountry = it
                    currentStep = ManualStep.SELECT_CITY
                }
                ManualStep.SELECT_CITY -> CityList(selectedCountry?.code ?: "") {
                    selectedCity = it
                    currentStep = ManualStep.SUMMARY
                }
                ManualStep.SUMMARY -> SummaryStep(
                    city = selectedCity!!,
                    method = selectedMethod,
                    onMethodChange = { selectedMethod = it },
                    onConfirm = {
                        onAdd(selectedCity!!.name, selectedCity!!.lat, selectedCity!!.lon, selectedMethod)
                    }
                )
            }
        }
    }
}

@Composable
fun CountryList(onCountrySelected: (Country) -> Unit) {
    var searchQuery by remember { mutableStateOf("") }
    val filteredCountries = CountryCityProvider.countries.filter {
        it.name.contains(searchQuery, ignoreCase = true)
    }

    Column {
        SearchBar(query = searchQuery, onQueryChange = { searchQuery = it }, placeholder = "Search Country")
        LazyColumn {
            items(filteredCountries) { country ->
                ListItem(
                    headlineContent = { Text(country.name) },
                    modifier = Modifier.clickable { onCountrySelected(country) }
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = Color.LightGray.copy(alpha = 0.5f))
            }
        }
    }
}

@Composable
fun CityList(countryCode: String, onCitySelected: (City) -> Unit) {
    var searchQuery by remember { mutableStateOf("") }
    val filteredCities = CountryCityProvider.getCitiesForCountry(countryCode).filter {
        it.name.contains(searchQuery, ignoreCase = true)
    }

    Column {
        SearchBar(query = searchQuery, onQueryChange = { searchQuery = it }, placeholder = "Search City")
        LazyColumn {
            items(filteredCities) { city ->
                ListItem(
                    headlineContent = { Text(city.name) },
                    modifier = Modifier.clickable { onCitySelected(city) }
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = Color.LightGray.copy(alpha = 0.5f))
            }
        }
    }
}

@Composable
fun SummaryStep(
    city: City,
    method: CalculationMethod,
    onMethodChange: (CalculationMethod) -> Unit,
    onConfirm: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("City: ${city.name}", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                Text("Latitude: ${city.lat}", color = Color.Gray)
                Text("Longitude: ${city.lon}", color = Color.Gray)
            }
        }

        Text("Select Calculation Method", fontWeight = FontWeight.SemiBold)
        CalculationMethodDropdown(selectedMethod = method, onMethodSelected = onMethodChange)

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = onConfirm,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = DeepTeal),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Add City", color = Color.White, modifier = Modifier.padding(8.dp))
        }
    }
}

@Composable
fun SearchBar(query: String, onQueryChange: (String) -> Unit, placeholder: String) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        placeholder = { Text(placeholder) },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
        shape = RoundedCornerShape(12.dp),
        singleLine = true
    )
}
