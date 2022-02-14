package com.demo.camunda.worker;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.camunda.zeebe.client.ZeebeClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

@Component
public class PaymentReceiver {
  
  private Logger logger = LoggerFactory.getLogger(PaymentReceiver.class);

  @Autowired
  private ZeebeClient client;
  
  @Autowired
  private ObjectMapper objectMapper;
  
  @RabbitListener(queues = "paymentResponse")
  @Transactional
  public void messageReceived(String paymentResponseString) throws JsonMappingException, JsonProcessingException {
    PaymentResponseMessage paymentResponse = objectMapper.readValue(paymentResponseString, PaymentResponseMessage.class);
    logger.info("Received " + paymentResponse);
    
    // Route message to workflow
    client.newPublishMessageCommand()
      .messageName("message-payment-received") //
      .correlationKey(paymentResponse.paymentRequestId)
      .variables(Collections.singletonMap("paymentConfirmationId", paymentResponse.paymentConfirmationId)) //
      .send().join();
  }
  
  public static class PaymentResponseMessage {
    public String paymentRequestId;
    public String paymentConfirmationId;
    public String toString() {
      return "PaymentResponseMessage [paymentRequestId=" + paymentRequestId + ", paymentConfirmationId=" + paymentConfirmationId + "]";
    }
  }

}
