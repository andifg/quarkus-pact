import static au.com.dius.pact.consumer.dsl.LambdaDsl.newJsonBody;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.HashMap;
import java.util.Map;

import jakarta.inject.Inject;
import jakarta.ws.rs.HttpMethod;
import jakarta.ws.rs.core.Response.Status;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.V4Pact;
import au.com.dius.pact.core.model.annotations.Pact;
import au.com.dius.pact.core.model.annotations.PactDirectory;
import io.quarkus.test.junit.QuarkusTest;

// <1>
@ExtendWith(PactConsumerTestExt.class)
@PactTestFor(providerName = "farm", port = "8085")
@PactDirectory("target/pacts")
@QuarkusTest // Needed to enable dependency injection of the rest client
public class ConsumerTest {

    // <2>
    @Inject
    Knitter knitter;

    @Pact(provider = "farm", consumer = "knitter")
    public V4Pact createPact(PactDslWithProvider builder) {

        Map<String, String> requestHeaders = new HashMap<>();
        requestHeaders.put("Accept", "application/json");

        Map<String, String> responseHeaders = new HashMap<>();
        responseHeaders.put("Content-Type", "application/json");

        var requestBody = newJsonBody(body -> body.stringType("colour").numberType("orderNumber")).build(); // <3>

        var woolResponseBody = newJsonBody(body -> body.stringValue("colour", "white")).build(); // <4>

        // <5>
        return builder.uponReceiving("post request").path("/wool/order").headers(requestHeaders).method(HttpMethod.POST)
                .body(requestBody).willRespondWith().status(Status.OK.getStatusCode()).headers(responseHeaders)
                .body(woolResponseBody)
                .toPact(V4Pact.class);

    }

    @Test
    public void testConsumption() { //<6>
        String knitted = knitter.knit("irrelevant"); // <7>
        assertEquals("a nice white sweater", knitted);
    }
}
