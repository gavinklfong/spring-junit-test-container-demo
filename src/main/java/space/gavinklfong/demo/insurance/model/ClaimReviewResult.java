package space.gavinklfong.demo.insurance.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import space.gavinklfong.demo.insurance.dto.Product;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@NoArgsConstructor
@AllArgsConstructor
public class ClaimReviewResult {
    @Id
    String claimId;
    Product product;
    String customerId;
    Status status;
    Double claimAmount;
    String remarks;
}
