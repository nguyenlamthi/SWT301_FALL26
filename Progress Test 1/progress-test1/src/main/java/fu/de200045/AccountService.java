package fu.de200045;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public class AccountService {
    public static final int MAX_FAILED_ATTEMPTS = 5;
    public static final int PASSWORD_HISTORY_SIZE = 3;
    public static final int MIN_AGE = 18;

    private final Map<String, Account> accountsByUsername = new HashMap<>(); // key: username lowercase
    private final Map<String, String> usernameByEmail = new HashMap<>();     // key: email lowercase -> username key

    public AccountService() {
        // 2 map đã khởi tạo ngay lúc khai báo field ở trên, constructor không cần làm gì thêm
    }

    public ResultCode register(String username, String email, String password,
                               String confirmPassword, LocalDate dateOfBirth, String phone) {
        LocalDate today = LocalDate.now();

        // REG-01: input rỗng/null hoặc ngày sinh ở tương lai
        if (isBlank(username) || isBlank(email) || isBlank(password) || isBlank(confirmPassword)
                || dateOfBirth == null || dateOfBirth.isAfter(today)) {
            return ResultCode.INVALID_INPUT;
        }
        // REG-02
        if (!AccountValidator.isValidUsername(username)) {
            return ResultCode.INVALID_USERNAME;
        }
        // REG-04
        if (!AccountValidator.isValidEmail(email)) {
            return ResultCode.INVALID_EMAIL;
        }
        // REG-06
        if (!AccountValidator.isValidPassword(password, username)) {
            return ResultCode.WEAK_PASSWORD;
        }
        // REG-07
        if (!password.equals(confirmPassword)) {
            return ResultCode.PASSWORD_MISMATCH;
        }
        // REG-08 — logic tính tuổi nằm ở AccountValidator (hàm thuần), ở đây chỉ gọi lại
        if (AccountValidator.calculateAge(dateOfBirth, today) < MIN_AGE) {
            return ResultCode.UNDERAGE;
        }
        // REG-09 — phone là TÙY CHỌN: null/"" được chấp nhận, nhưng nếu có nhập thì phải đúng định dạng
        if (phone != null && !phone.isEmpty() && !AccountValidator.isValidPhone(phone)) {
            return ResultCode.INVALID_PHONE;
        }
        // REG-03 — so khớp không phân biệt hoa/thường bằng key lowercase
        String usernameKey = key(username);
        if (accountsByUsername.containsKey(usernameKey)) {
            return ResultCode.DUPLICATE_USERNAME;
        }
        // REG-05
        String emailKey = key(email);
        if (usernameByEmail.containsKey(emailKey)) {
            return ResultCode.DUPLICATE_EMAIL;
        }
        // REG-10 — tạo tài khoản thành công
        String salt = PasswordHasher.generateSalt();
        String passwordHash = PasswordHasher.hash(salt, password);
        Account account = new Account(username, emailKey, dateOfBirth, phone, salt, passwordHash);
        accountsByUsername.put(usernameKey, account);
        usernameByEmail.put(emailKey, usernameKey);
        return ResultCode.SUCCESS;
    }

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }

    private static String key(String s) {
        return s.toLowerCase(Locale.ROOT);
    }
}
