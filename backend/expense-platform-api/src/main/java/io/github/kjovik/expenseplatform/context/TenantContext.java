package io.github.kjovik.expenseplatform.context;

import java.util.UUID;

public class TenantContext {
    private static final ThreadLocal<UUID> TENANT_ID = new ThreadLocal<>();
    private static final ThreadLocal<UUID> USER_ID = new ThreadLocal<>();
    private static final ThreadLocal<String> ROLE = new ThreadLocal<>();

     public static UUID getTenantId() {
        return TENANT_ID.get();
    }
    public static UUID getUserId() {
        return USER_ID.get();
    }
    public static String getRole() {
        return ROLE.get();
    }
    public static void setTenantId(UUID id) {
        TENANT_ID.set(id);
    }
    public static void setUserId(UUID id) {
        USER_ID.set(id);
    }
    public static void setRole(String id) {
        ROLE.set(id);
    }

    public static void clear(){
        TENANT_ID.remove();
        USER_ID.remove();
        ROLE.remove();
    }
}
