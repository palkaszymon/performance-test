import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Simulation;
import io.gatling.javaapi.http.HttpProtocolBuilder;

import java.util.concurrent.ThreadLocalRandom;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.http;
import static io.gatling.javaapi.http.HttpDsl.status;

public class TestSimulation extends Simulation {

    private static final String BASE_URL = "http://18.159.59.32:8080";
    HttpProtocolBuilder httpProtocol = http
            .baseUrl(BASE_URL)
            .acceptHeader("application/json")
            .contentTypeHeader("application/json");

    ScenarioBuilder scn = scenario("Create new product with random data")
            .exec(session -> {
                // Generate random data for each request
                String name = "Product_" + ThreadLocalRandom.current().nextInt(1, 9999999);
                String description = "Description_" + name;
                double price = ThreadLocalRandom.current().nextDouble(10.0, 10000.0);

                // Properly formatted JSON payload
                String jsonPayload = "{\"name\": \"" + name + "\", " +
                        "\"description\": \"" + description + "\", " +
                        "\"category\": \"Laptop\", " +
                        "\"price\": " + price + "}";


                return session.set("payload", jsonPayload);
            })
            .exec(http("Post new product")
                    .post("/api/products")
                    .body(StringBody("${payload}"))
                    .check(status().is(200)));


    public TestSimulation() {
        this.setUp(scn.injectOpen(nothingFor(5), constantUsersPerSec(100).during(6000)).protocols(httpProtocol));
    }
}
