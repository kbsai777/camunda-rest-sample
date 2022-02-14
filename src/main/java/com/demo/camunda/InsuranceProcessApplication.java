package com.demo.camunda;

import io.camunda.zeebe.spring.client.EnableZeebeClient;
import io.camunda.zeebe.spring.client.annotation.ZeebeDeployment;
import org.springframework.amqp.core.Queue;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
@EnableZeebeClient
@ZeebeDeployment(resources = { "classpath:process-insurance-application.bpmn" })
public class InsuranceProcessApplication {

  public static void main(String... args) {
    SpringApplication.run(InsuranceProcessApplication.class, args);
  }

  @Bean
  public RestTemplate restTemplate() {
    return new RestTemplate();
  }

  @Bean
  public org.springframework.amqp.core.Queue paymentResponseQueue(){
    return new Queue("paymentResponse",true);
  }
}