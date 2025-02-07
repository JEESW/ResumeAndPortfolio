package com.example.resumeandportfolio.model.entity.global;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import java.time.LocalDateTime;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/**
 * BaseEntity
 *
 * @author Ji-Seungwoo
 * @version 1.0
 */

@Getter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity {

    // 생성 일자
    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    // 수정 일자
    @LastModifiedDate
    private LocalDateTime updatedAt;
}