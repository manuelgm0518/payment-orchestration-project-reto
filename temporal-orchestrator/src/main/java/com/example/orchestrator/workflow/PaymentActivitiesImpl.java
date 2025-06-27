package com.example.orchestrator.workflow;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class PaymentActivitiesImpl implements PaymentActivities {

    private final WebClient webClient;
    private static final Logger logger = LoggerFactory.getLogger(PaymentActivitiesImpl.class);

    public PaymentActivitiesImpl(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }

    private record ValidationRequest(String accountNumber) {}

    @Override
    public boolean validateAccount(String accountNumber) {
        logger.info("[Activity] Validating account: {}", accountNumber);
        ValidationRequest request = new ValidationRequest(accountNumber);
        try {
            Map<?,?> response = webClient.post()
                    .uri("http://ms-validator-charger/api/v1/payment/validate")
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();
            boolean result = response != null && "VALIDATION_OK".equals(response.get("status"));
            logger.info("[Activity] Validation result for account {}: {}", accountNumber, result ? "SUCCESS" : "FAILURE");
            return result;
        } catch (WebClientResponseException e) {
            logger.error("[Activity] Account validation failed for {}: {}", accountNumber, e.getResponseBodyAsString());
            throw new RuntimeException("Account validation failed: " + e.getResponseBodyAsString(), e);
        }
    }

    private record ChargeRequest(String accountNumber, double amount) {}

    @Override
    public String chargeCard(String accountNumber, double amount) {
        logger.info("[Activity] Charging account: {} with amount: {}", accountNumber, amount);
        ChargeRequest request = new ChargeRequest(accountNumber, amount);
        try {
            Map<?,?> response = webClient.post()
                    .uri("http://ms-validator-charger/api/v1/payment/charge")
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();
            if (response != null && "CHARGE_SUCCESS".equals(response.get("status"))) {
                String transactionId = (String) response.get("transactionId");
                logger.info("[Activity] Charge successful for account {}. TransactionId: {}", accountNumber, transactionId);
                return transactionId;
            } else {
                logger.error("[Activity] Charge failed for account {}: {}", accountNumber, response);
                throw new RuntimeException("Charge failed: " + response);
            }
        } catch (WebClientResponseException e) {
            logger.error("[Activity] Charge failed for account {}: {}", accountNumber, e.getResponseBodyAsString());
            throw new RuntimeException("Charge failed: " + e.getResponseBodyAsString(), e);
        }
    }

    private record PersistenceRequest(String processId, String accountNumber, double chargedAmount) {}

    @Override
    public String saveTransaction(String processId, String accountNumber, double chargedAmount) {
        logger.info("[Activity] Persisting transaction. ProcessId: {}, Account: {}, Amount: {}", processId, accountNumber, chargedAmount);
        PersistenceRequest request = new PersistenceRequest(processId, accountNumber, chargedAmount);
        try {
            Map<?,?> response = webClient.post()
                    .uri("http://ms-persistence/api/v1/transaction/save")
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();
            if (response != null && "SAVED".equals(response.get("status"))) {
                String dbId = (String) response.get("dbId");
                logger.info("[Activity] Transaction persisted. ProcessId: {}, DB Id: {}", processId, dbId);
                return dbId;
            } else {
                logger.error("[Activity] Transaction persistence failed. ProcessId: {}, Response: {}", processId, response);
                throw new RuntimeException("Transaction save failed: " + response);
            }
        } catch (WebClientResponseException e) {
            logger.error("[Activity] Transaction persistence failed. ProcessId: {}, Error: {}", processId, e.getResponseBodyAsString());
            throw new RuntimeException("Transaction save failed: " + e.getResponseBodyAsString(), e);
        }
    }
}
