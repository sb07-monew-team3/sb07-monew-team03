package com.example.monew.domain.interest.entity;

import com.example.monew.domain.base.BaseCreatableEntity;
import com.example.monew.domain.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinTable;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
@Table(name = "interests")
public class Interest extends BaseCreatableEntity {

    @Column(name="name", nullable = false, updatable = false, unique = true)
    private String name;

}
