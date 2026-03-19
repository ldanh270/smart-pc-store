package utils;

import java.text.Normalizer;
import java.util.regex.Pattern;

/**
 * Utility class for generating URL-friendly slugs from text.
 * Supports Vietnamese diacritics removal.
 */
public class SlugUtil {

    private SlugUtil() {
        // Utility class — no instantiation
    }

    /**
     * Generate a slug from the given text.
     * Steps:
     * 1. Normalize Unicode (NFD) to decompose diacritics.
     * 2. Remove non-ASCII chars (diacritics, marks).
     * 3. Lowercase.
     * 4. Replace spaces and non-alphanumeric chars with hyphens.
     * 5. Collapse consecutive hyphens.
     * 6. Trim leading/trailing hyphens.
     *
     * @param text The input text (e.g. product name).
     * @return A URL-safe slug string.
     */
    public static String toSlug(String text) {
        if (text == null || text.isBlank()) {
            return "";
        }

        // Normalize and strip diacritics (handles Vietnamese characters)
        String normalized = Normalizer.normalize(text, Normalizer.Form.NFD);
        Pattern nonAscii = Pattern.compile("[^\\p{ASCII}]");
        String ascii = nonAscii.matcher(normalized).replaceAll("");

        return ascii
                .toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")   // remove invalid chars
                .replaceAll("[\\s]+", "-")          // spaces -> hyphens
                .replaceAll("-{2,}", "-")           // collapse consecutive hyphens
                .replaceAll("^-|-$", "");           // trim edge hyphens
    }
}
