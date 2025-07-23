/**
 * Subscription Upgrade/Downgrade JavaScript
 * Handles plan selection, price calculation, and payment method selection
 */

// Global variables
let selectedPlan = null;
let selectedPaymentMethod = 'balance';
let currentPlanType = null;
let daysRemaining = 0;
let userBalance = 0;

// Plan prices
const planPrices = {
    'DISCOVERY': 1.99,
    'STANDARD': 9.99,
    'PREMIUM': 14.99
};

// Plan durations in days
const planDurations = {
    'DISCOVERY': 7,
    'STANDARD': 30,
    'PREMIUM': 30
};

// Plan display names
const planDisplayNames = {
    'DISCOVERY': 'Découverte',
    'STANDARD': 'Standard',
    'PREMIUM': 'Premium'
};

// Initialize the page
document.addEventListener('DOMContentLoaded', function() {
    // Get current subscription info
    currentPlanType = document.getElementById('current-plan-name').textContent.trim();
    daysRemaining = parseInt(document.getElementById('days-remaining').textContent.trim());
    
    // Get user balance
    const balanceText = document.getElementById('current-balance').textContent.trim();
    userBalance = parseFloat(balanceText.replace('€', '').replace(',', '.').trim());
    
    // Pre-select current plan if it exists
    if (currentPlanType !== 'Aucun') {
        for (const planType in planDisplayNames) {
            if (planDisplayNames[planType] === currentPlanType) {
                // Don't auto-select if it's the current plan
                currentPlanType = planType;
                break;
            }
        }
    }
    
    // Add event listeners to payment methods
    document.querySelectorAll('.payment-method').forEach(method => {
        method.addEventListener('click', function() {
            const paymentType = this.getAttribute('data-payment-method');
            selectPaymentMethod(paymentType);
        });
    });
    
    // Add event listener to form submission
    const upgradeForm = document.getElementById('subscription-upgrade-form');
    if (upgradeForm) {
        upgradeForm.addEventListener('submit', function(e) {
            if (!validateForm()) {
                e.preventDefault();
                return false;
            }
        });
    }
});

/**
 * Select a subscription plan
 * @param {string} planType - The type of plan (DISCOVERY, STANDARD, PREMIUM)
 * @param {number} planPrice - The price of the plan
 */
function selectPlan(planType, planPrice) {
    // Don't do anything if selecting the current plan
    if (planType === currentPlanType) {
        return;
    }
    
    // Update selected plan
    selectedPlan = planType;
    
    // Update UI
    document.querySelectorAll('.plan-card').forEach(card => {
        card.classList.remove('selected');
    });
    
    document.getElementById(planType.toLowerCase() + '-plan').classList.add('selected');
    
    // Update checkout information
    document.getElementById('new-plan-name').textContent = planDisplayNames[planType];
    document.getElementById('base-price').textContent = planPrice.toFixed(2) + ' €';
    
    // Calculate price difference
    calculatePriceDifference(planType, planPrice);
    
    // Show appropriate buttons
    document.getElementById('checkout-actions').style.display = 'flex';
    
    // Enable the submit button
    document.getElementById('submit-button').disabled = false;
}

/**
 * Calculate the price difference between current and new plan
 * @param {string} newPlanType - The type of the new plan
 * @param {number} newPlanPrice - The price of the new plan
 */
function calculatePriceDifference(newPlanType, newPlanPrice) {
    // If no current subscription, just show the full price
    if (currentPlanType === 'Aucun' || currentPlanType === null) {
        document.getElementById('total-price').textContent = newPlanPrice.toFixed(2) + ' €';
        document.getElementById('price-difference-alert').style.display = 'none';
        document.getElementById('credit-row').style.display = 'none';
        return;
    }
    
    // Get current plan price
    const currentPlanPrice = planPrices[currentPlanType];
    const currentPlanDuration = planDurations[currentPlanType];
    const newPlanDuration = planDurations[newPlanType];
    
    // Calculate the value of remaining days in current subscription
    const dailyRateCurrentPlan = currentPlanPrice / currentPlanDuration;
    const remainingValue = dailyRateCurrentPlan * daysRemaining;
    
    // Calculate the new price
    let finalPrice = 0;
    let priceMessage = '';
    
    if (isPlanUpgrade(currentPlanType, newPlanType)) {
        // Upgrading - pay the difference
        const dailyRateNewPlan = newPlanPrice / newPlanDuration;
        const newPlanCostForRemainingDays = dailyRateNewPlan * daysRemaining;
        const priceDifference = newPlanCostForRemainingDays - remainingValue;
        
        finalPrice = priceDifference > 0 ? priceDifference : 0;
        priceMessage = 'Vous allez passer à un abonnement supérieur. La différence de prix sera calculée en tenant compte des jours restants de votre abonnement actuel.';
        
        document.getElementById('price-difference-alert').className = 'price-difference upgrade';
    } else {
        // Downgrading - calculate credit for next renewal
        finalPrice = 0;
        priceMessage = 'Vous allez passer à un abonnement inférieur. Ce changement prendra effet immédiatement, mais aucun remboursement ne sera effectué pour les jours restants de votre abonnement actuel.';
        
        document.getElementById('price-difference-alert').className = 'price-difference downgrade';
    }
    
    // Update UI
    document.getElementById('price-difference-message').textContent = priceMessage;
    document.getElementById('price-difference-alert').style.display = 'flex';
    
    if (finalPrice > 0) {
        document.getElementById('credit-row').style.display = 'flex';
        document.getElementById('credit-amount').textContent = '-' + remainingValue.toFixed(2) + ' €';
    } else {
        document.getElementById('credit-row').style.display = 'none';
    }
    
    document.getElementById('total-price').textContent = finalPrice.toFixed(2) + ' €';
    
    // Check if user has enough balance
    if (finalPrice > userBalance && selectedPaymentMethod === 'balance') {
        document.getElementById('balance-warning').style.display = 'block';
    } else {
        document.getElementById('balance-warning').style.display = 'none';
    }
}

