package utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import jakarta.servlet.http.HttpServletResponse;
import org.hibernate.Hibernate;
import org.hibernate.proxy.HibernateProxy;

import java.lang.reflect.Type;

/**
 * Utility class for handling HTTP requests and responses, particularly for JSON
 * data
 */
public class HttpUtil {

    /**
     * Gson instance for JSON serialization and deserialization
     */
    private static final com.google.gson.Gson gson = new GsonBuilder()
            .registerTypeAdapter(Instant.class, (JsonSerializer<Instant>) (src, typeOfSrc, context) ->
                    new JsonPrimitive(src.toString()))
            .registerTypeAdapter(LocalDate.class, (JsonSerializer<LocalDate>) (src, typeOfSrc, context) ->
                    new JsonPrimitive(src.format(DateTimeFormatter.ISO_LOCAL_DATE)))
            .registerTypeAdapter(LocalDate.class, (JsonDeserializer<LocalDate>) (json, typeOfT, context) ->
                    LocalDate.parse(json.getAsString(), DateTimeFormatter.ISO_LOCAL_DATE))
            .registerTypeAdapter(OffsetDateTime.class, (JsonSerializer<OffsetDateTime>) (src, typeOfSrc, context) ->
                    new JsonPrimitive(src.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)))
            .registerTypeAdapter(OffsetDateTime.class, (JsonDeserializer<OffsetDateTime>) (json, typeOfT, context) ->
                    OffsetDateTime.parse(json.getAsString(), DateTimeFormatter.ISO_OFFSET_DATE_TIME))
            .registerTypeHierarchyAdapter(HibernateProxy.class, new JsonSerializer<HibernateProxy>() {
                @Override
                public JsonElement serialize(HibernateProxy src, Type typeOfSrc, JsonSerializationContext context) {
                    // Force initialization and unproxy the object
                    return context.serialize(Hibernate.unproxy(src));
                }
            })
            .create();

    /**
     * Deserialize JSON from BufferedReader to an object of specified Java
     * class.
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
