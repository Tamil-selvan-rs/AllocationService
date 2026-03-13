package com.jsp.Allocation.Service;

import com.jsp.Allocation.Dto.AppResponseDto;
import com.jsp.Allocation.Entity.AllocationEntity;
import com.jsp.Allocation.Repository.AllocationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;

@Service
public class AllocationServiceImpl implements AllocationService {

    @Autowired
    private AllocationRepository repository;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public AppResponseDto processAllocation(List<Map<String, Object>> grantList) {
        try {
            List<AllocationEntity> list = grantList.stream().flatMap(map -> {
                BigInteger frequency = (map.get("frequency") != null) ? new BigInteger(map.get("frequency") + "") : new BigInteger("5");
                LocalDate date = LocalDate.parse(map.get("grantDate").toString());
                BigInteger grantNumber = new BigInteger(map.get("grantNumber").toString());
                BigInteger altKey = new BigInteger(map.get("altkey").toString());
                return IntStream.range(0, frequency.intValue()).mapToObj(i -> {
                    AllocationEntity allocationEntity = new AllocationEntity();
                    allocationEntity.setStatus("PENDING");
                    allocationEntity.setAllocationNumber(new BigInteger(grantNumber.intValue() / frequency.intValue() + ""));
                    allocationEntity.setAltKey(generateAltKey());
                    allocationEntity.setGrantId(altKey);
                    LocalDate newDate = date.plusYears(i + 1);
                    Date plannedDate = Date.from(newDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
                    allocationEntity.setPlannedAlloctionDate(plannedDate);
                    allocationEntity.setAllocationYear(newDate.getYear() + "");
                    allocationEntity.setCreatedDate(new Date());
                    return allocationEntity;
                });
            }).toList();
            repository.saveAll(list);
            return new AppResponseDto("200", null, "Success", list);
        } catch (Exception e) {
            return new AppResponseDto("500", e.getMessage(), "Failure", null);
        }

    }

    private BigInteger generateAltKey() {
        long value = Math.abs(ThreadLocalRandom.current().nextLong());
        return BigInteger.valueOf(value);
    }

    @Override
    public AppResponseDto processGetAllGrantsByPlanUd(BigInteger planId) {
        try {
            String sql = "SELECT g.*, sum(a.allocation_number) AS allocation_count FROM  allocation_master a LEFT OUTER JOIN emp_table_master g ON a.grant_id = g.alt_key WHERE g.plan_id = ? AND a.status = 'APPROVED' GROUP BY g.alt_key";
            List<Map<String, Object>> byPlanId = jdbcTemplate.queryForList(sql, planId);
            return new AppResponseDto("200",null,"success",byPlanId.isEmpty() ? "this planId not approved yet" : byPlanId );
        } catch (Exception e) {
            return new AppResponseDto("400", e.getMessage(), "Failed", null);
        }
    }

}