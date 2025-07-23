package com.library.service;

import com.library.enums.SubscriptionType;
import com.library.model.Student;
import com.library.model.Subscription;
import com.library.model.User;
import com.library.repository.SubscriptionRepository;
import com.library.service.impl.SubscriptionServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SubscriptionServiceImplTest {

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @InjectMocks
    private SubscriptionServiceImpl subscriptionService;

    private Student student;
    private Subscription subscription;

    @BeforeEach
    void setUp() {
        // Create a test student
        student = new Student();
        student.setId(1L);
        student.setName("Test Student");
        student.setEmail("student@test.com");
        student.setActive(true);

        // Create a test subscription
        subscription = new Subscription();
        subscription.setId(1L);
        subscription.setUser(student);
        subscription.setSubscriptionType(SubscriptionType.STANDARD);
        subscription.setStartDate(LocalDateTime.now());
        subscription.setEndDate(LocalDateTime.now().plusDays(30));
        subscription.setActive(true);
        subscription.setBooksRemaining(10);
    }

    @Test
    void testCreateSubscription() {
        // Arrange
        when(subscriptionRepository.findByUserAndActiveTrue(student)).thenReturn(Optional.empty());
        when(subscriptionRepository.save(any(Subscription.class))).thenReturn(subscription);

        // Act
        Subscription result = subscriptionService.createSubscription(student, SubscriptionType.STANDARD);

        // Assert
        assertNotNull(result);
        assertEquals(student, result.getUser());
        assertEquals(SubscriptionType.STANDARD, result.getSubscriptionType());
        assertTrue(result.isActive());
        assertEquals(10, result.getBooksRemaining());

        // Verify repository was called
        verify(subscriptionRepository).save(any(Subscription.class));
    }

    @Test
    void testGetActiveSubscription() {
        // Arrange
        when(subscriptionRepository.findByUserAndActiveTrue(student)).thenReturn(Optional.of(subscription));

        // Act
        Optional<Subscription> result = subscriptionService.getActiveSubscription(student);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(subscription, result.get());
    }

    @Test
    void testGetActiveSubscriptionWhenNoneExists() {
        // Arrange
        when(subscriptionRepository.findByUserAndActiveTrue(student)).thenReturn(Optional.empty());

        // Act
        Optional<Subscription> result = subscriptionService.getActiveSubscription(student);

        // Assert
        assertFalse(result.isPresent());
    }

    @Test
    void testGetSubscriptionById() {
        // Arrange
        when(subscriptionRepository.findById(1L)).thenReturn(Optional.of(subscription));

        // Act
        Optional<Subscription> result = subscriptionService.getSubscriptionById(1L);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(subscription, result.get());
    }

    @Test
    void testHasActiveSubscription() {
        // Arrange
        when(subscriptionRepository.findByUserAndActiveTrue(student)).thenReturn(Optional.of(subscription));

        // Act
        boolean result = subscriptionService.hasActiveSubscription(student);

        // Assert
        assertTrue(result);
    }

    @Test
    void testHasActiveSubscriptionWhenNoneExists() {
        // Arrange
        when(subscriptionRepository.findByUserAndActiveTrue(student)).thenReturn(Optional.empty());

        // Act
        boolean result = subscriptionService.hasActiveSubscription(student);

        // Assert
        assertFalse(result);
    }

    @Test
    void testCanBorrowBook() {
        // Arrange
        when(subscriptionRepository.findByUserAndActiveTrue(student)).thenReturn(Optional.of(subscription));

        // Act
        boolean result = subscriptionService.canBorrowBook(student);

        // Assert
        assertTrue(result);
    }

    @Test
    void testCanBorrowBookWhenNoQuotaRemaining() {
        // Arrange
        subscription.setBooksRemaining(0);
        when(subscriptionRepository.findByUserAndActiveTrue(student)).thenReturn(Optional.of(subscription));

        // Act
        boolean result = subscriptionService.canBorrowBook(student);

        // Assert
        assertFalse(result);
    }

    @Test
    void testDecrementBookQuota() {
        // Arrange
        int originalBooksRemaining = subscription.getBooksRemaining();
        when(subscriptionRepository.findByUserAndActiveTrue(student)).thenReturn(Optional.of(subscription));
        when(subscriptionRepository.save(any(Subscription.class))).thenReturn(subscription);

        // Act
        boolean result = subscriptionService.decrementBookQuota(student);

        // Assert
        assertTrue(result);
        assertEquals(originalBooksRemaining - 1, subscription.getBooksRemaining());

        // Verify repository was called
        verify(subscriptionRepository).save(any(Subscription.class));
    }

    @Test
    void testDecrementBookQuotaWithNoQuotaRemaining() {
        // Arrange
        subscription.setBooksRemaining(0);
        when(subscriptionRepository.findByUserAndActiveTrue(student)).thenReturn(Optional.of(subscription));

        // Act
        boolean result = subscriptionService.decrementBookQuota(student);

        // Assert
        assertFalse(result);

        // Verify repository was not called
        verify(subscriptionRepository, never()).save(any(Subscription.class));
    }

    @Test
    void testAddBooksToQuota() {
        // Arrange
        int originalBooksRemaining = subscription.getBooksRemaining();
        when(subscriptionRepository.findByUserAndActiveTrue(student)).thenReturn(Optional.of(subscription));
        when(subscriptionRepository.save(any(Subscription.class))).thenReturn(subscription);

        // Act
        boolean result = subscriptionService.addBooksToQuota(student, 5);

        // Assert
        assertTrue(result);
        assertEquals(originalBooksRemaining + 5, subscription.getBooksRemaining());

        // Verify repository was called
        verify(subscriptionRepository).save(any(Subscription.class));
    }

    @Test
    void testExtendSubscription() {
        // Arrange
        LocalDateTime originalEndDate = subscription.getEndDate();
        when(subscriptionRepository.findByUserAndActiveTrue(student)).thenReturn(Optional.of(subscription));
        when(subscriptionRepository.save(any(Subscription.class))).thenReturn(subscription);

        // Act
        boolean result = subscriptionService.extendSubscription(student, 15);

        // Assert
        assertTrue(result);
        assertTrue(subscription.getEndDate().isAfter(originalEndDate));

        // Verify repository was called
        verify(subscriptionRepository).save(any(Subscription.class));
    }

    @Test
    void testCancelSubscription() {
        // Arrange
        when(subscriptionRepository.findByUserAndActiveTrue(student)).thenReturn(Optional.of(subscription));
        when(subscriptionRepository.save(any(Subscription.class))).thenReturn(subscription);

        // Act
        boolean result = subscriptionService.cancelSubscription(student);

        // Assert
        assertTrue(result);
        assertFalse(subscription.isActive());

        // Verify repository was called
        verify(subscriptionRepository).save(any(Subscription.class));
    }

    @Test
    void testGetAllSubscriptions() {
        // Arrange
        List<Subscription> subscriptions = Arrays.asList(subscription);
        when(subscriptionRepository.findAllByUser(student)).thenReturn(subscriptions);

        // Act
        List<Subscription> result = subscriptionService.getAllSubscriptions(student);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(subscription, result.get(0));
    }

    @Test
    void testGetAllActiveSubscriptions() {
        // Arrange
        Subscription subscription2 = new Subscription();
        subscription2.setId(2L);
        subscription2.setUser(student);
        subscription2.setSubscriptionType(SubscriptionType.PREMIUM);
        subscription2.setStartDate(LocalDateTime.now());
        subscription2.setEndDate(LocalDateTime.now().plusDays(30));
        subscription2.setActive(true);

        List<Subscription> subscriptions = Arrays.asList(subscription, subscription2);
        when(subscriptionRepository.findAll()).thenReturn(subscriptions);

        // Act
        List<Subscription> result = subscriptionService.getAllActiveSubscriptions();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.contains(subscription));
        assertTrue(result.contains(subscription2));
    }

    @Test
    void testGetSubscriptionsExpiringSoon() {
        // Arrange
        List<Subscription> expiringSubscriptions = Arrays.asList(subscription);
        when(subscriptionRepository.findAllActiveSubscriptionsExpiringBefore(any(LocalDateTime.class)))
                .thenReturn(expiringSubscriptions);

        // Act
        List<Subscription> result = subscriptionService.getSubscriptionsExpiringSoon(1);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.contains(subscription));
        
        // Verify repository was called with correct date
        ArgumentCaptor<LocalDateTime> dateCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
        verify(subscriptionRepository).findAllActiveSubscriptionsExpiringBefore(dateCaptor.capture());
        
        // The date should be tomorrow
        LocalDateTime capturedDate = dateCaptor.getValue();
        assertEquals(LocalDateTime.now().plusDays(1).getDayOfYear(), capturedDate.getDayOfYear());
    }

    @Test
    void testGetSubscriptionsWithZeroBooksRemaining() {
        // Arrange
        subscription.setBooksRemaining(0);
        List<Subscription> zeroQuotaSubscriptions = Arrays.asList(subscription);
        when(subscriptionRepository.findAllActiveSubscriptionsWithZeroBooksRemaining())
                .thenReturn(zeroQuotaSubscriptions);

        // Act
        List<Subscription> result = subscriptionService.getSubscriptionsWithZeroBooksRemaining();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.contains(subscription));
    }

    @Test
    void testSaveSubscription() {
        // Arrange
        when(subscriptionRepository.save(subscription)).thenReturn(subscription);

        // Act
        Subscription result = subscriptionService.saveSubscription(subscription);

        // Assert
        assertNotNull(result);
        assertEquals(subscription, result);
    }
}