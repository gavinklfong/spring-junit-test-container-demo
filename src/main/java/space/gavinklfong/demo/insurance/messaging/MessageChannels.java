package space.gavinklfong.demo.insurance.messaging;

import org.springframework.cloud.stream.annotation.Input;
import org.springframework.cloud.stream.annotation.Output;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.SubscribableChannel;

public interface MessageChannels {

    static final String CLAIM_REQ_INPUT = "claimReqEventInput";

    static final String CLAIM_STATUS_OUTPUT = "claimStatusEventOutput";

    @Input(CLAIM_REQ_INPUT)
    SubscribableChannel claimReqInputEventChannel();

    @Output (CLAIM_STATUS_OUTPUT)
    MessageChannel claimStatusOutputEventChannel();

}
