package org.workshop.coffee.domain;

import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "LoyaltyPointsTransactions")
public class LoyaltyPointsTransaction {

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne
    @JoinColumn(name = "person_id", nullable = false)
    private Person person;

    @Column(nullable = false)
    private Integer points;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType type;

    @Column(length = 500)
    private String description;

    @Temporal(TemporalType.TIMESTAMP)
    @DateTimeFormat(pattern = "dd/MM/yyyy HH:mm")
    @Column(nullable = false)
    private Date transactionDate;

    @ManyToOne
    @JoinColumn(name = "order_id")
    private Order order;

    public enum TransactionType {
        EARNED, REDEEMED, ADJUSTED
    }

    public LoyaltyPointsTransaction() {
        super();
        this.transactionDate = new Date();
    }

    public LoyaltyPointsTransaction(Person person, Integer points, TransactionType type, String description) {
        super();
        this.person = person;
        this.points = points;
        this.type = type;
        this.description = description;
        this.transactionDate = new Date();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Person getPerson() {
        return person;
    }

    public void setPerson(Person person) {
        this.person = person;
    }

    public Integer getPoints() {
        return points;
    }

    public void setPoints(Integer points) {
        this.points = points;
    }

    public TransactionType getType() {
        return type;
    }

    public void setType(TransactionType type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(Date transactionDate) {
        this.transactionDate = transactionDate;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    @Override
    public String toString() {
        return "LoyaltyPointsTransaction{" +
                "id=" + id +
                ", person=" + (person != null ? person.getUsername() : null) +
                ", points=" + points +
                ", type=" + type +
                ", description='" + description + '\'' +
                ", transactionDate=" + transactionDate +
                '}';
    }
}

