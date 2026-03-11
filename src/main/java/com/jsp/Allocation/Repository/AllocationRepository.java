package com.jsp.Allocation.Repository;

import com.jsp.Allocation.Entity.AllocationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigInteger;

public interface AllocationRepository extends JpaRepository<AllocationEntity, BigInteger> {
}
