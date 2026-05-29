package com.barteqcz.loqa.location

import android.location.Address

object AddressRefiner {

    private val CITY_PART_SPLIT_REGEX = Regex("[-–/](?=\\p{Ll})")
    private val FORBIDDEN_WORDS = setOf(
        "district", "okres", "kraj", "mesto", "město", "powiat", "wojewodztwo"
    )

    fun findBestCityCandidate(addresses: List<Address>): String? {
        val firstAddress = addresses.firstOrNull() ?: return null
        val province = firstAddress.adminArea ?: ""
        val district = firstAddress.subAdminArea ?: ""

        val cityFields = addresses.flatMap { addr -> listOfNotNull(addr.locality, addr.subLocality) }
        val cityParts = cityFields.flatMap { it.split(CITY_PART_SPLIT_REGEX) }
        val districtParts = district.split(CITY_PART_SPLIT_REGEX)

        val allCandidates = (cityFields + cityParts + districtParts)
            .filter { part ->
                val p = part.lowercase()
                p.length > 2 &&
                !p.equals(province, ignoreCase = true) &&
                !p.all { it.isDigit() } &&
                p !in FORBIDDEN_WORDS
            }
            .distinct()

        return allCandidates.maxByOrNull { candidate ->
            var score = 0.0
            val appearedInCityFields = cityFields.any { it.contains(candidate, ignoreCase = true) }
            val isMainLocality = addresses.firstOrNull()?.locality?.contains(candidate, ignoreCase = true) == true
            
            if (appearedInCityFields) score += 5.0
            if (isMainLocality) score += 3.0
            if (!district.contains(candidate, ignoreCase = true)) score += 1.0
            
            score
        }
    }

    fun cleanCityName(name: String?): String? {
        if (name == null) return null
        
        return name.split(CITY_PART_SPLIT_REGEX)
            .firstOrNull { it.length > 2 }
            ?.replace(Regex("\\d+"), "")
            ?.trim()
            ?.takeIf { it.length > 2 }
    }
}
