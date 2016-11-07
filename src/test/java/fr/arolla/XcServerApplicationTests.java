package fr.arolla;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

// The search algorithm works up from the package that contains the test
// until it finds a @SpringBootApplication or @SpringBootConfiguration annotated class.
// As long as you’ve structured your code in a sensible way your main configuration is usually found.
// http://docs.spring.io/spring-boot/docs/current/reference/html/boot-features-testing.html#boot-features-testing-spring-boot-applications-detecting-config
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class XcServerApplicationTests {

    private static final Logger LOG = LoggerFactory.getLogger(XcServerApplication.class);
    public static final String UTF_8 = "UTF8";

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    @SuppressWarnings("rawtypes")
    public void usecase() throws IOException {
        HttpServer clientServer = new HttpServer(s -> q("{'total':180.0}"));
        clientServer.start();

        registerClient("Carmen", "McCallum", "http://localhost:" + clientServer.getPort());
        waitForIncomingRequest(clientServer);

        List<Seller> sellers = querySellers();
        assertThat(sellers.stream().map(s -> s.name).collect(toList())).contains("Carmen");

        await()
                .atMost(5, TimeUnit.SECONDS)
                .until(() -> {
                    Seller seller = querySellers().stream().filter(s -> s.name.equals("Carmen")).findFirst().get();
                    assertThat(seller.online).isTrue();
                    assertThat(seller.cash).describedAs("Negative amount due to wrong response").isNegative();
                });
    }

    private List<Seller> querySellers() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", MediaType.APPLICATION_JSON_VALUE);
        ResponseEntity<String> response =
                restTemplate.exchange("/sellers",
                        HttpMethod.GET,
                        new HttpEntity<>(headers),
                        String.class);
        assertThat(response.getStatusCode().value()).isEqualTo(200);

        ObjectMapper mapper = new ObjectMapper();
        JavaType type = mapper.getTypeFactory().constructCollectionType(List.class, Seller.class);
        try {
            return mapper.readValue(response.getBody(), type);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void waitForIncomingRequest(HttpServer clientServer) throws IOException {
        clientServer.processRequest();
    }

    private void registerClient(String username, String password, String url) throws UnsupportedEncodingException {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", MediaType.APPLICATION_FORM_URLENCODED_VALUE);
        ResponseEntity<Map> response =
                restTemplate.exchange("/seller",
                        HttpMethod.POST,
                        new HttpEntity<>("name=" + URLEncoder.encode(username, UTF_8)
                                + "&password=" + URLEncoder.encode(password, UTF_8)
                                + "&url=" + URLEncoder.encode(url, UTF_8), headers),
                        Map.class);
        assertThat(response.getStatusCode().value()).isEqualTo(200);
    }

    private static String q(String s) {
        return s.replace('\'', '"');
    }

    public static class HttpServer {
        // Fri, 31 Dec 1999 23:59:59 GMT
        private final SimpleDateFormat df = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
        private final Function<String, String> handler;
        private ServerSocket serverSocket;

        public HttpServer(Function<String, String> handler) {
            this.handler = handler;
        }

        public int getPort() {
            return serverSocket.getLocalPort();
        }

        public void start() throws IOException {
            serverSocket = new ServerSocket(0);
        }

        private void processRequest() throws IOException {
            Socket clientSocket = serverSocket.accept();
            LOG.info("Client connected");

            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));

            StringBuilder b = new StringBuilder();
            String s;
            while ((s = in.readLine()) != null) {
                b.append(s);
                if (s.isEmpty()) {
                    break;
                }
            }
            LOG.info("Data received '{}'", b);

            String response = handler.apply(b.toString());

            out.write("HTTP/1.0 200 OK\r\n");
            out.write("Date: $DATE\r\n".replace("$DATE", df.format(new Date())));
            out.write("Server: Beuarrrgh\r\n");
            out.write("Content-Type: application/json\r\n");
            out.write("Content-Length: $LENGTH\r\n".replace("$LENGTH", "" + response.length()));
            out.write("\r\n");
            out.write(response);
            out.close();

            LOG.info("Request handled");
            in.close();
            clientSocket.close();
        }
    }

    public static class Seller {
        // {"name":"al","cash":0.0,"online":false}
        public String name;
        public double cash;
        public boolean online;
    }
}
