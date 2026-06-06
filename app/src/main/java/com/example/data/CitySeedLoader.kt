package com.example.data

import android.content.Context

object CitySeedLoader {
    private const val BATCH_SIZE = 1000

    suspend fun importFromAsset(context: Context, dao: CityDao) {
        val fallbackCities = CountryCityProvider.getSeedCities().map { it.toEntity() }

        runCatching {
            context.assets.open("world_cities.csv").bufferedReader().useLines { lines ->
                val iterator = lines.iterator()
                if (!iterator.hasNext()) {
                    dao.insertAll(fallbackCities)
                    return@useLines
                }

                val header = parseCsvRow(iterator.next()) ?: run {
                    dao.insertAll(fallbackCities)
                    return@useLines
                }

                val cityIndex = header.indexOfFirst { it.equals("name", ignoreCase = true) || it.equals("city", ignoreCase = true) }
                val latIndex = header.indexOfFirst { it.equals("lat", ignoreCase = true) }
                val lonIndex = header.indexOfFirst { it.equals("lng", ignoreCase = true) || it.equals("lon", ignoreCase = true) }
                val countryIndex = header.indexOfFirst { it.equals("iso2", ignoreCase = true) || it.equals("country", ignoreCase = true) }

                if (cityIndex == -1 || latIndex == -1 || lonIndex == -1 || countryIndex == -1) {
                    dao.insertAll(fallbackCities)
                    return@useLines
                }

                val batch = ArrayList<CityEntity>(BATCH_SIZE)

                while (iterator.hasNext()) {
                    val row = iterator.next().trim()
                    if (row.isEmpty()) continue

                    parseCsvRow(row)?.let { columns ->
                        val cityName = columns.getOrNull(cityIndex)?.trim().orEmpty()
                        val lat = columns.getOrNull(latIndex)?.toDoubleOrNull()
                        val lon = columns.getOrNull(lonIndex)?.toDoubleOrNull()
                        val countryCode = columns.getOrNull(countryIndex)?.trim().orEmpty()

                        if (cityName.isNotEmpty() && lat != null && lon != null && countryCode.isNotEmpty()) {
                            batch.add(
                                CityEntity(
                                    name = cityName,
                                    lat = lat,
                                    lon = lon,
                                    countryCode = countryCode
                                )
                            )
                        }
                    }

                    if (batch.size >= BATCH_SIZE) {
                        dao.insertAll(batch.toList())
                        batch.clear()
                    }
                }

                if (batch.isNotEmpty()) {
                    dao.insertAll(batch)
                }
            }
        }.getOrElse {
            dao.insertAll(fallbackCities)
        }
    }

    private fun parseCsvRow(row: String): List<String>? {
        val columns = mutableListOf<String>()
        val current = StringBuilder()
        var inQuotes = false
        var index = 0

        while (index < row.length) {
            val char = row[index]
            when {
                char == '"' -> {
                    if (inQuotes && index + 1 < row.length && row[index + 1] == '"') {
                        current.append('"')
                        index++
                    } else {
                        inQuotes = !inQuotes
                    }
                }
                char == ',' && !inQuotes -> {
                    columns.add(current.toString())
                    current.setLength(0)
                }
                else -> current.append(char)
            }
            index++
        }

        columns.add(current.toString())
        return columns
    }
}
