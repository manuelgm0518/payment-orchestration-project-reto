package com.example.orchestrator.workflow;

import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

@WorkflowInterface
public interface PaymentWorkflow {

    @WorkflowMethod
    public void processPayment(String processId, String clientNumber, double chargeAmount);
    
}

