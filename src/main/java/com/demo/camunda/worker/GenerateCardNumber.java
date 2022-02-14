package com.demo.camunda.worker;

import com.demo.camunda.constants.AppConstants;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.spring.client.annotation.ZeebeWorker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.Collections;

@Component
public class GenerateCardNumber {

    Logger logger = LoggerFactory.getLogger(GenerateCardNumber.class);

    // This should be of course injected and depends on the environment.
// Hard coded for simplicity here
    public static String ENDPOINT = "http://localhost:8200/insurance";

    @Autowired
    private RestTemplate restTemplate;

    @ZeebeWorker(type = "process-application")
    public void callGenerateTicketRestService(final JobClient client, final ActivatedJob job) throws IOException {
        logger.info("Generate ticket via REST [" + job + "]");

        if ("fail".equalsIgnoreCase((String) job.getVariablesAsMap().get(AppConstants.TEST_APPLICATION_FAILURE))) {
            throw new IOException("[***Mock***] Could not connect to HTTP server");
        } else {
            GenerateCardNumberResponse res = restTemplate.getForObject(ENDPOINT, GenerateCardNumberResponse.class);
            logger.info("Succeeded with " + res);

            client.newCompleteCommand(job.getKey())
                    .variables(Collections.singletonMap(AppConstants.INSURANCE_CARD_NUMBER, res.insuranceCardNumber))
                    .send()
                    .exceptionally(throwable -> {
                        throw new RuntimeException("Could not complete job " + job, throwable);
                    });
        }
    }

    public static class GenerateCardNumberResponse {
        public String insuranceCardNumber;
    }
}
