package dto.order;

import java.util.List;

public class OrderViewResponseDto {

    private OrderResponseDto order;
    private List<OrderDetailDto> items;
    private String qrCode;

    public OrderResponseDto getOrder() {
        return order;
    }

    public void setOrder(OrderResponseDto order) {
        this.order = order;
    }

    public List<OrderDetailDto> getItems() {
        return items;
    }

    public void setItems(List<OrderDetailDto> items) {
        this.items = items;
    }

    public String getQrCode() {
        return qrCode;
    }

    public void setQrCode(String qrCode) {
        this.qrCode = qrCode;
    }
}
