package org.workshop.coffee.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.workshop.coffee.service.LoyaltyPointsService;
import org.workshop.coffee.service.PersonService;

@ControllerAdvice
public class LoyaltyPointsModelAttribute {

    @Autowired
    private LoyaltyPointsService loyaltyPointsService;

    @Autowired
    private PersonService personService;

    @ModelAttribute
    public void addLoyaltyPointsBalance(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && 
            !authentication.getName().equals("anonymousUser")) {
            try {
                var person = personService.findByUsername(authentication.getName());
                if (person != null) {
                    Integer balance = loyaltyPointsService.getBalance(person);
                    model.addAttribute("loyaltyPointsBalance", balance);
                }
            } catch (Exception e) {
                // Silently handle - user might not exist or service unavailable
                model.addAttribute("loyaltyPointsBalance", 0);
            }
        }
    }
}

