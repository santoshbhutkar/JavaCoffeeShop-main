package org.workshop.coffee.domain;

import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Date;

@Entity
public class Review {

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne
    @NotNull(message = "Product cannot be empty")
    private Product product;

    @ManyToOne
    @NotNull(message = "Reviewer cannot be empty")
    private Person person;

    @NotNull(message = "Rating cannot be empty")
    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 5, message = "Rating must be at most 5")
    private Integer rating;

    @Size(max = 1000, message = "Comment cannot exceed 1000 characters")
    private String comment;

    @Temporal(TemporalType.TIMESTAMP)
    @DateTimeFormat(pattern = "dd/MM/yyyy HH:mm")
    private Date reviewDate;

    public Review() {
        super();
        this.reviewDate = new Date();
    }

    public Review(Product product, Person person, Integer rating, String comment) {
        super();
        this.product = product;
        this.person = person;
        this.rating = rating;
        this.comment = comment;
        this.reviewDate = new Date();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public Person getPerson() {
        return person;
    }

    public void setPerson(Person person) {
        this.person = person;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Date getReviewDate() {
        return reviewDate;
    }

    public void setReviewDate(Date reviewDate) {
        this.reviewDate = reviewDate;
    }

    @Override
    public String toString() {
        return "Review{" +
                "id=" + id +
                ", product=" + (product != null ? product.getProductName() : null) +
                ", person=" + (person != null ? person.getUsername() : null) +
                ", rating=" + rating +
                ", comment='" + comment + '\'' +
                ", reviewDate=" + reviewDate +
                '}';
    }
}

