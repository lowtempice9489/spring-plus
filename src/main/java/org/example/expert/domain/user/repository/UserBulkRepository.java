package org.example.expert.domain.user.repository;

import lombok.RequiredArgsConstructor;
import org.example.expert.domain.user.entity.User;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class UserBulkRepository {

    private static final int BATCH_SIZE = 20_000;

    private static final String INSERT_SQL = """
            INSERT INTO users (email, password, nickname, user_role, created_at, modified_at)
            VALUES (?, ?, ?, ?, ?, ?)
            """;

    private final JdbcTemplate jdbcTemplate;

    public void bulkInsert(List<User> users) {
        Timestamp now = Timestamp.valueOf(LocalDateTime.now());

        jdbcTemplate.batchUpdate(
                INSERT_SQL,
                users,
                BATCH_SIZE,
                (ps, user) -> {
                    ps.setString(1, user.getEmail());
                    ps.setString(2, user.getPassword());
                    ps.setString(3, user.getNickname());
                    ps.setString(4, user.getUserRole().name());
                    ps.setTimestamp(5, now);
                    ps.setTimestamp(6, now);
                }
        );
    }
}