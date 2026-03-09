package utils;

/**
 * Utility class for number operations
 */
public class NumberUtil {

    /**
     * Check if a string is numeric
     *
     * @param str the string to check
     * @return true if the string is numeric, false otherwise
     */
    public static boolean isNumeric(String str) {
        if (str == null || str.isEmpty()) {
            return false;
        }
        return str.matches("\\d+");
    }
}
