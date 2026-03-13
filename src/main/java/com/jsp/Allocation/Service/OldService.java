package com.jsp.Allocation.Service;

import com.jsp.Allocation.Dto.AppResponseDto;
import com.jsp.Allocation.Entity.AllocationEntity;
import com.jsp.Allocation.Repository.AllocationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class OldService implements AllocationService {

    private AllocationRepository repository;

    @Override
    public AppResponseDto processAllocation(List<Map<String, Object>> grantList) {

        try {

            List<AllocationEntity> allocations = new ArrayList<>();

            for (Map<String, Object> list : grantList) {

                BigInteger altkey = new BigInteger(list.get("altkey").toString());
                BigInteger frequency = new BigInteger(list.get("frequency").toString());
                BigInteger grantNumber = new BigInteger(list.get("grantNumber").toString());

                Date grantDate = parseDate(list.get("grantDate").toString());

                BigInteger grantId = generateFreqKey(frequency, grantNumber);

                for (int i = 0; i < frequency.intValue(); i++) {

                    AllocationEntity entity = new AllocationEntity();

                    entity.setAltKey(generateAltKey());
                    entity.setStatus("Pending");
                    entity.setGrantId(altkey);
                    entity.setAllocationNumber(grantId);

                    LocalDateTime baseDate = grantDate.toInstant()
                            .atZone(ZoneId.systemDefault())
                            .toLocalDateTime();

                    entity.setAllocationYear(String.valueOf(baseDate.getYear() + i));
                    entity.setCreatedDate(new Date());
                    entity.setPlannedAlloctionDate(allocationDate(grantDate, i));

                    allocations.add(entity);
                }
            }

            List<AllocationEntity> saved = repository.saveAll(allocations);

            return new AppResponseDto("200", null, "Success", saved);

        } catch (Exception e) {
            return new AppResponseDto("500", e.getMessage(), "Error", null);
        }
    }

    @Override
    public AppResponseDto processGetAllGrantsByPlanUd(BigInteger planService) {
        return null;
    }

    private BigInteger generateAltKey() {
        long value = Math.abs(ThreadLocalRandom.current().nextLong());
        return BigInteger.valueOf(value);
    }

    private BigInteger generateFreqKey(BigInteger frequency, BigInteger grantNumber) {
        int i = (frequency.intValue() > 0) ? frequency.intValue() : 5;
        return BigInteger.valueOf(Math.abs(grantNumber.intValue() / i));
    }

    private Date allocationDate(Date grantDate, int count) {

        LocalDateTime dateTime = grantDate.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime()
                .plusYears(count);

        return Date.from(dateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

    private Date parseDate(String dateStr) {

        DateTimeFormatter formatter =
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS");

        LocalDateTime dateTime = LocalDateTime.parse(dateStr, formatter);

        return Date.from(
                dateTime.atZone(ZoneId.systemDefault()).toInstant()
        );
    }
}