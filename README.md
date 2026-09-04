# SPRING PLUS

## 구현 범위

- Level 1
    - @Transactional 저장 오류 수정
    - User nickname 추가 및 JWT Claim 반영
    - JPQL 기반 weather / 수정일 기간 검색
    - Todo Controller 테스트 수정
    - 관리자 권한 변경 AOP 수정

- Level 2
    - JPA Cascade를 활용한 Todo 작성자 담당자 자동 등록
    - Comment 조회 N+1 개선
    - findByIdWithUser QueryDSL 전환 및 N+1 개선
    - Spring Security 기반 JWT 인증/인가 전환

- Level 3
    - QueryDSL + Projection + Paging 일정 검색 API
    - REQUIRES_NEW를 이용한 매니저 요청 로그 독립 트랜잭션
    - 익명 WebSocket/STOMP 실시간 채팅
    - 100만 User JDBC Batch Insert 및 nickname 조회 성능 개선

## 대용량 데이터 처리 및 조회 성능 개선

### 1. 목표

대용량 데이터 환경에서 데이터 저장 및 조회 성능을 확인하기 위해 테스트 코드로 User 데이터 1,000,000건을 생성했습니다.

데이터 저장에는 JDBC Batch Insert를 사용했으며, 닉네임 정확 일치 검색 API를 구현한 뒤 MySQL의 `EXPLAIN`, `EXPLAIN ANALYZE`를 이용하여 인덱스 적용 전후의 조회 성능을 비교했습니다.

---

### 2. 100만 건 User 데이터 생성

대용량 데이터를 한 번에 메모리에 생성하지 않고 일정한 크기로 나누어 생성 및 저장하도록 구성했습니다.

- 전체 생성 데이터: 1,000,000건
- 데이터 생성 단위: 40,000건
- 트랜잭션 단위: 20,000건
- JDBC Batch Size: 20,000건
- 데이터베이스: MySQL 8.4
- 저장 방식: `JdbcTemplate.batchUpdate()`

40,000건씩 User 데이터를 생성한 뒤 20,000건 단위로 트랜잭션을 분리하여 저장했습니다.

이를 통해 100만 개의 User 객체를 한 번에 메모리에 적재하는 것을 피하고, 하나의 지나치게 큰 트랜잭션으로 데이터를 저장하는 것도 방지했습니다.

#### 닉네임 생성 방식

닉네임은 다음 요소를 조합하여 랜덤으로 생성했습니다.

- 과일 이름
- 영문 소문자 및 숫자로 구성된 랜덤 문자열
- 동일한 기본 닉네임이 생성된 횟수

예시:

