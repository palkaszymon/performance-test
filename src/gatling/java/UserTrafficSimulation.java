import io.gatling.javaapi.core.OpenInjectionStep;
import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Simulation;
import io.gatling.javaapi.http.HttpProtocolBuilder;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

import static io.gatling.javaapi.core.CoreDsl.StringBody;
import static io.gatling.javaapi.core.CoreDsl.rampUsersPerSec;
import static io.gatling.javaapi.core.CoreDsl.scenario;
import static io.gatling.javaapi.http.HttpDsl.http;
import static io.gatling.javaapi.http.HttpDsl.status;

public class UserTrafficSimulation extends Simulation {

    private static final String BASE_URL = "";
    private static final List<String> shippingMethods = List.of("Post", "Courier", "Packstation");
    HttpProtocolBuilder httpProtocol = http
            .baseUrl(BASE_URL)
            .acceptHeader("application/json")
            .contentTypeHeader("application/json");

    private OpenInjectionStep.RampRate.RampRateOpenInjectionStep postEndpointInjectionProfile() {
        int totalDesiredUserCount = 100;
        double userRampUpPerInterval = 10;
        double rampUpIntervalSeconds = 10;
        int totalRampUptimeSeconds = 100;
        int steadyStateDurationSeconds = 600;

        return rampUsersPerSec(userRampUpPerInterval / (rampUpIntervalSeconds / 60)).to(totalDesiredUserCount)
                .during(Duration.ofSeconds(totalRampUptimeSeconds + steadyStateDurationSeconds));
    }

    ScenarioBuilder scn = scenario("User Traffic Simulation")
            .exec(http("Get all products")
                    .get("/api/products")
                    .check(status().is(200)))
            .pause(5)
            .exec(http("Create new order")
                    .post("/api/orders")
                    .body(StringBody(session -> {
                        String email = "test+" + ThreadLocalRandom.current().nextInt(1, 999999) + "@example.com";
                        String address = "123 Main St";
                        String shippingMethod = shippingMethods.get(ThreadLocalRandom.current().nextInt(shippingMethods.size()));

                        int setSize = ThreadLocalRandom.current().nextInt(1, 6);
                        String productIdsJsonArray = LongStream.generate(() -> ThreadLocalRandom.current().nextLong(1, 100001))
                                .distinct()
                                .limit(setSize)
                                .mapToObj(Long::toString)
                                .collect(Collectors.joining(",", "[", "]"));

                        return String.format("""
                                {
                                    "email": "%s",
                                    "address": "%s",
                                    "shippingMethod": "%s",
                                    "productIds": %s
                                }""", email, address, shippingMethod, productIdsJsonArray);
                    }))
                    .check(status().is(200)));

    public UserTrafficSimulation() {
        this.setUp(scn.injectOpen(postEndpointInjectionProfile()).protocols(httpProtocol));
    }
}
