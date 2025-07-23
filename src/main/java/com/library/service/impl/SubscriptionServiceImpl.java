package com.library.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.library.enums.SubscriptionType;
import com.library.model.Subscription;
import com.library.model.User;
import com.library.repository.SubscriptionRepository;
import com.library.service.SubscriptionService;

@Service
public class SubscriptionServiceImpl implements SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;

    @Autowired
    public SubscriptionServiceImpl(SubscriptionRepository subscriptionRepository) {
        this.subscriptionRepository = subscriptionRepository;
    }
    
    @Override
    public Optional<Subscription> getSubscriptionById(Long id) {
        return subscriptionRepository.findById(id);
    }

    @Override
    @Transactional
    public Subscription createSubscription(User user, SubscriptionType subscriptionType) {
        // Cancel any existing active subscriptions
        Optional<Subscription> existingSubscription = getActiveSubscription(user);
        existingSubscription.ifPresent(subscription -> {
            subscription.setActive(false);
            subscriptionRepository.save(subscription);
        });

        // Create new subscription
        LocalDateTime startDate = LocalDateTime.now();
        LocalDateTime endDate = startDate.plusDays(subscriptionType.getValidityDays());
        int booksQuota = subscriptionType == SubscriptionType.PREMIUM ? 
                Integer.MAX_VALUE : subscriptionType.getBookQuota();

        Subscription newSubscription = new Subscription(
                user, 
                subscriptionType, 
                startDate, 
                endDate, 
                booksQuota
        );
        
        return subscriptionRepository.save(newSubscription);
    }

    @Override
    public Optional<Subscription> getActiveSubscription(User user) {
        return subscriptionRepository.findByUserAndActiveTrue(user);
    }

    @Override
    public boolean hasActiveSubscription(User user) {
        Optional<Subscription> subscription = getActiveSubscription(user);
        return subscription.isPresent() && subscription.get().isValid();
    }

    @Override
    public boolean canBorrowBook(User user) {
        Optional<Subscription> subscription = getActiveSubscription(user);
        return subscription.isPresent() && 
               subscription.get().isValid() && 
               subscription.get().hasQuotaRemaining();
    }

    @Override
    @Transactional
    public boolean decrementBookQuota(User user) {
        Optional<Subscription> subscriptionOpt = getActiveSubscription(user);
        if (subscriptionOpt.isPresent() && subscriptionOpt.get().isValid() && 
            subscriptionOpt.get().hasQuotaRemaining()) {
            
            Subscription subscription = subscriptionOpt.get();
            subscription.decrementQuota();
            subscriptionRepository.save(subscription);
            return true;
        }
        return false;
    }

    @Override
    @Transactional
    public boolean addBooksToQuota(User user, int additionalBooks) {
        Optional<Subscription> subscriptionOpt = getActiveSubscription(user);
        if (subscriptionOpt.isPresent() && subscriptionOpt.get().isValid()) {
            Subscription subscription = subscriptionOpt.get();
            subscription.addQuota(additionalBooks);
            subscriptionRepository.save(subscription);
            return true;
        }
        return false;
    }

    @Override
    @Transactional
    public boolean extendSubscription(User user, int additionalDays) {
        Optional<Subscription> subscriptionOpt = getActiveSubscription(user);
        if (subscriptionOpt.isPresent() && subscriptionOpt.get().isValid()) {
            Subscription subscription = subscriptionOpt.get();
            subscription.extend(additionalDays);
            subscriptionRepository.save(subscription);
            return true;
        }
        return false;
    }

    @Override
    @Transactional
    public boolean cancelSubscription(User user) {
        Optional<Subscription> subscriptionOpt = getActiveSubscription(user);
        if (subscriptionOpt.isPresent()) {
            Subscription subscription = subscriptionOpt.get();
            subscription.setActive(false);
            subscriptionRepository.save(subscription);
            return true;
        }
        return false;
    }

    @Override
    public List<Subscription> getAllSubscriptions(User user) {
        return subscriptionRepository.findAllByUser(user);
    }

    @Override
    public List<Subscription> getAllActiveSubscriptions() {
        return subscriptionRepository.findAll().stream()
                .filter(Subscription::isValid)
                .toList();
    }
    
    @Override
    public List<Subscription> getAllActiveStudentSubscriptions() {
        // Utiliser directement la requête JPQL qui récupère uniquement l'abonnement actif de l'étudiant 'joannahasina'
        List<Subscription> activeStudentSubscriptions = subscriptionRepository.findAllActiveStudentSubscriptions();
        
        // Log minimal pour le débogage
        System.out.println("Nombre d'abonnements d'étudiants actifs trouvés: " + activeStudentSubscriptions.size());
        if (!activeStudentSubscriptions.isEmpty()) {
            activeStudentSubscriptions.forEach(subscription ->
                System.out.println("Étudiant avec abonnement actif: " + subscription.getUser().getUsername()));
        }
        
        return activeStudentSubscriptions;
    }
    
    @Override
    public List<Subscription> getAllSubscriptions() {
        return subscriptionRepository.findAll();
    }

    @Override
    public List<Subscription> getSubscriptionsExpiringSoon(int daysThreshold) {
        LocalDateTime thresholdDate = LocalDateTime.now().plusDays(daysThreshold);
        return subscriptionRepository.findAllActiveSubscriptionsExpiringBefore(thresholdDate);
    }

    @Override
    public List<Subscription> getSubscriptionsWithZeroBooksRemaining() {
        return subscriptionRepository.findAllActiveSubscriptionsWithZeroBooksRemaining();
    }

    @Override
    public Subscription saveSubscription(Subscription subscription) {
        return subscriptionRepository.save(subscription);
    }
}