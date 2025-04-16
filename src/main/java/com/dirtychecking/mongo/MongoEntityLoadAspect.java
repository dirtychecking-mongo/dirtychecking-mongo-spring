package com.dirtychecking.mongo;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class MongoEntityLoadAspect {
    private final MongoPersistenceContext persistenceContext;

    @AfterReturning(pointcut = "execution(* org.springframework.data.mongodb.repository.MongoRepository+.*(..))",
            returning = "result")
    public void persistEntityAfterLoad(Object result) {
        if (result == null) {
            return;
        }

        // Optional 객체 처리
        if (result instanceof Optional<?>) {
            Optional<?> optional = (Optional<?>) result;
            optional.ifPresent(this::persistIfNeeded);
            return;
        }

        // Iterable 객체 처리
        if (result instanceof Iterable<?>) {
            for (Object entity : (Iterable<?>) result) {
                persistIfNeeded(entity);
            }
            return;
        }

        // 일반 객체 처리
        persistIfNeeded(result);
    }

    private void persistIfNeeded(Object entity) {
        if (entity != null) {
            persistenceContext.persist(entity);
            log.info("엔티티 {} 스냅샷 저장 완료", entity.getClass().getSimpleName());
        }
    }
}
