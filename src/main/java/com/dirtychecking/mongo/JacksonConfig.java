package com.dirtychecking.mongo;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;

import java.io.IOException;

@Configuration
public class JacksonConfig {

    @Bean
    public Module customGeoJsonModule() {
        SimpleModule module = new SimpleModule();

        module.addSerializer(GeoJsonPoint.class, new JsonSerializer<GeoJsonPoint>() {
            @Override
            public void serialize(GeoJsonPoint value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
                gen.writeStartObject();
                gen.writeStringField("type", "Point");
                gen.writeFieldName("coordinates");
                gen.writeStartArray();
                gen.writeNumber(value.getX());
                gen.writeNumber(value.getY());
                gen.writeEndArray();
                gen.writeEndObject();
            }
        });

        module.addDeserializer(GeoJsonPoint.class, new JsonDeserializer<GeoJsonPoint>() {
            @Override
            public GeoJsonPoint deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
                JsonNode node = p.getCodec().readTree(p);
                if (node.has("coordinates")) {
                    JsonNode coords = node.get("coordinates");
                    double x = coords.get(0).asDouble();
                    double y = coords.get(1).asDouble();
                    return new GeoJsonPoint(x, y);
                }
                throw new IOException("Cannot deserialize GeoJsonPoint");
            }
        });

        return module;
    }
}
