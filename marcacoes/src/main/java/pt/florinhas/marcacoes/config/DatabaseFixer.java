package pt.florinhas.marcacoes.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class DatabaseFixer implements CommandLineRunner {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) throws Exception {
        try {
            System.out.println("EXECUTING DATABASE FIX: Adding 'activo' column to 'funcionario' table...");
            // Use DEFAULT TRUE to ensure existing employees can still login
            jdbcTemplate.execute("ALTER TABLE funcionario ADD COLUMN IF NOT EXISTS activo BOOLEAN DEFAULT TRUE;");
            System.out.println("DATABASE FIX EXECUTED SUCCESSFULLY.");
        } catch (Exception e) {
            System.out.println("DATABASE FIX FAILED (might already exist or other error): " + e.getMessage());
        }
    }
}
