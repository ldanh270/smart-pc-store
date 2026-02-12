package utils;

import com.google.gson.Gson;
import jakarta.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.IOException;

/**
 * Utility class for handling HTTP requests and responses, particularly for JSON data.
 */
public class HttpUtil {
    /**
     * Gson instance for JSON serialization and deserialization.
     */
    private static final Gson gson = new Gson();

    /**
     * Deserialize JSON from BufferedReader to an object of specified Java class.
     *
     * @param reader the BufferedReader containing JSON data
     * @param tClass the class of the object to deserialize to
     * @param <T>    the type of the object
     * @return the deserialized object
     */
    public static <T> T jsonToClass(BufferedReader reader, Class<T> tClass) {
        return gson.fromJson(reader, tClass);
    }

    /**
     * Send a JSON response with specified status code and data.
     *
     * @param resp   the HttpServletResponse to send the response
     * @param status the HTTP status code
     * @param data   the data object to serialize to JSON
     * @throws IOException if an input or output exception occurs
     */
    public static void sendJson(HttpServletResponse resp, int status, Object data) throws IOException {
        resp.setStatus(status);
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        resp.getWriter().write(gson.toJson(data));
        resp.getWriter().flush();
    }

    public static void sendJson(HttpServletResponse resp, int status) throws IOException {
        resp.setStatus(status);
        resp.getWriter().flush();
    }
}
