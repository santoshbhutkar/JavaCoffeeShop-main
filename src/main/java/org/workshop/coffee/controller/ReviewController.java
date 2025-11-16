package org.workshop.coffee.controller;

import org.workshop.coffee.domain.Product;
import org.workshop.coffee.domain.Review;
import org.workshop.coffee.service.ProductService;
import org.workshop.coffee.service.PersonService;
import org.workshop.coffee.service.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/reviews")
public class ReviewController {

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private ProductService productService;

    @Autowired
    private PersonService personService;

    @ModelAttribute("products")
    public List<Product> populateProducts() {
        return productService.getAllProducts();
    }

    @GetMapping("/product/{productId}")
    public String showProductReviews(@PathVariable Long productId, Model model) {
        Product product = productService.getProduct(productId);
        if (product == null) {
            return "redirect:/";
        }
        List<Review> reviews = reviewService.getReviewsByProduct(product);
        Double averageRating = reviewService.getAverageRating(product);
        
        model.addAttribute("product", product);
        model.addAttribute("reviews", reviews);
        model.addAttribute("averageRating", averageRating);
        return "review/list";
    }

    @GetMapping("/add")
    public String showReviewAddForm(Model model) {
        model.addAttribute("review", new Review());
        return "review/add";
    }

    @GetMapping("/add/product/{productId}")
    public String showReviewAddFormForProduct(@PathVariable Long productId, Model model) {
        Product product = productService.getProduct(productId);
        if (product == null) {
            return "redirect:/";
        }
        Review review = new Review();
        review.setProduct(product);
        model.addAttribute("review", review);
        return "review/add";
    }

    @PostMapping("/add")
    public String saveReview(@Valid Review review, BindingResult result, Model model, 
                            Principal principal, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "review/add";
        }

        try {
            // Set the person from the authenticated user
            review.setPerson(personService.findByUsername(principal.getName()));
            
            // Ensure review date is set
            if (review.getReviewDate() == null) {
                review.setReviewDate(new java.util.Date());
            }

            reviewService.save(review);

            redirectAttributes.addFlashAttribute("message", "Your review has been submitted successfully.");
            return "redirect:/reviews/product/" + review.getProduct().getId();
        } catch (Exception e) {
            result.reject(null, "Error saving review: " + e.getMessage());
            return "review/add";
        }
    }

    @GetMapping("/list")
    public String listAllReviews(Model model) {
        model.addAttribute("reviews", reviewService.getAllReviews());
        return "review/list";
    }

    @GetMapping("/delete/{id}")
    public String deleteReview(@PathVariable Long id, Principal principal, RedirectAttributes redirectAttributes) {
        Review review = reviewService.getReview(id);
        if (review == null) {
            redirectAttributes.addFlashAttribute("error", "Review not found.");
            return "redirect:/reviews/list";
        }

        // Check if user is admin or the review owner
        String username = principal.getName();
        boolean isAdmin = personService.findByUsername(username).getRoles() == org.workshop.coffee.domain.Role.ROLE_ADMIN;
        boolean isOwner = review.getPerson().getUsername().equals(username);

        if (isAdmin || isOwner) {
            Long productId = review.getProduct().getId();
            reviewService.delete(id);
            redirectAttributes.addFlashAttribute("message", "Review deleted successfully.");
            return "redirect:/reviews/product/" + productId;
        } else {
            redirectAttributes.addFlashAttribute("error", "You don't have permission to delete this review.");
            return "redirect:/reviews/product/" + review.getProduct().getId();
        }
    }
}

