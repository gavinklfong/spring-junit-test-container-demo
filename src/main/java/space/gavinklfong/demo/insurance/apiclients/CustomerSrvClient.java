package space.gavinklfong.demo.insurance.apiclients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import space.gavinklfong.demo.insurance.dto.Customer;

@FeignClient(name = "customer-srv", url = "${app.customer-srv.url}")
public interface CustomerSrvClient {
    @RequestMapping(method = RequestMethod.GET, value = "/customers/{id}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    Customer getCustomer(@PathVariable(required = false) String id);
}
