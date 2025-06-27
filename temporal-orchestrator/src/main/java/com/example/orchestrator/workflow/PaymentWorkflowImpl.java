package com.example.orchestrator.workflow;

import io.temporal.activity.ActivityOptions;
import io.temporal.workflow.Workflow;
import java.time.Duration;
import io.temporal.common.RetryOptions;
import org.slf4j.Logger;

public class PaymentWorkflowImpl implements PaymentWorkflow {

    private static final ActivityOptions DEFAULT_ACTIVITY_OPTIONS = ActivityOptions.newBuilder()
            .setStartToCloseTimeout(Duration.ofSeconds(30))
            .setRetryOptions(RetryOptions.newBuilder()
                    .setInitialInterval(Duration.ofSeconds(2))
                    .setMaximumInterval(Duration.ofSeconds(10))
                    .setBackoffCoefficient(2.0)
                    .setMaximumAttempts(5)
                    .build())
            .build();

    private final PaymentActivities activities = Workflow.newActivityStub(PaymentActivities.class, DEFAULT_ACTIVITY_OPTIONS);

    @Override
    public void processPayment(String processId, String clientNumber, double chargeAmount) {
        Logger logger = Workflow.getLogger(PaymentWorkflowImpl.class);
        logger.info("[ProcessID: {}] Starting payment process.", processId);

        // Step 1: Validate Account
        logger.info("[ProcessID: {}] Executing validation activity for account {}.", processId, clientNumber);
        boolean isValid = activities.validateAccount(clientNumber);
        if (!isValid) {
            logger.info("[ProcessID: {}] Account {} validation failed.", processId, clientNumber);
            throw new RuntimeException("Account validation failed for client: " + clientNumber);
        }
        logger.info("[ProcessID: {}] Account {} validated successfully.", processId, clientNumber);

        // Step 2: Charge Card
        logger.info("[ProcessID: {}] Executing charge activity for account {} with amount {}.", processId, clientNumber, chargeAmount);
        String transactionId = activities.chargeCard(clientNumber, chargeAmount);
        if (transactionId == null || transactionId.isEmpty()) {
            logger.info("[ProcessID: {}] Charge failed for account {}.", processId, clientNumber);
            throw new RuntimeException("Charge failed for client: " + clientNumber);
        }
        logger.info("[ProcessID: {}] Charge successful. TransactionId: {}.", processId, transactionId);

        // Step 3: Save Transaction
        logger.info("[ProcessID: {}] Executing persistence activity.", processId);
        String dbId = activities.saveTransaction(processId, clientNumber, chargeAmount);
        if (dbId == null || dbId.isEmpty()) {
            logger.info("[ProcessID: {}] Transaction save failed.", processId);
            throw new RuntimeException("Transaction save failed for processId: " + processId);
        }
        logger.info("[ProcessID: {}] Process completed and saved to DB. DB Id: {}.", processId, dbId);
    }
    
}
