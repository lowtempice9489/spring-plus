package org.example.expert.domain.user;

import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.enums.UserRole;
import org.example.expert.domain.user.service.UserBulkInsertService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.junit.jupiter.api.Tag;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

@SpringBootTest
@ActiveProfiles("performance")
@Tag("performance")
class UserBulkInsertTest {

    private static final int TOTAL_COUNT = 1_000_000;
    private static final int GENERATION_SIZE = 40_000;

    private static final String[] FRUITS = {
            "사과", "포도", "복숭아", "딸기", "수박",
            "자두", "체리", "레몬", "망고", "오렌지"
    };

    @Autowired
    private UserBulkInsertService userBulkInsertService;

    @Test
    void bulkInsertOneMillionUsers() {
        Map<String, Integer> nicknameCounts = new HashMap<>();

        long startedAt = System.currentTimeMillis();

        for (int start = 1; start <= TOTAL_COUNT; start += GENERATION_SIZE) {
            int end = Math.min(start + GENERATION_SIZE - 1, TOTAL_COUNT);

            List<User> users = new ArrayList<>(end - start + 1);

            for (int sequence = start; sequence <= end; sequence++) {
                String nickname = createUniqueNickname(nicknameCounts);

                User user = new User(
                        "dummy" + sequence + "@test.local",
                        "dummy-password",
                        nickname,
                        UserRole.USER
                );

                users.add(user);
            }

            userBulkInsertService.bulkInsert(users);

            System.out.printf(
                    "저장 완료: %,d / %,d%n",
                    end,
                    TOTAL_COUNT
            );
        }

        long elapsedMillis = System.currentTimeMillis() - startedAt;

        System.out.printf(
                "%,d건 저장 완료 - 소요 시간: %,d ms (%.2f sec)%n",
                TOTAL_COUNT,
                elapsedMillis,
                elapsedMillis / 1000.0
        );
    }

    private String createUniqueNickname(Map<String, Integer> nicknameCounts) {
        String baseNickname = createRandomBaseNickname();

        int occurrence = nicknameCounts.merge(
                baseNickname,
                1,
                Integer::sum
        );

        return baseNickname + "_" + occurrence;
    }

    private String createRandomBaseNickname() {
        ThreadLocalRandom random = ThreadLocalRandom.current();

        String fruit = FRUITS[random.nextInt(FRUITS.length)];

        StringBuilder suffix = new StringBuilder(5);

        for (int i = 0; i < 5; i++) {
            if (random.nextBoolean()) {
                suffix.append((char) ('a' + random.nextInt(26)));
            } else {
                suffix.append(random.nextInt(10));
            }
        }

        return fruit + suffix;
    }
}