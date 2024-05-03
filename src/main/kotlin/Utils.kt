class Utils {
}

fun String.toBooleanFromBinary(): Boolean {
    return this == "1"
}

fun isNumeric(value: String): Boolean{
    return !(value.toIntOrNull() == null && value.toDoubleOrNull() == null)
}