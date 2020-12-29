package com.soybeany.system.cache.demo.manager.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * @author Soybeany
 * @date 2020/12/25
 */
public interface SecretKeyEntityRepository extends JpaRepository<SecretKeyEntity, Long> {

    Optional<SecretKeyEntity> findByKey(String key);

}
