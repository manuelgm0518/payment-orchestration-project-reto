package com.example.orchestrator.config;

import com.example.orchestrator.workflow.PaymentActivitiesImpl;
import com.example.orchestrator.workflow.PaymentWorkflowImpl;
import io.temporal.client.WorkflowClient;
import io.temporal.worker.Worker;
import io.temporal.worker.WorkerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TemporalWorkerConfig {

    public static final String TASK_QUEUE = "default";

    @Bean
    public WorkerFactory workerFactory(WorkflowClient workflowClient, PaymentActivitiesImpl paymentActivities) {
        WorkerFactory factory = WorkerFactory.newInstance(workflowClient);
        Worker worker = factory.newWorker(TASK_QUEUE);
        worker.registerWorkflowImplementationTypes(PaymentWorkflowImpl.class);
        worker.registerActivitiesImplementations(paymentActivities);
        factory.start();
        return factory;
    }
} 