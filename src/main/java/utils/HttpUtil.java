package utils;

import com.google.gson.Gson;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

public class HttpUtil {

    private static final Gson gson = new Gson();

    public static void sendJson(HttpServletResponse resp, int statusCode, String json) throws IOException {
        resp.setStatus(statusCode);
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        resp.getWriter().write(json);
    }

    public static void sendJson(HttpServletResponse resp, int statusCode, Object object) throws IOException {
        sendJson(resp, statusCode, gson.toJson(object));
    }
}
