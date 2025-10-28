# Test SQL Scripts

This directory contains SQL scripts for test data management in the SchoolDay application.

## Files

- `clear_test_data.sql` - Clears test data while preserving base data
- `clear_all_data.sql` - Clears all data including base data
- `README.md` - This documentation file

## Automatic Cleanup

The base test classes (`BaseRepositoryTest` and `BaseServiceTest`) are configured with the `@Sql` annotation to automatically execute `clear_test_data.sql` after each test method:

```java
@Sql(scripts = "/sql/clear_test_data.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
```

This ensures that:
- Each test starts with a clean state
- Tests don't interfere with each other
- No need to create unique test data for each test
- Base data (users, roles, children) is preserved between tests

## Script Details

### clear_test_data.sql

This script clears test data while preserving the base data defined in `infra/db/sql/basedata.sql`:

- **Preserves**: Base users (user_1 through user_10), base roles (role_1 through role_3), base children (child_1 through child_5)
- **Clears**: Any test data created during tests
- **Order**: Respects foreign key constraints by deleting in the correct order

### clear_all_data.sql

This script completely clears all data from the test database:

- **Use case**: When you need complete isolation between tests
- **Method**: Temporarily disables foreign key checks to allow deletion in any order
- **Warning**: This removes all data including base data

## Usage Examples

### Standard Test (Automatic Cleanup)

```java
@Test
public void testUserCreation() {
    // This test can use any email - cleanup happens automatically
    User user = User.builder()
        .email("test@example.com")  // No need for unique values
        .firstName("Test")
        .lastName("User")
        .password("password")
        .userStatus(UserStatus.ACTIVE)
        .build();
    
    userRepository.save(user);
    // Test assertions...
    // Cleanup happens automatically after this test
}
```

### Test with Complete Isolation

```java
@Test
@Sql(scripts = "/sql/clear_all_data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
public void testWithCompleteIsolation() {
    // This test starts with a completely empty database
    // All data including base data is cleared
}
```

### Custom Cleanup

```java
@Test
@Sql(scripts = {
    "/sql/custom_setup.sql",
    "/sql/clear_test_data.sql"
}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
public void testWithCustomCleanup() {
    // Custom setup and cleanup
}
```

## Benefits

1. **Simplified Tests**: No need to generate unique values for each test
2. **Consistent Data**: Tests can use the same test data patterns
3. **Automatic Cleanup**: No manual cleanup code required
4. **Isolation**: Tests don't interfere with each other
5. **Performance**: Faster test execution with automatic cleanup

## Best Practices

1. **Use consistent test data**: Since cleanup is automatic, use the same test data patterns across tests
2. **Don't rely on test order**: Each test should be independent
3. **Use base data when possible**: Leverage the preserved base data for tests that need existing data
4. **Keep tests simple**: Focus on testing logic, not data setup/cleanup

## Troubleshooting

### Tests Still Failing with Unique Constraint Violations

1. Check that your test class extends `BaseRepositoryTest` or `BaseServiceTest`
2. Verify the `@Sql` annotation is present on the base class
3. Ensure the test database is properly set up with base data

### Cleanup Not Working

1. Check that the SQL script path is correct (`/sql/clear_test_data.sql`)
2. Verify the test database connection in `application.properties`
3. Check that foreign key constraints are properly handled

### Performance Issues

1. Consider using `@Transactional` on test methods for faster rollback
2. Use `clear_all_data.sql` only when complete isolation is needed
3. Group related tests to minimize cleanup overhead 