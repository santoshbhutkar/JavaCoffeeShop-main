package org.workshop.coffee.repository;

import org.workshop.coffee.domain.LoyaltyPoints;
import org.workshop.coffee.domain.Person;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LoyaltyPointsRepository extends JpaRepository<LoyaltyPoints, Long> {

    Optional<LoyaltyPoints> findByPerson(Person person);

    Optional<LoyaltyPoints> findByPersonId(Long personId);

}

