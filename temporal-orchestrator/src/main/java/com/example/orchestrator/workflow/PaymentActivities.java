package com.example.orchestrator.workflow;

import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;

@ActivityInterface
public interface PaymentActivities {
    
    @ActivityMethod
    boolean validateAccount(String accountNumber);

    @ActivityMethod
    String chargeCard(String accountNumber, double amount);

    @ActivityMethod
    String saveTransaction(String processId, String accountNumber, double chargedAmount);
}
