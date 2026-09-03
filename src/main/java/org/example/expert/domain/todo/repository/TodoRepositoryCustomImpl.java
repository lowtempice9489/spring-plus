package org.example.expert.domain.todo.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.example.expert.domain.todo.dto.response.TodoSearchResponse;
import org.example.expert.domain.todo.entity.Todo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.example.expert.domain.comment.entity.QComment.comment;
import static org.example.expert.domain.manager.entity.QManager.manager;
import static org.example.expert.domain.todo.entity.QTodo.todo;
import static org.example.expert.domain.user.entity.QUser.user;

public class TodoRepositoryCustomImpl implements TodoRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    public TodoRepositoryCustomImpl(EntityManager entityManager) {
        this.queryFactory = new JPAQueryFactory(entityManager);
    }

    @Override
    public Optional<Todo> findByIdWithUser(Long todoId) {
        Todo result = queryFactory
                .selectFrom(todo)
                .leftJoin(todo.user, user)
                .fetchJoin()
                .where(todo.id.eq(todoId))
                .fetchOne();

        return Optional.ofNullable(result);
    }

    @Override
    public Page<TodoSearchResponse> searchTodos(
            String title,
            LocalDateTime startDate,
            LocalDateTime endDate,
            String managerNickname,
            Pageable pageable
    ) {
        BooleanBuilder conditions = new BooleanBuilder();

        if (title != null && !title.isBlank()) {
            conditions.and(todo.title.contains(title));
        }

        if (startDate != null) {
            conditions.and(todo.createdAt.goe(startDate));
        }

        if (endDate != null) {
            conditions.and(todo.createdAt.loe(endDate));
        }

        if (managerNickname != null && !managerNickname.isBlank()) {
            conditions.and(
                    JPAExpressions
                            .selectOne()
                            .from(manager)
                            .join(manager.user, user)
                            .where(
                                    manager.todo.eq(todo),
                                    user.nickname.contains(managerNickname)
                            )
                            .exists()
            );
        }

        List<TodoSearchResponse> content = queryFactory
                .select(
                        Projections.constructor(
                                TodoSearchResponse.class,
                                todo.title,
                                JPAExpressions
                                        .select(manager.count())
                                        .from(manager)
                                        .where(manager.todo.eq(todo)),
                                JPAExpressions
                                        .select(comment.count())
                                        .from(comment)
                                        .where(comment.todo.eq(todo))
                        )
                )
                .from(todo)
                .where(conditions)
                .orderBy(todo.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(todo.count())
                .from(todo)
                .where(conditions)
                .fetchOne();

        return new PageImpl<>(
                content,
                pageable,
                total != null ? total : 0L
        );
    }
}
