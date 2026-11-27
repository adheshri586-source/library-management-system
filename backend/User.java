package library.backend;

import java.io.Serializable;

public final class User implements Serializable {
    private static final long serialVersionUID = 1L;
    public final String id, name, email, password, role, department;
    public String memberCode = "", course = "", semester = "", designation = "";

    public User(String id, String name, String email, String password, String role, String department) {
        this.id = id; this.name = name; this.email = email; this.password = password;
        this.role = role; this.department = department;
    }
}
