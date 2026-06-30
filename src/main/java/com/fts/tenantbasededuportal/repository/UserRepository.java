package com.fts.tenantbasededuportal.repository;

import com.fts.tenantbasededuportal.entity.Organization;
import com.fts.tenantbasededuportal.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User,String> {

    Page<User> findByActiveTrueAndIdNot(String userId,
            Pageable pageable);

    Page<User> findByOrganizationAndActiveTrueAndIdNot(Organization organization,
            String userId,
            Pageable pageable);

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    List<User> findByOrganization(Organization organization);

    Optional<User> findByIdAndActiveTrue(String id);

    List<User> findByActiveTrue();

    List<User> findByOrganizationId(String id);

    List<User> findByOrganizationAndActiveTrue(Organization organization);

    boolean existsByOrganizationAndActiveTrue(Organization organization);

    Optional<User> findByActivationToken(String activationToken);

    Optional<User> findByResetPasswordToken(String resetPasswordToken);

    Optional<User> findByEmailAndActiveTrue(String email);
}
