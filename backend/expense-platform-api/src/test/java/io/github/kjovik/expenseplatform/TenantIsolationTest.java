package io.github.kjovik.expenseplatform;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.*;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class TenantIsolationTest extends AbstractIntegrationTest{
    @Autowired
    JdbcTemplate jdbcTemplate;
    UUID tenantA;
    UUID tenantB;
    UUID userA;
    UUID userB;
    UUID expenseId1;
    UUID expenseId2;
    @BeforeEach
     public void seedData(){
        tenantA = UUID.randomUUID();
        tenantB = UUID.randomUUID();
        userA = UUID.randomUUID();
        userB = UUID.randomUUID();
        expenseId1 = UUID.randomUUID();
        expenseId2 = UUID.randomUUID();

        jdbcTemplate.execute("ALTER ROLE app_user WITH PASSWORD 'test'");
        jdbcTemplate.update("DELETE FROM journal_lines");
        jdbcTemplate.update("DELETE FROM journal_entries");
        jdbcTemplate.update("DELETE FROM accounts");
        jdbcTemplate.update("DELETE FROM expenses");
        jdbcTemplate.update("DELETE FROM users");
        jdbcTemplate.update("DELETE FROM tenants");

        jdbcTemplate.update("INSERT INTO tenants (id,name) VALUES (?,?)",
                    tenantA,"tenant-A");
        jdbcTemplate.update("INSERT INTO tenants (id,name) VALUES (?,?)",
                    tenantB,"tenant-B");

        jdbcTemplate.update("INSERT INTO users (id,tenant_id,email,password_hash,role,full_name) VALUES (?,?,?,?,?,?)",
                userA,tenantA,"a@x.com","hash","ADMIN","UserA");
        jdbcTemplate.update("INSERT INTO users (id,tenant_id,email,password_hash,role,full_name) VALUES (?,?,?,?,?,?)",
                userB,tenantB,"b@x.com","hash","ADMIN","UserB");

        jdbcTemplate.update("INSERT INTO expenses (id,tenant_id,user_id,amount_minor,category,status)VALUES (?,?,?,?,?,?)",
                expenseId1,tenantA,userA,1000L,"Travel","SUBMITTED");
        jdbcTemplate.update("INSERT INTO expenses (id,tenant_id,user_id,amount_minor,category,status)VALUES (?,?,?,?,?,?)",
                expenseId2,tenantB,userB,2000L,"Meals","SUBMITTED");


    }
    public Connection openAsAppUser() throws SQLException {
        return DriverManager.getConnection(postgres.getJdbcUrl(),"app_user","test");
    }
    public long countExpensesIn(Connection connection,String tenantId) throws SQLException{
        connection.rollback();
        connection.setAutoCommit(false);
        try (Statement s = connection.createStatement()){
            s.execute("SET LOCAL app.tenant_id = '" + tenantId + "'");
        }

        long count;
        try (Statement s = connection.createStatement();
             ResultSet rs = s.executeQuery("SELECT COUNT(*) FROM expenses")) {
            rs.next();
            count = rs.getLong(1);
        }
        connection.rollback();
        return count;
    }

    @Test
    void rls_isolates_tenants_by_session_variable() throws Exception {
        try (Connection conn = openAsAppUser()) {
            conn.setAutoCommit(false);

            assertThat(countExpensesIn(conn, tenantA.toString())).isEqualTo(1L);
            assertThat(countExpensesIn(conn, tenantB.toString())).isEqualTo(1L);
            assertThat(countExpensesIn(conn, "")).isZero();
        }
    }
}
