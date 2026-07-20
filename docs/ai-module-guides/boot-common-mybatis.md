# boot-common-mybatis

## 1. Module Purpose
`boot-common-mybatis` provides MyBatis-Plus integration defaults for the repository. It standardizes mapper scanning, pagination, optimistic locking, and reusable Lambda wrapper helpers.

## 2. Main Components
- `com.mhd.boot.common.mybatis.config.MybatisPlusConfig`
  - Registers pagination and optimistic-lock interceptors
  - Registers an ID generator based on local network information
- `com.mhd.boot.common.mybatis.domain.PageParam`
  - Standard page request object with validation
- `com.mhd.boot.common.mybatis.domain.PageResponse<T>`
  - Page response DTO used by upper layers when a shared page model is desired
- `com.mhd.boot.common.mybatis.wrapper.LambdaQueryWrapperX<T>`
  - Query wrapper with `IfPresent` methods
- `com.mhd.boot.common.mybatis.wrapper.LambdaUpdateWrapperX<T>`
  - Update wrapper with `IfPresent` methods
- `com.mhd.boot.common.mybatis.domain.PageResultUtils`
  - Helper for page result conversion

## 3. Boundaries and Non-Goals
This module does:
- configure MyBatis-Plus behavior consistently
- reduce boilerplate condition building
- standardize page request bounds
- support optimistic lock updates

This module does not:
- replace raw SQL for every possible reporting query
- implement data access logic itself
- manage multi-datasource routing rules by itself

## 4. Runtime Behavior
1. Application starts and scans mapper package from `mybatis-plus.mapperPackage`
2. `MybatisPlusConfig` registers pagination and optimistic-lock plugins
3. Service methods build `LambdaQueryWrapperX` or `LambdaUpdateWrapperX`
4. Mapper methods execute with wrapper-generated SQL
5. `PageParam` validation prevents out-of-range paging requests early

## 5. Validation and Wrapper Rules
`PageParam` enforces:
- `pageNo` not null, minimum 1
- `pageSize` not null, minimum 1, maximum 100

`LambdaQueryWrapperX` and `LambdaUpdateWrapperX` offer:
- `eqIfPresent`
- `likeIfPresent`
- `inIfPresent`
- `betweenIfPresent`
- `setIfPresent`

`betweenIfPresent` with an array requires exactly two values.

## 6. Exception Model
- `IllegalArgumentException` for wrapper misuse
- MyBatis/MyBatis-Plus runtime exceptions for mapper or SQL issues
- constraint violations are expected to surface through the web exception layer

## 7. Usage Guidance
Recommended:
- use `PageParam` for every list page entry point
- use `LambdaQueryWrapperX` for optional query conditions
- use `LambdaUpdateWrapperX` for sparse update payloads
- keep business logic in service layer, not in wrappers

Be careful with:
- large offset paging, because it can become expensive
- optimistic lock update conflicts, because concurrent writes can return zero affected rows
- overusing wrapper methods where a simple mapper method is enough

## 8. Minimal Example
```java
LambdaQueryWrapperX<UserEntity> query = new LambdaQueryWrapperX<UserEntity>()
        .likeIfPresent(UserEntity::getName, request.getName())
        .eqIfPresent(UserEntity::getStatus, request.getStatus())
        .betweenIfPresent(UserEntity::getCreateTime, request.getBeginTime(), request.getEndTime());

Page<UserEntity> page = userMapper.selectPage(new Page<>(request.getPageNo(), request.getPageSize()), query);
```

## 9. Extended Example
```java
LambdaUpdateWrapperX<UserEntity> update = new LambdaUpdateWrapperX<UserEntity>()
        .eq(UserEntity::getId, request.getId())
        .setIfPresent(UserEntity::getName, request.getName())
        .setIfPresent(UserEntity::getStatus, request.getStatus());

int rows = userMapper.update(null, update);
if (rows == 0) {
    throw new BusinessException(ErrorCodeEnum.FAIL, "update failed due to optimistic lock or missing row");
}
```

## 10. AI Reading Guidance
- Use `PageParam` and the `IfPresent` wrappers first; do not re-implement optional condition checks in every service.
- When paging fails, inspect the interceptor configuration before rewriting query code.
- When an update unexpectedly affects zero rows, verify optimistic lock behavior before changing the data model.

## 11. Testing Notes
- This module currently has no direct tests in the repository.
- If you change wrapper helper behavior, add unit tests for the `IfPresent` branches and the `betweenIfPresent` validation path.
