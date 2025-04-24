package com.example.cecv_e_commerce.repository;

import com.example.cecv_e_commerce.domain.model.MailLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MailLogRepository extends JpaRepository<MailLog, Integer> {
}