```text
사과4h8r5_1
포도7a2c1_1
사과4h8r5_2
````

생성 과정에서는 `Map<String, Integer>`를 이용해 동일한 기본 닉네임의 발생 횟수를 관리하여 가능한 한 닉네임 중복을 줄였습니다.

이메일은 각 테스트 데이터가 중복되지 않도록 순번을 이용해 생성했습니다.

```text
dummy1@test.local
dummy2@test.local
...
dummy1000000@test.local
```

#### 100만 건 저장 결과

MySQL 환경에서 1,000,000건의 데이터가 정상적으로 저장되는 것을 확인했습니다.

```text
저장 완료: 1,000,000 / 1,000,000
1,000,000건 저장 완료 - 소요 시간: 27,360 ms (27.36 sec)
```

---

### 3. 닉네임 정확 일치 검색 API

닉네임을 조건으로 User 목록을 조회하는 API를 구현했습니다.

```http
GET /users/search?nickname={nickname}
```

부분 일치 검색이 아닌 정확히 동일한 닉네임만 조회하도록 구현했습니다.

응답에서는 비밀번호와 같은 불필요하거나 민감한 정보를 제외하고 다음 정보만 반환합니다.

```json
[
  {
    "id": 500000,
    "nickname": "딸기4ae4z_1"
  }
]
```

---

### 4. 최초 조회 성능 - 인덱스 없음

먼저 `nickname` 컬럼에 별도의 인덱스를 생성하지 않은 상태에서 조회 성능을 측정했습니다.

```sql
SELECT id, email, nickname
FROM users
WHERE nickname = '딸기4ae4z_1';
```

`EXPLAIN` 결과 `type = ALL`이 확인되었으며, nickname 검색에 사용할 수 있는 인덱스가 존재하지 않아 전체 테이블을 탐색했습니다.

```text
type: ALL
key: NULL
rows: 약 993,289
Extra: Using where
```

`EXPLAIN ANALYZE`에서도 다음과 같이 Table Scan이 확인되었습니다.

```text
Table scan on users
rows=1e+6
```

반복 측정 결과 실제 실행 시간은 대략 **평균 약 367.6ms 수준**이었습니다.

즉 닉네임 하나를 찾기 위해 약 100만 건의 데이터를 탐색해야 했습니다.

---

### 5. 1차 최적화 - nickname 인덱스

정확 일치 검색의 탐색 범위를 줄이기 위해 `nickname` 컬럼에 B-Tree 인덱스를 적용했습니다.

```sql
CREATE INDEX idx_users_nickname
ON users (nickname);
```

인덱스 적용 후 `EXPLAIN` 결과는 다음과 같이 변경되었습니다.

```text
type: ref
key: idx_users_nickname
rows: 1
```

`EXPLAIN ANALYZE`에서도 전체 테이블 탐색 대신 다음과 같이 Index Lookup이 사용되는 것을 확인했습니다.

```text
Index lookup on users using idx_users_nickname
```

5회 반복 측정 결과 조회 시간은 약 **0.02~0.04ms 수준**으로 감소했습니다.

따라서 전체 테이블을 탐색하던 방식과 비교하여 매우 큰 성능 개선을 확인할 수 있었습니다.

---

### 6. 2차 최적화 실험 - Covering Index

추가적인 조회 성능 개선 가능성을 확인하기 위해 `nickname`, `email`을 이용한 복합 인덱스를 생성했습니다.

```sql
CREATE INDEX idx_users_nickname_email
ON users (nickname, email);
```

조회 대상인 `id`, `email`, `nickname` 중 MySQL InnoDB의 Secondary Index에는 Primary Key 값도 함께 저장되므로 해당 인덱스만으로 조회 결과를 구성할 수 있는 Covering Index 사용 가능성을 확인했습니다.

그러나 일반 조회에서 MySQL Optimizer는 기존의 `idx_users_nickname` 인덱스를 선택했습니다.

따라서 실험 목적으로 다음과 같이 복합 인덱스를 강제로 사용하여 실행 계획과 성능을 비교했습니다.

```sql
SELECT id, email, nickname
FROM users FORCE INDEX (idx_users_nickname_email)
WHERE nickname = '딸기4ae4z_1';
```

`EXPLAIN`에서는 다음과 같이 확인되었습니다.

```text
key: idx_users_nickname_email
Extra: Using index
```

`EXPLAIN ANALYZE`에서도 다음과 같이 Covering Index Lookup이 사용되었습니다.

```text
Covering index lookup on users using idx_users_nickname_email
```

5회 반복 측정 결과 약 **0.02~0.03ms 수준**의 조회 시간이 확인되었습니다.

---

### 7. 조회 성능 비교

| 단계        | 조회 방식                   | 실행 계획                 | 측정 시간         |
|-----------|-------------------------|-----------------------|---------------|
| 최초 조회     | 인덱스 없음                  | Table Scan            | 평균 약 367.6ms  |
| 1차 최적화    | nickname 단일 인덱스         | Index Lookup          | 약 0.02~0.04ms |
| 2차 최적화 실험 | nickname + email 복합 인덱스 | Covering Index Lookup | 약 0.02~0.03ms |

---

### 8. 성능 개선 결과 및 판단

가장 큰 성능 개선은 `nickname` 단일 인덱스를 적용했을 때 발생했습니다.

인덱스가 없을 때는 약 100만 건을 대상으로 Table Scan이 발생했지만, nickname 인덱스를 적용한 뒤에는 검색 대상에 직접 접근하는 Index Lookup으로 실행 계획이 변경되었습니다.

반면 `(nickname, email)` 복합 인덱스를 이용한 Covering Index 실험에서는 단일 인덱스보다 명확한 추가 성능 향상이 확인되지는 않았습니다.

또한 MySQL Optimizer 역시 별도의 힌트가 없는 일반 조회에서는 기존 `idx_users_nickname` 인덱스를 선택했습니다.

따라서 이번 조회 요구사항에서는 **nickname 단일 인덱스만으로도 충분히 높은 조회 성능을 확보할 수 있다고 판단했습니다.**

복합 인덱스는 저장 공간 증가와 INSERT/UPDATE 시 인덱스 관리 비용도 발생하므로, 단순히 인덱스를 많이 추가하기보다는 실제 쿼리 패턴과 실행 계획을 확인한 뒤 필요한 인덱스만 적용하는 것이 적절하다고 판단했습니다.

---

### 9. 테스트 환경 분리

100만 건 Bulk Insert 테스트는 일반적인 단위/통합 테스트와 달리 MySQL 환경과 대량의 데이터를 필요로 하기 때문에 `performance` 태그를 사용하여 일반 테스트와 분리했습니다.

```java
@Tag("performance")
```

일반 Gradle 테스트에서는 해당 태그를 제외하여 대용량 성능 테스트가 매번 자동으로 실행되지 않도록 구성했습니다.

```gradle
tasks.named('test') {
    useJUnitPlatform {
        excludeTags 'performance'
    }
}
```

이를 통해 일반 테스트는 외부 MySQL 성능 테스트 환경에 의존하지 않고 실행할 수 있으며, 대용량 데이터 테스트가 필요한 경우 별도로 실행할 수 있도록 구성했습니다.
