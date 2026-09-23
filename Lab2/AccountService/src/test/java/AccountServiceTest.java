import fu.de200045.AccountService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class AccountServiceTest {

    private AccountService service;

    @BeforeEach
    void setUp() {
        service = new AccountService();
    }

    @ParameterizedTest(name = "Email hợp lệ: {0}")
    @ValueSource(strings = {
            "john@example.com",
            "alice.b@mail.co.uk",
            "carol_99@domain.io"
    })
    @DisplayName("isValidEmail trả về true với email đúng định dạng")
    void isValidEmail_ValidEmails_ReturnsTrue(String email) {
        assertTrue(service.isValidEmail(email));
    }

    @ParameterizedTest(name = "Email không hợp lệ: \"{0}\"")
    @CsvSource(value = {
            "bobmail.com",
            "missing@dot",
            "'@nodomain.com'",
            "' '",
            "NULL"
    }, nullValues = "NULL")
    @DisplayName("isValidEmail trả về false với email sai định dạng / null")
    void isValidEmail_InvalidEmails_ReturnsFalse(String email) {
        assertFalse(service.isValidEmail(email));
    }

    @ParameterizedTest(name = "Row {index}: ({0},{1},{2}) → {3}")
    @CsvFileSource(resources = "/test-data.csv", numLinesToSkip = 1)
    @DisplayName("registerAccount với dữ liệu từ test-data.csv")
    void registerAccount_FromCsv(String username, String password,
                                 String email, boolean expected) {
        boolean actual = service.registerAccount(username, password, email);
        assertEquals(expected, actual,
                () -> String.format("(%s,%s,%s) phải trả về %s",
                        username, password, email, expected));
    }

    @Test
    @DisplayName("registerAccount: password = 6 ký tự (biên dưới) → false")
    void registerAccount_PasswordExactly6_ReturnsFalse() {
        assertFalse(service.registerAccount("bob", "abcdef", "bob@mail.com"));
    }

    @Test
    @DisplayName("registerAccount: password = 7 ký tự (biên trên) → true")
    void registerAccount_PasswordExactly7_ReturnsTrue() {
        assertTrue(service.registerAccount("bob", "abcdefg", "bob@mail.com"));
    }

    @Test
    @DisplayName("registerAccount: tất cả tham số null → false")
    void registerAccount_AllNull_ReturnsFalse() {
        assertFalse(service.registerAccount(null, null, null));
    }
}