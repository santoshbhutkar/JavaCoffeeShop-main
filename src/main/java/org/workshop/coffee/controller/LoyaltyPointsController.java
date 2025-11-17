package org.workshop.coffee.controller;

import org.workshop.coffee.domain.LoyaltyPointsTransaction;
import org.workshop.coffee.service.LoyaltyPointsService;
import org.workshop.coffee.service.PersonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/loyalty")
public class LoyaltyPointsController {

    @Autowired
    private LoyaltyPointsService loyaltyPointsService;

    @Autowired
    private PersonService personService;

    @GetMapping("/points")
    public String showLoyaltyPoints(Model model, Principal principal) {
        var person = personService.findByUsername(principal.getName());
        Integer balance = loyaltyPointsService.getBalance(person);
        List<LoyaltyPointsTransaction> transactions = loyaltyPointsService.getTransactionHistory(person);

        model.addAttribute("balance", balance);
        model.addAttribute("transactions", transactions);
        model.addAttribute("pointsPerDollar", 10);
        model.addAttribute("pointsForRedemption", 100);
        model.addAttribute("discountValue", loyaltyPointsService.calculateDiscountFromPoints(balance));

        return "loyalty/points";
    }

    @PostMapping("/redeem")
    public String redeemPoints(@RequestParam Integer pointsToRedeem, 
                              Principal principal, 
                              RedirectAttributes redirectAttributes) {
        if (pointsToRedeem == null || pointsToRedeem <= 0) {
            redirectAttributes.addFlashAttribute("error", "Invalid points amount.");
            return "redirect:/loyalty/points";
        }

        var person = personService.findByUsername(principal.getName());
        Integer currentBalance = loyaltyPointsService.getBalance(person);

        if (pointsToRedeem > currentBalance) {
            redirectAttributes.addFlashAttribute("error", 
                "Insufficient points. You have " + currentBalance + " points available.");
            return "redirect:/loyalty/points";
        }

        Double discountAmount = loyaltyPointsService.redeemPoints(person, pointsToRedeem);

        if (discountAmount > 0) {
            redirectAttributes.addFlashAttribute("message", 
                "Successfully redeemed " + pointsToRedeem + " points for $" + 
                String.format("%.2f", discountAmount) + " discount. You can use this discount on your next order.");
        } else {
            redirectAttributes.addFlashAttribute("error", "Failed to redeem points.");
        }

        return "redirect:/loyalty/points";
    }
}

