package com.soybeany.system.cache.demo.manager.repository;

import com.soybeany.system.cache.demo.manager.model.CacheServerInfo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * @author Soybeany
 * @date 2020/12/23
 */
public interface CacheServerInfoRepository extends JpaRepository<CacheServerInfo, Long> {

    Optional<CacheServerInfo> findByHost(String host);

}
