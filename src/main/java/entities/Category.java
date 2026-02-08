package entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.Nationalized;

@Entity
@Table(name = "Categories")
public class Category {
    @Id
    @Column(name = "Id", nullable = false)
    private Integer id;

    @Nationalized
    @Column(name = "CategoryName")
    private String categoryName;

    @Nationalized
    @Column(name = "Description")
    private String description;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

}