package integration.setup;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestComponent;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import space.gavinklfong.demo.insurance.dto.Customer;
import space.gavinklfong.demo.insurance.dto.Risk;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

@TestComponent
public class CustomerSrvSetup {

    @Autowired
    WireMockServer wireMockServer;

    private ObjectMapper objectMapper = new ObjectMapper();

    public void setUpCustomerForId(String customerId, Risk risk) throws JsonProcessingException {

        Customer customer = Customer.builder().id(customerId).risk(risk).build();

        wireMockServer.stubFor(get(urlEqualTo(String.format("/customers/%s", customer.getId())))
                .willReturn(aResponse()
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody(objectMapper.writeValueAsString(customer))));
    }

}


