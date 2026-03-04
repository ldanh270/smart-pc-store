package dto.order;

import java.util.List;

public class OrderViewResponseDto {

    private OrderResponseDto order;
    private List<OrderDetailDto> items;

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
}
