package integration.tests;

import com.fasterxml.jackson.databind.ObjectMapper;
import integration.AbstractIntegrationTest;
import integration.IntegrationTestContext;
import integration.actions.MonogoDBActions;
import integration.actions.RabbitMQActions;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import space.gavinklfong.demo.insurance.dto.ClaimRequest;
import space.gavinklfong.demo.insurance.dto.Priority;
import space.gavinklfong.demo.insurance.dto.Product;
import space.gavinklfong.demo.insurance.model.ClaimReviewResult;
import space.gavinklfong.demo.insurance.model.Status;

import java.io.IOException;
import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@Slf4j
public class ClaimIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private RabbitMQActions rabbitMQAction;

    @Autowired
    private MonogoDBActions monogoDBActions;

    @Autowired
    private IntegrationTestContext testContext;

    @Autowired
    private RabbitAdmin rabbitAdmin;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void runTest() throws InterruptedException, IOException {

        givenListenerCreatedForClaimStatusUpdatedExchange();

        whenSubmitClaimRequestToQueue("HOME", 2000D);
        waitForXSeconds(2);

        thenClaimStatusIsReviewedAndSavedToDatabaseWithStatus("NEED_FOLLOW_UP");
        waitForXSeconds(1);
        thenClaimStatusIsSentToMessageQueueForCommunication();
    }

    private void givenListenerCreatedForClaimStatusUpdatedExchange() {

        TopicExchange claimUpdatedExchange = new TopicExchange("claimUpdated.exchange");
        Queue receiverQueue = new Queue("claimUpdated.exchange.receiver");
        rabbitAdmin.declareQueue(receiverQueue);
        rabbitAdmin.declareBinding(BindingBuilder.bind(receiverQueue).to(claimUpdatedExchange).with("#"));
//        rabbitAdmin.declareBinding(new Binding("receiver", Binding.DestinationType.EXCHANGE, "claimUpdated.exchange", "#", new HashMap()));
    }

    private void whenSubmitClaimRequestToQueue(String product, Double amount) {
        ClaimRequest claimRequest = ClaimRequest.builder()
                .id(UUID.randomUUID().toString())
                .customerId(UUID.randomUUID().toString())
                .claimAmount(amount)
                .priority(Priority.MEDIUM)
                .product(Product.valueOf(product))
                .build();
        rabbitMQAction.sendMessage("claimSubmitted.exchange", claimRequest);
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
}
