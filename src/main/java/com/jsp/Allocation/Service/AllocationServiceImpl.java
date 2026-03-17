package com.jsp.Allocation.Service;

import com.jsp.Allocation.Dto.AppResponseDto;
import com.jsp.Allocation.Entity.AllocationEntity;
import com.jsp.Allocation.Repository.AllocationRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.time.Instant;
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
    @Autowired
    private RestTemplate restTemplate;

    @Override
    public AppResponseDto processAllocation(List<Map<String, Object>> grantList) {
        try {

            List<AllocationEntity> list = grantList.stream().flatMap(map -> {
                BigInteger frequency = (map.get("frequency") != null) ? new BigInteger(map.get("frequency") + "") : new BigInteger("5");
                LocalDate date = Instant.parse(map.get("grantDate").toString())
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate();
                BigInteger grantNumber = new BigInteger(map.get("grantNumber").toString());
                BigInteger altKey = new BigInteger(map.get("altKey").toString());
                return IntStream.range(0, frequency.intValue()).mapToObj(i -> {
                    AllocationEntity allocationEntity = new AllocationEntity();
                    allocationEntity.setStatus("PENDING");
                    BigDecimal allocationValue = new BigDecimal(grantNumber)
                            .divide(new BigDecimal(frequency), RoundingMode.HALF_UP);
                    allocationEntity.setAllocationNumber(allocationValue.toBigInteger());
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
            processApproveGrants(grantList);
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
            return new AppResponseDto("200", null, "success", byPlanId.isEmpty() ? "this planId not approved yet" : byPlanId);
        } catch (Exception e) {
            return new AppResponseDto("400", e.getMessage(), "Failed", null);
        }
    }

    @Override
    @PostConstruct
    public void init() {
        try {
            BigInteger planId = getPlanId();
            List<Map<String, Object>> grantsForAllocation = getGrantsForAllocation(planId);
            processAllocation(grantsForAllocation);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private BigInteger getPlanId() {
        return new BigInteger(
                restTemplate.getForObject("http://localhost:8080/currentPlanId", AppResponseDto.class)
                        .getData().toString()
        );
    }

    private List<Map<String, Object>> getGrantsForAllocation(BigInteger planId) {
        ResponseEntity<AppResponseDto<List<Map<String, Object>>>> response =
                restTemplate.exchange(
                        "http://localhost:8080/findByPlanIdAndGrantStatusAndAllocationStatus/"
                                + planId + "/APPROVED/PENDING",
                        HttpMethod.GET,
                        null,
                        new ParameterizedTypeReference<AppResponseDto<List<Map<String, Object>>>>() {
                        }
                );
        return response.getBody().getData();
    }
    private void processApproveGrants(List<Map<String, Object>> grantList) {
        List<BigInteger> altKeys = grantList.stream()
                .map(map -> new BigInteger(map.get("altKey").toString()))
                .distinct()
                .toList();
        String url ="http://localhost:8080/acceptGrants";
        HttpEntity<List<BigInteger>> request = new HttpEntity<>(altKeys);

        restTemplate.exchange(url, HttpMethod.PUT, request, Void.class);
    }


}