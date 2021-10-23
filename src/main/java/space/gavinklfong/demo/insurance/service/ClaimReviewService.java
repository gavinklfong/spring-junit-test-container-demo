package space.gavinklfong.demo.insurance.service;

import com.github.javafaker.Faker;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import space.gavinklfong.demo.insurance.apiclients.CustomerSrvClient;
import space.gavinklfong.demo.insurance.dto.ClaimRequest;
import space.gavinklfong.demo.insurance.dto.Customer;
import space.gavinklfong.demo.insurance.dto.Product;
import space.gavinklfong.demo.insurance.dto.Risk;
import space.gavinklfong.demo.insurance.model.ClaimReviewResult;
import space.gavinklfong.demo.insurance.model.Status;
import space.gavinklfong.demo.insurance.repository.ClaimProcessRepository;

import java.util.Random;

@Slf4j
@Service
public class ClaimReviewService {

    private static final int PROCESS_TIME_LOWER_BOUND = 500;

    @Autowired
    private ClaimProcessRepository claimProcessRepo;

    @Autowired
    private CustomerSrvClient customerSrvClient;

    private MeterRegistry meterRegistry;

    private Counter approvedClaimCounter;
    private Counter needFollowupClaimCounter;
    private Counter declinedClaimCounter;

    public ClaimReviewService(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        this.approvedClaimCounter = this.meterRegistry.counter("claim.result", "status", "approved");
        this.needFollowupClaimCounter = this.meterRegistry.counter("claim.result", "status", "need-followup");
        this.declinedClaimCounter = this.meterRegistry.counter("claim.result", "status", "declined");
    }

    public ClaimReviewResult processClaimRequest(ClaimRequest claimRequest) {
        log.info("Claim request processing - start - {}", claimRequest.toString());
        long start = System.currentTimeMillis();

        // Simulate claim process
        try {
            Random random = new Random();
            long processingTime = PROCESS_TIME_LOWER_BOUND + random.nextInt(500);
            Thread.sleep(processingTime);

            Faker faker = new Faker();

            Customer customer = customerSrvClient.getCustomer(claimRequest.getCustomerId());

            Status claimStatus = evaluateClaimRequest(claimRequest, customer);

            ClaimReviewResult result = ClaimReviewResult.builder()
                    .customerId(claimRequest.getCustomerId())
                    .claimId(claimRequest.getId())
                    .status(claimStatus)
                    .product(claimRequest.getProduct())
                    .claimAmount(claimRequest.getClaimAmount())
                    .remarks(faker.lorem().sentence())
                    .build();

            result = claimProcessRepo.save(result);

            updateClaimStatusMetric(result);

            long end = System.currentTimeMillis();
            log.info("Claim request processing - end - processing time {}, claim = {}, status = {}", (end - start), claimRequest.toString(), result.getStatus());

            return result;

        } catch (InterruptedException e) {
            long end = System.currentTimeMillis();
            log.error("claim process failed. processing time = {}, claim = {}", (end - start), claimRequest.toString());
            throw new RuntimeException("claim process failed. id = " + claimRequest.getId());
        }
    }

    private Status evaluateClaimRequest(ClaimRequest claimRequest, Customer customer) {

        if (customer.getRisk().equals(Risk.HIGH)) {
            return Status.DECLINED;
        } else if (customer.getRisk().equals(Risk.MEDIUM)) {
            return Status.NEED_FOLLOW_UP;
        } else {
            if (claimRequest.getProduct() == Product.MEDICAL) {
                if (claimRequest.getClaimAmount() < 5000) {
                    return Status.APPROVED;
                } else {
                    return Status.DECLINED;
                }
            }
        }

       return Status.NEED_FOLLOW_UP;
    }

    private void updateClaimStatusMetric(ClaimReviewResult result) {
        if (result.getStatus().equals(Status.APPROVED)) {
            approvedClaimCounter.increment();
        } else if (result.getStatus().equals(Status.DECLINED)) {
            declinedClaimCounter.increment();
        } else if (result.getStatus().equals(Status.NEED_FOLLOW_UP)) {
            needFollowupClaimCounter.increment();
        }
    }
}
