package com.example.orchestrator;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Map;
import java.util.UUID;

import com.example.orchestrator.config.TemporalWorkerConfig;
import com.example.orchestrator.workflow.PaymentWorkflow;

@RestController
@RequestMapping("/api/v1/orchestrator")
public class OrchestratorController {

    private final WorkflowClient workflowClient;
    private static final Logger logger = LoggerFactory.getLogger(OrchestratorController.class);

    public OrchestratorController(WorkflowClient workflowClient) {
        this.workflowClient = workflowClient;
    }

    public record PaymentRequest(String clientNumber, double chargeAmount) {}

    @PostMapping("/process-payment")
    public ResponseEntity<?> processPayment(@RequestBody PaymentRequest request) {
        if (!isValidRequest(request)) {
            return ResponseEntity.badRequest().body("Invalid request: clientNumber must be non-empty and chargeAmount must be positive.");
        }

        try {
            String processId = UUID.randomUUID().toString();
            logger.info("Starting payment workflow for client {} with amount {}. [ProcessID: {}]", 
                request.clientNumber(), request.chargeAmount(), processId);
            
            startWorkflow(processId, request);
            return ResponseEntity.ok(Map.of("processId", processId, "status", "STARTED"));
        } catch (Exception e) {
            logger.error("Failed to start payment workflow: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body("Failed to start workflow: " + e.getMessage());
        }
    }

    private boolean isValidRequest(PaymentRequest request) {
        return request.clientNumber() != null && 
               !request.clientNumber().isBlank() && 
               request.chargeAmount() > 0;
    }

    private void startWorkflow(String processId, PaymentRequest request) {
        WorkflowOptions options = WorkflowOptions.newBuilder()
                .setTaskQueue(TemporalWorkerConfig.TASK_QUEUE)
                .setWorkflowId(processId)
                .build();
        
        PaymentWorkflow workflow = workflowClient.newWorkflowStub(PaymentWorkflow.class, options);
        WorkflowClient.start(workflow::processPayment, processId, request.clientNumber(), request.chargeAmount());
    }
}