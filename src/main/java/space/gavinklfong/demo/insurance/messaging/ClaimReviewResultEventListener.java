package space.gavinklfong.demo.insurance.messaging;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import space.gavinklfong.demo.insurance.model.ClaimReviewResult;

@Slf4j
@Component
public class ClaimReviewResultEventListener {

//    @StreamListener(MessageChannels.CLAIM_STATUS_INPUT)
    public void handleClaimRequestEvent(ClaimReviewResult claimReviewResult) {
        log.info("Claim result generated: " + claimReviewResult);
    }

}

