package org.workshop.coffee.repository;

import org.workshop.coffee.domain.LoyaltyPointsTransaction;
import org.workshop.coffee.domain.Person;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LoyaltyPointsTransactionRepository extends JpaRepository<LoyaltyPointsTransaction, Long> {

    List<LoyaltyPointsTransaction> findByPersonOrderByTransactionDateDesc(Person person);

    List<LoyaltyPointsTransaction> findByPersonIdOrderByTransactionDateDesc(Long personId);

}

