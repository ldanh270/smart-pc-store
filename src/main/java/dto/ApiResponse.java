package dto;

public class ApiResponse<T> {
    public ApiResponse(boolean success, String message, T data) {
    }

    public ApiResponse(boolean success, String message) {
    }
}
