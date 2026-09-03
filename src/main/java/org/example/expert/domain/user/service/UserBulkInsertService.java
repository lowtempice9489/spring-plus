package org.example.expert.domain.user.service;

import lombok.RequiredArgsConstructor;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.repository.UserBulkRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserBulkInsertService {

    private static final int TRANSACTION_SIZE = 20_000;

    private final UserBulkRepository userBulkRepository;
    private final TransactionTemplate transactionTemplate;

    public void bulkInsert(List<User> users) {
        for (int start = 0; start < users.size(); start += TRANSACTION_SIZE) {
            int end = Math.min(start + TRANSACTION_SIZE, users.size());
            List<User> batch = users.subList(start, end);

            transactionTemplate.executeWithoutResult(status ->
                    userBulkRepository.bulkInsert(batch)
            );
        }
    }
}