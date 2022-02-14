package com.demo.camunda.service;

import com.demo.camunda.constants.AppConstants;
import com.demo.camunda.model.ApplicationResponse;
import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.ZeebeFuture;
import io.camunda.zeebe.client.api.command.ClientStatusException;
import io.camunda.zeebe.client.api.response.ProcessInstanceResult;
import io.camunda.zeebe.spring.client.EnableZeebeClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;

import java.util.HashMap;
import java.util.UUID;

@RestController
@EnableZeebeClient
public class EligibilityRestController {

    private Logger logger = LoggerFactory.getLogger(EligibilityRestController.class);

    @Autowired
    private ZeebeClient client;

    @PutMapping("/insurance")
    public ResponseEntity<ApplicationResponse> applyInsurance(ServerWebExchange serverWebExchange) {

        String failureTestVariable = serverWebExchange.getRequest().getQueryParams().getFirst("failureScenario");

        ApplicationResponse response = new ApplicationResponse();
        response.applicationReferenceId = UUID.randomUUID().toString();

        HashMap<String, Object> variables = loadVariables(failureTestVariable, response);

        ZeebeFuture<ProcessInstanceResult> zeebeFuture = client.newCreateInstanceCommand().bpmnProcessId("insurance-application").latestVersion().variables(variables).withResult().send();

        try {
            ProcessInstanceResult workflowInstanceResult = zeebeFuture.join();

            response.applicationReferenceId = (String) workflowInstanceResult.getVariablesAsMap().get(AppConstants.APPLICATION_REFERENCE_ID);
            response.paymentConfirmationId = (String) workflowInstanceResult.getVariablesAsMap().get(AppConstants.PAYMENT_CONFIRMATION_ID);
            response.insuranceCardNumber = (String) workflowInstanceResult.getVariablesAsMap().get(AppConstants.INSURANCE_CARD_NUMBER);

            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (ClientStatusException ex) {
            logger.error("Timeout on waiting for workflow"); //, ex);
            return ResponseEntity.status(HttpStatus.ACCEPTED).build();
        }
    }

    private HashMap<String, Object> loadVariables(String failureTestVariable, ApplicationResponse response) {
        HashMap<String, Object> variables = new HashMap<>();
        variables.put(AppConstants.APPLICATION_REFERENCE_ID, response.applicationReferenceId);
        if (failureTestVariable != null) {
            variables.put(AppConstants.TEST_APPLICATION_FAILURE, failureTestVariable);
        }
        return variables;
    }

}
