package com.jsp.Allocation.Service;

import com.jsp.Allocation.Dto.AppResponseDto;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;

public interface AllocationService {
    AppResponseDto processAllocation(List<Map<String, Object>> grantList);
    AppResponseDto processGetAllGrantsByPlanUd(BigInteger planService);
}
