package org.workshop.coffee.service;

import org.workshop.coffee.domain.Product;
import org.workshop.coffee.domain.Review;
import org.workshop.coffee.repository.ReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReviewService {

    @Autowired
    private ReviewRepository reviewRepository;

    public Review save(Review review) {
        if (review.getReviewDate() == null) {
            review.setReviewDate(new java.util.Date());
        }
        return reviewRepository.save(review);
    }

    public void delete(Long reviewId) {
        reviewRepository.deleteById(reviewId);
    }

    public Review getReview(Long reviewId) {
        return reviewRepository.findById(reviewId).orElse(null);
    }

    public List<Review> getReviewsByProduct(Product product) {
        return reviewRepository.findByProductOrderByReviewDateDesc(product);
    }

    public List<Review> getAllReviews() {
        return reviewRepository.findAll();
    }

    public Double getAverageRating(Product product) {
        List<Review> reviews = reviewRepository.findByProduct(product);
        if (reviews.isEmpty()) {
            return 0.0;
        }
        return reviews.stream()
                .mapToInt(Review::getRating)
                .average()
                .orElse(0.0);
    }

}

