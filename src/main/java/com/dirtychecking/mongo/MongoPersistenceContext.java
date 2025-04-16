package com.dirtychecking.mongo;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.aop.framework.Advised;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Component
public class MongoPersistenceContext {
    private final ObjectMapper objectMapper;
    private final Map<Object, Object> entitySnapshot = new HashMap<>();

    @Autowired
    public MongoPersistenceContext(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public void persist(Object entity) {
        if (entity == null) {
            return;
        }

        // Optional 타입이면 내부 값을 가져와서 처리
        if (entity instanceof Optional<?>) {
            ((Optional<?>) entity).ifPresent(this::persist);
            return;
        }

        // 스프링 데이터 프록시 엔티티 등은 처리하지 않기
        if (entity instanceof Proxy || entity instanceof Advised) {
            return;
        }

        try {
            entitySnapshot.put(entity, cloneEntity(entity));
        } catch (Exception e) {
        }
    }

    public boolean isDirty(Object entity) {
        Object original = entitySnapshot.get(entity);
        if (original == null) {
            return false;
        }

        try {
            String originalJson = objectMapper.writeValueAsString(original);
            String currentJson = objectMapper.writeValueAsString(entity);

            return !originalJson.equals(currentJson);
        } catch (Exception e) {
            return false; // 비교할 수 없는 경우 변경되지 않은 것으로 간주
        }
    }

    public void clear() {
        entitySnapshot.clear();
    }

    private Object cloneEntity(Object entity) {
        try {
            String json = objectMapper.writeValueAsString(entity);
            return objectMapper.readValue(json, entity.getClass());
        } catch (Exception e) {
            throw new RuntimeException("Failed to clone entity: " + e.getMessage(), e);
        }
    }

    public Iterable<Object> getAllEntities() {
        return entitySnapshot.keySet();
    }
}