/**
 * Determine if the new plan is an upgrade from the current plan
 * @param {string} currentPlan - The current plan type
 * @param {string} newPlan - The new plan type
 * @returns {boolean} - True if it's an upgrade, false otherwise
 */
function isPlanUpgrade(currentPlan, newPlan) {
    const planValues = {
        'DISCOVERY': 1,
        'STANDARD': 2,
        'PREMIUM': 3
    };
    
    return planValues[newPlan] > planValues[currentPlan];
}

/**
 * Select a payment method
 * @param {string} method - The payment method (balance, credit_card, paypal)
 */
function selectPaymentMethod(method) {
    selectedPaymentMethod = method;
    
    // Update UI
    document.querySelectorAll('.payment-method').forEach(el => {
        el.classList.remove('selected');
    });
    
    document.querySelectorAll('.payment-form').forEach(form => {
        form.classList.remove('active');
    });
    
    // Select the payment method
    document.querySelector(`.payment-method[data-payment-method="${method}"]`).classList.add('selected');
    
    // Show the appropriate form
    if (method !== 'balance') {
        document.getElementById(`${method}-form`).classList.add('active');
    }
    
    // Check if user has enough balance for account balance payment
    const totalPrice = parseFloat(document.getElementById('total-price').textContent.replace('€', '').trim());
    if (totalPrice > userBalance && method === 'balance') {
        document.getElementById('balance-warning').style.display = 'block';
        document.getElementById('submit-button').disabled = true;
    } else {
        document.getElementById('balance-warning').style.display = 'none';
        document.getElementById('submit-button').disabled = false;
    }
}

/**
 * Validate the form before submission
 * @returns {boolean} - True if valid, false otherwise
 */
function validateForm() {
    // Check if a plan is selected
    if (!selectedPlan) {
        alert('Veuillez sélectionner un plan d\'abonnement.');
        return false;
    }
    
    // If using account balance, check if there's enough balance
    if (selectedPaymentMethod === 'balance') {
        const totalPrice = parseFloat(document.getElementById('total-price').textContent.replace('€', '').trim());
        if (totalPrice > userBalance) {
            alert('Votre solde est insuffisant pour cette transaction.');
            return false;
        }
    }
    
    // Validate credit card form
    if (selectedPaymentMethod === 'credit_card') {
        const cardNumber = document.getElementById('card-number').value;
        const cardName = document.getElementById('card-name').value;
        const cardExpiry = document.getElementById('card-expiry').value;
        const cardCvv = document.getElementById('card-cvv').value;
        
        if (!cardNumber || !cardName || !cardExpiry || !cardCvv) {
            alert('Veuillez remplir tous les champs de la carte de crédit.');
            return false;
        }
        
        // Basic validation
        if (!/^\d{16}$/.test(cardNumber.replace(/\s/g, ''))) {
            alert('Le numéro de carte doit contenir 16 chiffres.');
            return false;
        }
        
        if (!/^\d{3,4}$/.test(cardCvv)) {
            alert('Le code CVV doit contenir 3 ou 4 chiffres.');
            return false;
        }
    }
    
    // Validate PayPal form
    if (selectedPaymentMethod === 'paypal') {
        const paypalEmail = document.getElementById('paypal-email').value;
        
        if (!paypalEmail) {
            alert('Veuillez entrer votre adresse email PayPal.');
            return false;
        }
        
        // Basic email validation
        if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(paypalEmail)) {
            alert('Veuillez entrer une adresse email valide.');
            return false;
        }
    }
    
    return true;
}

/**
 * Format credit card number with spaces
 * @param {HTMLInputElement} input - The input element
 */
function formatCardNumber(input) {
    let value = input.value.replace(/\s+/g, '').replace(/[^0-9]/gi, '');
    let formattedValue = '';
    
    for (let i = 0; i < value.length; i++) {
        if (i > 0 && i % 4 === 0) {
            formattedValue += ' ';
        }
        formattedValue += value[i];
    }
    
    input.value = formattedValue;
}

/**
 * Toggle sidebar visibility
 */
function toggleSidebar() {
    const sidebar = document.getElementById('sidebar');
    const mainContent = document.getElementById('mainContent');
    
    sidebar.classList.toggle('collapsed');
    mainContent.classList.toggle('expanded');
}