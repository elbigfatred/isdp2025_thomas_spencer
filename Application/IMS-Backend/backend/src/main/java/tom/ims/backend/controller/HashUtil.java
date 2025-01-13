package tom.ims.backend.controller;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * Utility class for hashing passwords.
 *
 * This class provides a single utility method for securely hashing passwords
 * using the BCrypt hashing algorithm. The hashed password can be stored in
 * the database for secure authentication purposes.
 */
public class HashUtil {
    // A shared instance of BCryptPasswordEncoder for hashing operations
    private static final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    /**
     * Hashes a plaintext password using the BCrypt algorithm.
     *
     * @param password the plaintext password to be hashed
     * @return the hashed password as a String
     */
    public static String hashPassword(String password) {
        return encoder.encode(password);
    }
}
