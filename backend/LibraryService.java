package library.backend;

import library.database.LibraryDatabase;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

/** Application rules shared by the JavaFX screens and the persistence layer. */
public final class LibraryService {
    private final LibraryDatabase database;
    public final List<Book> books;
    public final List<User> users;
    public final List<Loan> loans;

    public LibraryService(LibraryDatabase database) {
        this.database = database;
        this.books = database.books;
        this.users = database.users;
        this.loans = database.loans;
    }
    public List<Book> books() { return List.copyOf(books); }
    public List<User> members() { return List.copyOf(users); }
    public List<Loan> loans() { return List.copyOf(loans); }
    public User authenticate(String email,String password,String role) {
        if (role==null) return null;
        return users.stream().filter(u->u.email.equalsIgnoreCase(email)&&u.password.equals(password)&&u.role.equals(role)).findFirst().orElse(null);
    }
    public boolean emailExists(String email) { return users.stream().anyMatch(u->u.email.equalsIgnoreCase(email)); }
    public User register(String name,String email,String password,String role,String memberCode,String department,String course,String semester,String designation) {
        if(emailExists(email)) return null;
        User user=new User(database.nextUserId(),name,email,password,role,department);
        user.memberCode=memberCode; user.course=course; user.semester=semester; user.designation=designation;
        database.users.add(user); database.save(); return user;
    }
    public Book book(String id) { return database.book(id); }
    public User user(String id) { return database.user(id); }
    public Book addBook(String title,String author,String category,int copies) {
        Book book=new Book(database.nextBookId(),title,author,category,copies,copies); database.books.add(book); database.save(); return book;
    }
    public boolean hasActiveLoans(Book book) { return database.loans.stream().anyMatch(l->l.bookId.equals(book.id)&&!l.returned); }
    public void deleteBook(Book book) { database.books.remove(book); database.save(); }
    public int activeLoans(User user) { return (int)database.loans.stream().filter(l->l.userId.equals(user.id)&&!l.returned).count(); }
    public List<Loan> activeLoansFor(User user) { return database.loans.stream().filter(l->l.userId.equals(user.id)&&!l.returned).toList(); }
    public Loan activeLoan(User user,Book book) { return database.loans.stream().filter(l->l.userId.equals(user.id)&&l.bookId.equals(book.id)&&!l.returned).findFirst().orElse(null); }
    public int fine(Loan loan) {
        User user=user(loan.userId); if(user!=null&&user.role.equals("Faculty")) return 0;
        LocalDate end=loan.returned&&loan.returnDate!=null?loan.returnDate:LocalDate.now();
        return (int)Math.max(0,ChronoUnit.DAYS.between(loan.dueDate,end))*2;
    }
    public Loan borrow(User user,Book book) {
        if(book==null||user==null||book.available<1) return null;
        int limit=user.role.equals("Faculty")?5:3; if(activeLoans(user)>=limit||activeLoan(user,book)!=null) return null;
        LocalDate today=LocalDate.now(); int days=user.role.equals("Faculty")?30:14;
        Loan loan=new Loan(database.nextLoanId(),user.id,book.id,today,today.plusDays(days));
        book.available--; database.loans.add(loan); database.save(); return loan;
    }
    public int returnLoan(Loan loan) {
        if(loan==null||loan.returned) return 0;
        int amount=fine(loan); loan.returned=true; loan.returnDate=LocalDate.now();
        Book book=database.book(loan.bookId); if(book!=null) book.available=Math.min(book.total,book.available+1);
        database.save(); return amount;
    }
    public String bookTitle(String id) { Book book=database.book(id); return book==null?id:book.title; }
    public String userName(String id) { User user=database.user(id); return user==null?id:user.name; }
    public void save() { database.save(); }
    public int nextUserIdValue() { return Integer.parseInt(database.nextUserId().substring(1)); }
    public String nextUserId() { return database.nextUserId(); }
    public String nextBookId() { return database.nextBookId(); }
    public String nextLoanId() { return database.nextLoanId(); }
    public int fine(Loan loan, Object ignoredLegacyArgument) { return fine(loan); }
    public int activeLoansCount(User user) { return activeLoans(user); }
    public String bookTitleById(String id) { return bookTitle(id); }
    public String userNameById(String id) { return userName(id); }
}
