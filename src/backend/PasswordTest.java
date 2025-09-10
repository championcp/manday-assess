import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordTest {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String storedHash = "$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9b2UgdHOT4.Ea1u";
        String password = "admin123";
        
        boolean matches = encoder.matches(password, storedHash);
        System.out.println("密码匹配结果: " + matches);
        
        // 测试生成新的哈希
        String newHash = encoder.encode(password);
        System.out.println("新哈希: " + newHash);
        System.out.println("新哈希匹配: " + encoder.matches(password, newHash));
    }
}
