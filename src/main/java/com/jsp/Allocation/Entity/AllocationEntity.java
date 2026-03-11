package com.jsp.Allocation.Entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

import java.math.BigInteger;
import java.util.Date;
@Data
@Entity
@Table(name = "allocation_master")
public class AllocationEntity {
    @Id
    @Column(name = "alt_key")
    private BigInteger altKey;
    @Column(name="grant_id")
    private BigInteger grantId;
    @Column(name="allocation_number")
    private BigInteger allocationNumber;
    @Column(name="allocation_date")
    private Date plannedAlloctionDate;
    @Column(name="allocation_year")
    private String allocationYear;
    private String status;
    @Column(name="created_date")
    private Date createdDate;
    @Column(name="modified_date")
    private Date modifiedDate;

}
