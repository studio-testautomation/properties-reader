package in.testautomationstudio.commons.util;

public final class PlaceholderResolver {
    private static final String PLACEHOLDER_START = "${";
    private static final String PLACEHOLDER_END = "}";

    private PlaceholderResolver() {
    }

    public static String resolvePlaceholders(String text) {
        if (text == null) return null;
        StringBuilder sb = new StringBuilder();
        int i = 0;
        while (i < text.length()) {
            int start = text.indexOf(PLACEHOLDER_START, i);
            if (start == -1) {
                sb.append(text.substring(i));
                break;
            }
            sb.append(text, i, start);
            int end = text.indexOf(PLACEHOLDER_END, start);
            if (end == -1) {
                sb.append(text.substring(start));
                break;
            }
            String key = text.substring(start + 2, end);
            String value = System.getProperty(key);
            if (value == null) {
                value = System.getenv(key);
            }
            if (value == null) {
                throw new IllegalArgumentException("No environment variable or system property found for placeholder: " + key + " in filePath: " + text);
            }
            sb.append(value);
            i = end + 1;
        }
        return sb.toString();
    }
}
