package db.migration;

import org.codeforamerica.shiba.TinkEncryptor;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.Map;

public class V9__EncryptExistingData extends BaseJavaMigration {
    public void migrate(Context context) throws GeneralSecurityException, IOException {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(new SingleConnectionDataSource(context.getConnection(), true));
        TinkEncryptor tinkEncryptor = new TinkEncryptor(System.getenv("ENCRYPTION_KEY"));
        List<Map<String, Object>> records = jdbcTemplate.queryForList("SELECT id, data FROM applications");

        SqlParameterSource[] sqlParameterSources = records.stream()
                .map(record -> new MapSqlParameterSource("encryptedData", tinkEncryptor.encrypt((String) record.get("data")))
                        .addValue("id", record.get("id")))
                .toArray(SqlParameterSource[]::new);
        NamedParameterJdbcTemplate namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
        namedParameterJdbcTemplate.batchUpdate(
                "UPDATE applications " +
                        "SET encrypted_data = :encryptedData " +
                        "WHERE id = :id", sqlParameterSources);
    }
}
