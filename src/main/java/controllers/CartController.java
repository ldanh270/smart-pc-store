package controllers;

import dto.cart.AddToCartRequestDto;
import dto.cart.CartItemResponseDto;
import dto.cart.UpdateCartItemRequestDto;
import services.CartService;
import utils.HttpUtil;
import utils.JwtUtil;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    public void handleGetCart(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            Integer userId = JwtUtil.getUserIdFromAuthorizationHeader(req.getHeader("Authorization"));
            List<CartItemResponseDto> items = cartService.getMyCart(userId);
            HttpUtil.sendJson(resp, HttpServletResponse.SC_OK, items);
        } catch (Exception e) {
            HttpUtil.sendJson(resp, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        }
    }

    public void handleAddToCart(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            Integer userId = JwtUtil.getUserIdFromAuthorizationHeader(req.getHeader("Authorization"));
            AddToCartRequestDto dto = HttpUtil.jsonToClass(req.getReader(), AddToCartRequestDto.class);

            cartService.addToCart(userId, dto.getProductId(), dto.getQuantity());
            HttpUtil.sendJson(resp, HttpServletResponse.SC_CREATED, "Product added to cart successfully");
        } catch (Exception e) {
            HttpUtil.sendJson(resp, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        }
    }

    public void handleUpdateQuantity(HttpServletRequest req, HttpServletResponse resp, Integer cartItemId)
            throws IOException {
        try {
            Integer userId = JwtUtil.getUserIdFromAuthorizationHeader(req.getHeader("Authorization"));
            UpdateCartItemRequestDto dto = HttpUtil.jsonToClass(req.getReader(), UpdateCartItemRequestDto.class);

            cartService.updateQuantity(userId, cartItemId, dto.getQuantity());
            HttpUtil.sendJson(resp, HttpServletResponse.SC_OK, "Cart item updated successfully");
        } catch (Exception e) {
            HttpUtil.sendJson(resp, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        }
    }

    public void handleRemoveItem(HttpServletRequest req, HttpServletResponse resp, Integer cartItemId)
            throws IOException {
        try {
            Integer userId = JwtUtil.getUserIdFromAuthorizationHeader(req.getHeader("Authorization"));
            cartService.removeItem(userId, cartItemId);
            HttpUtil.sendJson(resp, HttpServletResponse.SC_OK, "Cart item removed successfully");
        } catch (Exception e) {
            HttpUtil.sendJson(resp, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        }
    }

    public void handleClearCart(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            Integer userId = JwtUtil.getUserIdFromAuthorizationHeader(req.getHeader("Authorization"));
            cartService.clearCart(userId);
            HttpUtil.sendJson(resp, HttpServletResponse.SC_OK, "Cart cleared successfully");
        } catch (Exception e) {
            HttpUtil.sendJson(resp, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        }
    }
}
