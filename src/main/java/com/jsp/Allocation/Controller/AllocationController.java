package com.jsp.Allocation.Controller;


import com.jsp.Allocation.Dto.AppResponseDto;
import com.jsp.Allocation.Service.AllocationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;

@Controller
public class AllocationController {
    @Autowired
    AllocationService allocationService;
    @PostMapping("/createAllocation")
    public @ResponseBody AppResponseDto getResponse(@RequestBody List<Map<String, Object>> grantList) {

        return allocationService.processAllocation(grantList);
    }
}
