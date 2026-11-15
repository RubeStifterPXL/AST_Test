package be.pxl.qacademy.config;

/**
 * De vaste testgebruikers uit de opdracht. De username kan overschreven worden met een
 * omgevingsvariabele (bv. USER_STUDENT_1), het wachtwoord komt altijd uit QA_PASSWORD.
 */
public enum TestUser {
    ADMIN("USER_ADMIN", "admin.test", Role.ADMIN),
    INSTRUCTOR("USER_INSTRUCTOR", "instructor.test", Role.INSTRUCTOR),
    STUDENT_1("USER_STUDENT_1", "student.fresh", Role.STUDENT),
    STUDENT_2("USER_STUDENT_2", "student.second", Role.STUDENT),
    STUDENT_3("USER_STUDENT_3", "student.third", Role.STUDENT);

    public enum Role { ADMIN, INSTRUCTOR, STUDENT }

    private final String envKey;
    private final String defaultUsername;
    private final Role role;

    TestUser(String envKey, String defaultUsername, Role role) {
        this.envKey = envKey;
        this.defaultUsername = defaultUsername;
        this.role = role;
    }

    public String username() {
        return Config.get(envKey, defaultUsername);
    }

    public String password() {
        return Config.password();
    }

    public Role role() {
        return role;
    }
}
