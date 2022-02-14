package com.demo.camunda.worker;

import com.demo.camunda.constants.AppConstants;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.spring.client.annotation.ZeebeWorker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.UUID;

@Component
public class RetrievePaymentAdapter {

    private Logger logger = LoggerFactory.getLogger(RetrievePaymentAdapter.class);

    public static String RABBIT_QUEUE_NAME = "paymentRequest";

    @Autowired
    protected RabbitTemplate rabbitTemplate;

    @ZeebeWorker(type = "retrieve-payment")
    public void retrievePayment(final JobClient client, final ActivatedJob job) {
        logger.info("Send message to retrieve payment [" + job + "]");

        String paymentRequestId = UUID.randomUUID().toString();

        rabbitTemplate.convertAndSend(RABBIT_QUEUE_NAME, paymentRequestId);

        // complete activity
        client.newCompleteCommand(job.getKey()) //
                .variables(Collections.singletonMap(AppConstants.PAYMENT_REQUEST_ID, paymentRequestId))
                .send().join();
    }
}
