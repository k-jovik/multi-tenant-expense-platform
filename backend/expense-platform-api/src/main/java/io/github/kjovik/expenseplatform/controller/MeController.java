package io.github.kjovik.expenseplatform.controller;


import io.github.kjovik.expenseplatform.context.TenantContext;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class MeController {

    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> me (){
        return ResponseEntity.ok(Map.of(
                "userId", TenantContext.getUserId(),
                "tenantId", TenantContext.getTenantId(),
                "role", TenantContext.getRole()
        ));
    }
}
