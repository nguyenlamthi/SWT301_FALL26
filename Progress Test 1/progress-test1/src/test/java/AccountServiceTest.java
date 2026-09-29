import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.stream.Stream;

import fu.de200045.Account;
import fu.de200045.AccountService;
import fu.de200045.AccountStatus;
import fu.de200045.ResultCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class AccountServiceTest {

    static final String USER = "alice_01";
    static final String EMAIL = "alice@example.com";
    static final String PASS = "Secret@123";
    static final String PHONE = "0912345678";
    static final LocalDate DOB = LocalDate.now().minusYears(20); // 20 tuổi, luôn hợp lệ dù chạy năm nào

    AccountService service;

    @BeforeEach
    void setUp() {
        service = new AccountService(); // mỗi test một service mới -> test độc lập
    }

    @Nested
    class Register {

        @Test
        void register_ValidData_CreatesActiveAccountWithHashedPassword() {
            ResultCode result = service.register(USER, EMAIL, PASS, PASS, DOB, PHONE);

            assertEquals(ResultCode.SUCCESS, result);
            Account acc = service.findByUsername(USER).orElseThrow();
            assertEquals(AccountStatus.ACTIVE, acc.getStatus());
            assertEquals(0, acc.getFailedAttempts());
            assertFalse(acc.isLocked());
            assertNotEquals(PASS, acc.getCurrentPasswordHash());
            assertEquals("alice@example.com", acc.getEmail());
        }

        @Test
        void register_UpperCaseEmail_StoredAsLowerCase() {
            service.register(USER, "ALICE@EXAMPLE.COM", PASS, PASS, DOB, PHONE);
            Account acc = service.findByUsername(USER).orElseThrow();
            assertEquals("alice@example.com", acc.getEmail());
        }

        @Test
        void register_TwoAccountsSamePassword_HaveDifferentSaltAndHash() {
            service.register(USER, EMAIL, PASS, PASS, DOB, PHONE);
            service.register("bob_02", "bob@example.com", PASS, PASS, DOB, PHONE);

            Account alice = service.findByUsername(USER).orElseThrow();
            Account bob = service.findByUsername("bob_02").orElseThrow();
            assertNotEquals(alice.getSalt(), bob.getSalt());
            assertNotEquals(alice.getCurrentPasswordHash(), bob.getCurrentPasswordHash());
        }

        @ParameterizedTest(name = "[{index}] {0}")
        @MethodSource("AccountServiceTest#invalidRegisterInputs")
        void register_InvalidInput_ReturnsExpectedCode(String desc, String u, String e, String p, String c,
                                                       LocalDate dob, String phone, ResultCode expected) {
            assertEquals(expected, service.register(u, e, p, c, dob, phone));
            assertTrue(service.findByUsername(u).isEmpty());
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {" "})
        void register_UsernameNullEmptyBlank_ReturnsInvalidInput(String username) {
            assertEquals(ResultCode.INVALID_INPUT, service.register(username, EMAIL, PASS, PASS, DOB, PHONE));
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {" "})
        void register_EmailNullEmptyBlank_ReturnsInvalidInput(String email) {
            assertEquals(ResultCode.INVALID_INPUT, service.register(USER, email, PASS, PASS, DOB, PHONE));
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {" "})
        void register_PasswordNullEmptyBlank_ReturnsInvalidInput(String password) {
            assertEquals(ResultCode.INVALID_INPUT, service.register(USER, EMAIL, password, password, DOB, PHONE));
        }

        @Test
        void register_DuplicateUsernameIgnoreCase_ReturnsDuplicateUsername() {
            service.register(USER, EMAIL, PASS, PASS, DOB, PHONE);
            ResultCode result = service.register("ALICE_01", "other@example.com", PASS, PASS, DOB, PHONE);
            assertEquals(ResultCode.DUPLICATE_USERNAME, result);
        }

        @Test
        void register_DuplicateEmailIgnoreCase_ReturnsDuplicateEmail() {
            service.register(USER, EMAIL, PASS, PASS, DOB, PHONE);
            ResultCode result = service.register("bob_02", "ALICE@EXAMPLE.COM", PASS, PASS, DOB, PHONE);
            assertEquals(ResultCode.DUPLICATE_EMAIL, result);
        }

        @ParameterizedTest(name = "[{index}] hôm nay - {0} năm + {1} ngày -> {2}")
        @CsvSource({
                "18, 0, SUCCESS",
                "18, 1, UNDERAGE",
                "0, 1, INVALID_INPUT"
        })
        void register_AgeBoundary(int yearsAgo, int plusDays, ResultCode expected) {
            LocalDate dob = LocalDate.now().minusYears(yearsAgo).plusDays(plusDays);
            assertEquals(expected, service.register(USER, EMAIL, PASS, PASS, dob, null));
        }
    }

    static Stream<Arguments> invalidRegisterInputs() {
        return Stream.of(
                Arguments.of("username sai định dạng", "1alice", EMAIL, PASS, PASS, DOB, PHONE, ResultCode.INVALID_USERNAME),
                Arguments.of("email sai định dạng", USER, "bad-email", PASS, PASS, DOB, PHONE, ResultCode.INVALID_EMAIL),
                Arguments.of("password yếu", USER, EMAIL, "weak", "weak", DOB, PHONE, ResultCode.WEAK_PASSWORD),
                Arguments.of("confirm không khớp", USER, EMAIL, PASS, "Other@123", DOB, PHONE, ResultCode.PASSWORD_MISMATCH),
                Arguments.of("chưa đủ tuổi", USER, EMAIL, PASS, PASS, LocalDate.now().minusYears(17), PHONE, ResultCode.UNDERAGE),
                Arguments.of("phone sai định dạng", USER, EMAIL, PASS, PASS, DOB, "123", ResultCode.INVALID_PHONE),

                Arguments.of("username sai + email sai -> báo username trước",
                        "1alice", "bad-email", PASS, PASS, DOB, PHONE, ResultCode.INVALID_USERNAME),
                Arguments.of("email sai + password yếu -> báo email trước",
                        USER, "bad-email", "weak", "weak", DOB, PHONE, ResultCode.INVALID_EMAIL),
                Arguments.of("password yếu + confirm lệch -> báo weak password trước",
                        USER, EMAIL, "weak", "khac", DOB, PHONE, ResultCode.WEAK_PASSWORD)
        );
    }

    @Nested
    class Login {

        @BeforeEach
        void registerDefaultAccount() {
            service.register(USER, EMAIL, PASS, PASS, DOB, PHONE);
        }

        @Test
        void login_UserNotExist_ReturnsInvalidCredentials() {
            assertEquals(ResultCode.INVALID_CREDENTIALS, service.login("nobody", PASS));
        }

        @Test
        void login_DisabledAccount_ReturnsAccountDisabled() {
            service.disableAccount(USER);
            assertEquals(ResultCode.ACCOUNT_DISABLED, service.login(USER, PASS));
        }

        @Test
        void login_UserNotExist_And_WrongPassword_ReturnSameCode() {
            ResultCode notExist = service.login("nobody", PASS);
            ResultCode wrongPass = service.login(USER, "WrongPass@123");
            assertEquals(notExist, wrongPass); // cả 2 đều INVALID_CREDENTIALS, không lộ thông tin username có tồn tại hay không
        }

        @Test
        void login_LockedAccount_CorrectPassword_StillReturnsAccountLocked() {
            lockAccountByFailedAttempts(); // helper bên dưới, sai đủ 5 lần để khóa
            ResultCode result = service.login(USER, PASS); // mật khẩu ĐÚNG
            assertEquals(ResultCode.ACCOUNT_LOCKED, result);
        }

        @Test
        void login_LockedAccount_AttemptsDoNotIncreaseFurtherWhenAlreadyLocked() {
            lockAccountByFailedAttempts();
            int attemptsAfterLock = service.findByUsername(USER).orElseThrow().getFailedAttempts();

            service.login(USER, "AnotherWrong@1"); // thử thêm 1 lần nữa khi đã khóa
            int attemptsAfterExtraTry = service.findByUsername(USER).orElseThrow().getFailedAttempts();

            assertEquals(attemptsAfterLock, attemptsAfterExtraTry); // bộ đếm KHÔNG đổi
        }

        private void lockAccountByFailedAttempts() {
            for (int i = 0; i < AccountService.MAX_FAILED_ATTEMPTS; i++) {
                service.login(USER, "WrongPass@" + i);
            }
        }

        @Test
        void login_FourFailedAttempts_StillInvalidCredentials_NotLocked() {
            for (int i = 0; i < 4; i++) {
                assertEquals(ResultCode.INVALID_CREDENTIALS, service.login(USER, "Wrong@" + i));
            }
            assertFalse(service.isLocked(USER));
            assertEquals(4, service.findByUsername(USER).orElseThrow().getFailedAttempts());
        }

        @Test
        void login_FifthFailedAttempt_LocksAccount() {
            for (int i = 0; i < 4; i++) {
                service.login(USER, "Wrong@" + i);
            }
            ResultCode fifthAttempt = service.login(USER, "Wrong@4"); // lần sai thứ 5

            assertEquals(ResultCode.ACCOUNT_LOCKED, fifthAttempt);
            assertTrue(service.isLocked(USER));
        }

        @Test
        void login_CorrectPassword_ReturnsSuccessAndResetsFailedAttempts() {
            service.login(USER, "Wrong@1");
            service.login(USER, "Wrong@2"); // sai 2 lần trước

            ResultCode result = service.login(USER, PASS); // rồi đăng nhập đúng

            assertEquals(ResultCode.SUCCESS, result);
            assertEquals(0, service.findByUsername(USER).orElseThrow().getFailedAttempts());
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {" "})
        void login_UsernameNullEmptyBlank_ReturnsInvalidInput(String username) {
            assertEquals(ResultCode.INVALID_INPUT, service.login(username, PASS));
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {" "})
        void login_PasswordNullEmptyBlank_ReturnsInvalidInput(String password) {
            assertEquals(ResultCode.INVALID_INPUT, service.login(USER, password));
        }

        @Test
        void login_UsernameDifferentCase_StillWorks() {
            assertEquals(ResultCode.SUCCESS, service.login("ALICE_01", PASS));
        }
    }
}