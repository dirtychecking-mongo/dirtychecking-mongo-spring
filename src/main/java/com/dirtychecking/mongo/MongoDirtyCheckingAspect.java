package com.dirtychecking.mongo;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class MongoDirtyCheckingAspect {
    private final MongoPersistenceContext persistenceContext;
    private final ApplicationContext applicationContext;

    @AfterReturning("@annotation(org.springframework.transaction.annotation.Transactional) || " +
            "@within(org.springframework.transaction.annotation.Transactional)")
    public void performDirtyChecking() {
        log.info("더티 체킹 수행 중...");

        List<String> entityNames = new ArrayList<>();
        for (Object entity : persistenceContext.getAllEntities()) {
            entityNames.add(entity.getClass().getSimpleName());
        }
        log.info("현재 스냅샷에 있는 엔티티들: {}", entityNames);

        for (Object entity : persistenceContext.getAllEntities()) {
            boolean isDirty = persistenceContext.isDirty(entity);
            log.info("엔티티 {} 더티 체크 결과: {}",
                    entity.getClass().getSimpleName(),
                    isDirty
            );

            if (isDirty) {
                log.info("변경 감지됨: {}", entity);
                saveEntity(entity);
            }
        }

        persistenceContext.clear(); // 스냅샷 초기화
    }

    // 엔티티 타입에 맞는 저장소를 찾아 엔티티를 저장하는 메서드
    @SuppressWarnings("unchecked")
    private void saveEntity(Object entity) {
        Class<?> entityClass = entity.getClass();
        String repositoryBeanName = getRepositoryBeanName(entityClass);

        try {
            // 적절한 저장소 빈을 찾아서 가져옴
            MongoRepository<Object, String> repository =
                    (MongoRepository<Object, String>) applicationContext.getBean(repositoryBeanName);

            // 엔티티 저장
            repository.save(entity);
            log.info("엔티티 {} 자동 저장 완료", entityClass.getSimpleName());
        } catch (NoSuchBeanDefinitionException e) {
            log.warn("엔티티 {}에 대한 저장소를 찾을 수 없음: {}",
                    entityClass.getSimpleName(), repositoryBeanName);
        } catch (Exception e) {
            log.error("엔티티 {} 저장 중 오류 발생: {}",
                    entityClass.getSimpleName(), e.getMessage());
        }
    }

    // 엔티티 클래스에 해당하는 저장소 빈 이름을 생성
    private String getRepositoryBeanName(Class<?> entityClass) {
        String entityName = entityClass.getSimpleName();
        // 일반적인 네이밍 규칙: UserRepository, RegionRepository 등
        return Character.toLowerCase(entityName.charAt(0)) +
                entityName.substring(1) + "Repository";
    }
}