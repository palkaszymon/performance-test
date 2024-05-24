import io.gatling.javaapi.core.*;
import io.gatling.javaapi.http.HttpProtocolBuilder;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.http;
import static io.gatling.javaapi.http.HttpDsl.status;

public class UserTrafficSimulation extends Simulation {

   private static final String BASE_URL = "http://18.159.59.32:8080";
   private static final List<String> shippingMethods = List.of("Post", "Courier", "Packstation");
   HttpProtocolBuilder httpProtocol = http
           .baseUrl(BASE_URL)
           .acceptHeader("application/json")
           .contentTypeHeader("application/json");

   ScenarioBuilder scn = scenario("User Traffic Simulation")
           .exec(http("Get all products")
                   .get("/api/products")
                   .check(status().is(200)))
           .pause(5)
           .exec(http("Create new order")
                   .post("/api/orders")
                   .body(StringBody(session -> {
                       String email = "test+" + ThreadLocalRandom.current().nextInt(1000, 999999) + "@example.com";
                       String address = "123 Main St";
                       String shippingMethod = shippingMethods.get(ThreadLocalRandom.current().nextInt(shippingMethods.size()));

                       int setSize = ThreadLocalRandom.current().nextInt(1, 6);
                       String productIdsJsonArray = LongStream.generate(() -> ThreadLocalRandom.current().nextLong(1, 10001))
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
       this.setUp(scn.injectOpen(nothingFor(5), constantUsersPerSec(10).during(60)).protocols(httpProtocol));
   }
}