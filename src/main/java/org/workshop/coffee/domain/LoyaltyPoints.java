package org.workshop.coffee.domain;

import javax.persistence.*;

@Entity
@Table(name = "LoyaltyPoints")
public class LoyaltyPoints {

    @Id
    @GeneratedValue
    private Long id;

    @OneToOne
    @JoinColumn(name = "person_id", unique = true)
    private Person person;

    @Column(nullable = false)
    private Integer balance = 0;

    public LoyaltyPoints() {
        super();
    }

    public LoyaltyPoints(Person person) {
        super();
        this.person = person;
        this.balance = 0;
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

    public Integer getBalance() {
        return balance;
    }

    public void setBalance(Integer balance) {
        this.balance = balance;
    }

    public void addPoints(Integer points) {
        this.balance += points;
    }

    public void deductPoints(Integer points) {
        if (this.balance >= points) {
            this.balance -= points;
        }
    }

    @Override
    public String toString() {
        return "LoyaltyPoints{" +
                "id=" + id +
                ", person=" + (person != null ? person.getUsername() : null) +
                ", balance=" + balance +
                '}';
    }
}

