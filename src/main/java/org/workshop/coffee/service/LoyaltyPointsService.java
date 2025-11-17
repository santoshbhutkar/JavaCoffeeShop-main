package org.workshop.coffee.service;

import org.workshop.coffee.domain.LoyaltyPoints;
import org.workshop.coffee.domain.LoyaltyPointsTransaction;
import org.workshop.coffee.domain.Order;
import org.workshop.coffee.domain.Person;
import org.workshop.coffee.repository.LoyaltyPointsRepository;
import org.workshop.coffee.repository.LoyaltyPointsTransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class LoyaltyPointsService {

    private static final int POINTS_PER_DOLLAR = 10; // Earn 10 points per $1 spent
    private static final int POINTS_FOR_REDEMPTION = 100; // 100 points = $1 discount

    @Autowired
    private LoyaltyPointsRepository loyaltyPointsRepository;

    @Autowired
    private LoyaltyPointsTransactionRepository transactionRepository;

    /**
     * Get or create loyalty points account for a person
     */
    public LoyaltyPoints getOrCreateLoyaltyPoints(Person person) {
        return loyaltyPointsRepository.findByPerson(person)
                .orElseGet(() -> {
                    LoyaltyPoints lp = new LoyaltyPoints(person);
                    return loyaltyPointsRepository.save(lp);
                });
    }

    /**
     * Get current points balance for a person
     */
    public Integer getBalance(Person person) {
        LoyaltyPoints lp = getOrCreateLoyaltyPoints(person);
        return lp.getBalance();
    }

    /**
     * Earn points from an order
     * Points are calculated as: order total * POINTS_PER_DOLLAR
     */
    @Transactional
    public void earnPointsFromOrder(Order order) {
        if (order.getPerson() == null || order.getTotalAmount() <= 0) {
            return;
        }

        Person person = order.getPerson();
        int pointsEarned = (int) Math.round(order.getTotalAmount() * POINTS_PER_DOLLAR);

        if (pointsEarned > 0) {
            LoyaltyPoints lp = getOrCreateLoyaltyPoints(person);
            lp.addPoints(pointsEarned);
            loyaltyPointsRepository.save(lp);

            // Create transaction record
            LoyaltyPointsTransaction transaction = new LoyaltyPointsTransaction(
                    person,
                    pointsEarned,
                    LoyaltyPointsTransaction.TransactionType.EARNED,
                    "Earned from Order #" + order.getId() + " ($" + String.format("%.2f", order.getTotalAmount()) + ")"
            );
            transaction.setOrder(order);
            transactionRepository.save(transaction);
        }
    }

    /**
     * Redeem points for discount
     * @param person The person redeeming points
     * @param pointsToRedeem Number of points to redeem
     * @return Discount amount in dollars, or 0 if insufficient points
     */
    @Transactional
    public Double redeemPoints(Person person, Integer pointsToRedeem) {
        if (pointsToRedeem <= 0) {
            return 0.0;
        }

        LoyaltyPoints lp = getOrCreateLoyaltyPoints(person);
        
        if (lp.getBalance() < pointsToRedeem) {
            return 0.0; // Insufficient points
        }

        lp.deductPoints(pointsToRedeem);
        loyaltyPointsRepository.save(lp);

        double discountAmount = pointsToRedeem / (double) POINTS_FOR_REDEMPTION;

        // Create transaction record
        LoyaltyPointsTransaction transaction = new LoyaltyPointsTransaction(
                person,
                -pointsToRedeem,
                LoyaltyPointsTransaction.TransactionType.REDEEMED,
                "Redeemed " + pointsToRedeem + " points for $" + String.format("%.2f", discountAmount) + " discount"
        );
        transactionRepository.save(transaction);

        return discountAmount;
    }

    /**
     * Get transaction history for a person
     */
    public List<LoyaltyPointsTransaction> getTransactionHistory(Person person) {
        return transactionRepository.findByPersonOrderByTransactionDateDesc(person);
    }

    /**
     * Calculate discount amount from points
     */
    public Double calculateDiscountFromPoints(Integer points) {
        return points / (double) POINTS_FOR_REDEMPTION;
    }

    /**
     * Get points required for a specific discount amount
     */
    public Integer getPointsRequiredForDiscount(Double discountAmount) {
        return (int) Math.ceil(discountAmount * POINTS_FOR_REDEMPTION);
    }
}

