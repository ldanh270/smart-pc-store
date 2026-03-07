package entities;

import jakarta.persistence.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "\"DemandForecasts\"")
public class DemandForecast {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "\"productId\"")
    private Product product;

    @Column(name = "\"forecastDate\"")
    private LocalDate forecastDate;

    @Column(name = "\"predictedQuantity\"")
    private Integer predictedQuantity;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public LocalDate getForecastDate() {
        return forecastDate;
    }

    public void setForecastDate(LocalDate forecastDate) {
        this.forecastDate = forecastDate;
    }

    public Integer getPredictedQuantity() {
        return predictedQuantity;
    }

    public void setPredictedQuantity(Integer predictedQuantity) {
        this.predictedQuantity = predictedQuantity;
    }

}
