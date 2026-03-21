package dto.dashboard;

/**
 * DTO item for category distribution chart.
 */
public class DashboardCategoryStatDto {

    private String name;
    private Long value;

    public DashboardCategoryStatDto() {
    }

    public DashboardCategoryStatDto(String name, Long value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getValue() {
        return value;
    }

    public void setValue(Long value) {
        this.value = value;
    }
}
