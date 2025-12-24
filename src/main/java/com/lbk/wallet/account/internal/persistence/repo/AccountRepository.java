package com.lbk.wallet.account.internal.persistence.repo;

import com.lbk.wallet.account.internal.persistence.entity.AccountEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AccountRepository extends JpaRepository<AccountEntity, String> {
    List<AccountEntity> findByUserId(String userId);

    Page<AccountEntity> findByUserId(String userId, Pageable pageable);

    Page<AccountEntity> findByUserIdAndTypeIgnoreCase(String userId, String type, Pageable pageable);
}
