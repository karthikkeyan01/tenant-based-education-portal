package com.fts.tenantbasededuportal.repository;

import com.fts.tenantbasededuportal.entity.Organization;
import com.fts.tenantbasededuportal.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User,String> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    List<User> findByOrganization(Organization organization);

    boolean existsByOrganization(Organization organization);
}
