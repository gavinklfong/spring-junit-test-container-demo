package integration.tests;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import integration.AbstractComponentTest;
import integration.ComponentTestContext;
import integration.actions.MonogoDBActions;
import integration.setup.CustomerSrvSetup;
import integration.setup.WireMockSetup;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import space.gavinklfong.demo.insurance.dto.ClaimRequest;
import space.gavinklfong.demo.insurance.dto.Priority;
import space.gavinklfong.demo.insurance.dto.Product;
import space.gavinklfong.demo.insurance.dto.Risk;
import space.gavinklfong.demo.insurance.model.ClaimReviewResult;
import space.gavinklfong.demo.insurance.model.Status;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@Slf4j
public class HighRiskCustomerClaimProcessingComponentTest extends AbstractComponentTest {

    @Autowired
    private MonogoDBActions monogoDBActions;

    @Autowired
    private ComponentTestContext testContext;

    @Autowired
    private RabbitAdmin rabbitAdmin;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private CustomerSrvSetup customerSrvSetup;

    private ObjectMapper objectMapper = new ObjectMapper();

    private TopicExchange claimUpdatedExchange = new TopicExchange("claimUpdated.exchange");
    private Queue receiverQueue = new Queue("claimUpdated.exchange.receiver");

    private static final String HIGH_RISK_CUSTOMER_ID = "C94C6168-6AED-46F9-9BA8-AA17793D41F9";
    private static final String MEDIUM_RISK_CUSTOMER_ID = "0753A8A5-6331-43E0-B868-6708058CEAB6";
    private static final String LOW_RISK_CUSTOMER_ID = "ABEF6840-4926-4A21-9837-42878D062C50";

    @AfterEach
    void afterEach() {
        WireMockSetup.reset();
        resetReceiverQueue();
    }

    @Test
    void givenHighRiskCustomer_whenHomePolicyClaimSubmitted_thenStatusIsDeclined() throws InterruptedException, IOException {

        givenListenerCreatedForClaimStatusUpdatedExchange();
        givenHighRiskCustomer();

        whenSubmitClaimRequestToQueue(HIGH_RISK_CUSTOMER_ID,"HOME", 1000D);
        waitForXSeconds(2);

        thenClaimStatusIsReviewedAndSavedToDatabaseWithStatus("DECLINED");
        waitForXSeconds(1);
        thenClaimStatusIsSentToMessageQueueForCommunication();
    }

    @Test
    void givenHighRiskCustomer_whenMedicalPolicyClaimBelow5kSubmitted_thenStatusIsDeclined() throws InterruptedException, IOException {

        givenListenerCreatedForClaimStatusUpdatedExchange();
        givenHighRiskCustomer();

        whenSubmitClaimRequestToQueue(HIGH_RISK_CUSTOMER_ID,"MEDICAL", 100D);
        waitForXSeconds(2);

        thenClaimStatusIsReviewedAndSavedToDatabaseWithStatus("DECLINED");
        waitForXSeconds(1);
        thenClaimStatusIsSentToMessageQueueForCommunication();
    }

    @Test
    void givenHighRiskCustomer_whenMedicalPolicyClaimEq5kSubmitted_thenStatusIsDeclined() throws InterruptedException, IOException {

        givenListenerCreatedForClaimStatusUpdatedExchange();
        givenHighRiskCustomer();

        whenSubmitClaimRequestToQueue(HIGH_RISK_CUSTOMER_ID,"MEDICAL", 5000D);
        waitForXSeconds(2);

        thenClaimStatusIsReviewedAndSavedToDatabaseWithStatus("DECLINED");
        waitForXSeconds(1);
        thenClaimStatusIsSentToMessageQueueForCommunication();
    }

    private void givenHighRiskCustomer() throws JsonProcessingException {
        customerSrvSetup.setUpCustomerForId(HIGH_RISK_CUSTOMER_ID, Risk.HIGH);
    }

    private void givenListenerCreatedForClaimStatusUpdatedExchange() {
        rabbitAdmin.declareQueue(receiverQueue);
        rabbitAdmin.declareBinding(BindingBuilder.bind(receiverQueue).to(claimUpdatedExchange).with("#"));
    }

    private void whenSubmitClaimRequestToQueue(String customerId, String product, Double amount) {
        ClaimRequest claimRequest = ClaimRequest.builder()
                .id(UUID.randomUUID().toString())
                .customerId(customerId)
                .claimAmount(amount)
                .priority(Priority.MEDIUM)
                .product(Product.valueOf(product))
                .build();
        rabbitTemplate.convertAndSend("claimSubmitted.exchange", "#", claimRequest);
        testContext.setClaimRequest(claimRequest);
    }

    private void waitForXSeconds(int seconds) throws InterruptedException {
        TimeUnit.SECONDS.sleep(seconds);
    }

    private void thenClaimStatusIsReviewedAndSavedToDatabaseWithStatus(String status) {
        Optional<ClaimReviewResult> result = monogoDBActions.retrieveClaimProcess(testContext.getClaimRequest().getId());
        assertTrue(result.isPresent());
        assertEquals(Status.valueOf(status), result.get().getStatus());
    }

    private void thenClaimStatusIsSentToMessageQueueForCommunication() throws IOException {
        Message claimUpdateMsg = rabbitTemplate.receive("claimUpdated.exchange.receiver");
        ClaimReviewResult claimUpdate = objectMapper.readValue(claimUpdateMsg.getBody(), ClaimReviewResult.class);
        ClaimRequest claimRequest = testContext.getClaimRequest();
        assertTrue(claimUpdate.getClaimId().equals(claimRequest.getId()));
    }

    private void resetReceiverQueue() {
        rabbitAdmin.purgeQueue(receiverQueue.getName(), true);
    }
}
