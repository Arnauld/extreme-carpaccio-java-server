package fr.arolla;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * @author <a href="http://twitter.com/aloyer">@aloyer</a>
 */
@Configuration
public class XcServerConfiguration {

    @Autowired
    public XcServerConfiguration() {
    }

    @Bean
    @Primary
    public ObjectMapper configureMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, true);
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, true);

        JavaTimeModule module = new JavaTimeModule();
        mapper.registerModule(module);
        return mapper;
    }
}
