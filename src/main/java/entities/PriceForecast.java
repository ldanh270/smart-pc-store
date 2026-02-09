package entities;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "PriceForecasts")
public class PriceForecast {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ProductId")
    private Product product;

    @Column(name = "ForecastDate")
    private LocalDate forecastDate;

    @Column(name = "PredictedPrice", precision = 18, scale = 2)
    private BigDecimal predictedPrice;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
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

    public BigDecimal getPredictedPrice() {
        return predictedPrice;
    }

    public void setPredictedPrice(BigDecimal predictedPrice) {
        this.predictedPrice = predictedPrice;
    }

}
