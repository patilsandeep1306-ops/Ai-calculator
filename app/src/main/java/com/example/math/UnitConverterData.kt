package com.example.math

import com.example.model.ConversionUnit
import com.example.model.UnitCategoryType

object UnitConverterData {

    val categories: Map<UnitCategoryType, List<ConversionUnit>> = mapOf(
        UnitCategoryType.LENGTH to listOf(
            ConversionUnit("m", "Meter", "m", 1.0),
            ConversionUnit("km", "Kilometer", "km", 1000.0),
            ConversionUnit("cm", "Centimeter", "cm", 0.01),
            ConversionUnit("mm", "Millimeter", "mm", 0.001),
            ConversionUnit("mi", "Mile", "mi", 1609.344),
            ConversionUnit("yd", "Yard", "yd", 0.9144),
            ConversionUnit("ft", "Foot", "ft", 0.3048),
            ConversionUnit("in", "Inch", "in", 0.0254),
            ConversionUnit("nmi", "Nautical Mile", "nmi", 1852.0)
        ),
        UnitCategoryType.WEIGHT to listOf(
            ConversionUnit("kg", "Kilogram", "kg", 1.0),
            ConversionUnit("g", "Gram", "g", 0.001),
            ConversionUnit("mg", "Milligram", "mg", 0.000001),
            ConversionUnit("lb", "Pound", "lb", 0.45359237),
            ConversionUnit("oz", "Ounce", "oz", 0.0283495231),
            ConversionUnit("ton", "Metric Ton", "t", 1000.0)
        ),
        UnitCategoryType.TEMPERATURE to listOf(
            ConversionUnit("c", "Celsius", "°C", 1.0,
                customToFactor = { it },
                customFromFactor = { it }
            ),
            ConversionUnit("f", "Fahrenheit", "°F", 1.0,
                customToFactor = { (it - 32.0) * (5.0 / 9.0) }, // to Celsius
                customFromFactor = { it * (9.0 / 5.0) + 32.0 }  // from Celsius
            ),
            ConversionUnit("k", "Kelvin", "K", 1.0,
                customToFactor = { it - 273.15 },
                customFromFactor = { it + 273.15 }
            )
        ),
        UnitCategoryType.SPEED to listOf(
            ConversionUnit("ms", "Meters per second", "m/s", 1.0),
            ConversionUnit("kmh", "Kilometers per hour", "km/h", 1.0 / 3.6),
            ConversionUnit("mph", "Miles per hour", "mph", 0.44704),
            ConversionUnit("knot", "Knot", "kn", 0.514444),
            ConversionUnit("fts", "Feet per second", "ft/s", 0.3048)
        ),
        UnitCategoryType.AREA to listOf(
            ConversionUnit("sqm", "Square Meter", "m²", 1.0),
            ConversionUnit("sqkm", "Square Kilometer", "km²", 1_000_000.0),
            ConversionUnit("sqft", "Square Foot", "ft²", 0.092903),
            ConversionUnit("acre", "Acre", "ac", 4046.86),
            ConversionUnit("ha", "Hectare", "ha", 10000.0),
            ConversionUnit("sqmi", "Square Mile", "mi²", 2_589_988.11)
        ),
        UnitCategoryType.VOLUME to listOf(
            ConversionUnit("l", "Liter", "L", 1.0),
            ConversionUnit("ml", "Milliliter", "mL", 0.001),
            ConversionUnit("cum", "Cubic Meter", "m³", 1000.0),
            ConversionUnit("gal", "Gallon (US)", "gal", 3.78541),
            ConversionUnit("qt", "Quart (US)", "qt", 0.946353),
            ConversionUnit("pt", "Pint (US)", "pt", 0.473176),
            ConversionUnit("floz", "Fluid Ounce (US)", "fl oz", 0.0295735)
        ),
        UnitCategoryType.DATA to listOf(
            ConversionUnit("b", "Byte", "B", 1.0),
            ConversionUnit("kb", "Kilobyte", "KB", 1024.0),
            ConversionUnit("mb", "Megabyte", "MB", 1024.0 * 1024.0),
            ConversionUnit("gb", "Gigabyte", "GB", 1024.0 * 1024.0 * 1024.0),
            ConversionUnit("tb", "Terabyte", "TB", 1024.0 * 1024.0 * 1024.0 * 1024.0)
        ),
        UnitCategoryType.TIME to listOf(
            ConversionUnit("s", "Second", "s", 1.0),
            ConversionUnit("ms", "Millisecond", "ms", 0.001),
            ConversionUnit("min", "Minute", "min", 60.0),
            ConversionUnit("h", "Hour", "hr", 3600.0),
            ConversionUnit("d", "Day", "d", 86400.0),
            ConversionUnit("wk", "Week", "wk", 604800.0),
            ConversionUnit("yr", "Year", "yr", 31536000.0)
        ),
        UnitCategoryType.ENERGY to listOf(
            ConversionUnit("j", "Joule", "J", 1.0),
            ConversionUnit("kj", "Kilojoule", "kJ", 1000.0),
            ConversionUnit("cal", "Calorie", "cal", 4.184),
            ConversionUnit("kcal", "Kilocalorie", "kcal", 4184.0),
            ConversionUnit("wh", "Watt-hour", "Wh", 3600.0),
            ConversionUnit("kwh", "Kilowatt-hour", "kWh", 3_600_000.0),
            ConversionUnit("btu", "BTU", "BTU", 1055.06)
        )
    )

    fun convert(
        value: Double,
        fromUnit: ConversionUnit,
        toUnit: ConversionUnit
    ): Double {
        if (fromUnit.id == toUnit.id) return value

        // Custom non-linear conversions (e.g. Temperature)
        if (fromUnit.customToFactor != null && toUnit.customFromFactor != null) {
            val baseVal = fromUnit.customToFactor.invoke(value)
            return toUnit.customFromFactor.invoke(baseVal)
        }

        // Standard linear conversions
        val baseVal = value * fromUnit.factorToBase
        return baseVal / toUnit.factorToBase
    }
}
