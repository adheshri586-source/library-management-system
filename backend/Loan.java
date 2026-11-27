package library.backend;

import java.io.Serializable;
import java.time.LocalDate;

public final class Loan implements Serializable {
    private static final long serialVersionUID = 1L;
    public final String id, userId, bookId;
    public final LocalDate issueDate, dueDate;
    public LocalDate returnDate;
    public boolean returned;

    public Loan(String id, String userId, String bookId, LocalDate issueDate, LocalDate dueDate) {
        this.id = id; this.userId = userId; this.bookId = bookId;
        this.issueDate = issueDate; this.dueDate = dueDate;
    }
}
