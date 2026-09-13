package com.mim736.qvs.repo;

import com.mim736.qvs.domain.Role;
import com.mim736.qvs.domain.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserAccountRepository extends JpaRepository<UserAccount, Long> {

    Optional<UserAccount> findByUsername(String username);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    boolean existsByRoleAndFullNameIgnoreCase(Role role, String fullName);

    List<UserAccount> findByRoleOrderByFullNameAsc(Role role);

    @Query("SELECT u FROM UserAccount u WHERE u.role = :role AND u.institution.code = :code ORDER BY u.fullName ASC")
    List<UserAccount> findStudentsAtInstitution(@Param("role") Role role, @Param("code") String code);

    @Query("SELECT COUNT(u) FROM UserAccount u WHERE u.role = :role AND u.institution.code = :code")
    long countStudentsAtInstitution(@Param("role") Role role, @Param("code") String code);

    List<UserAccount> findAllByOrderByRoleAscFullNameAsc();
}
