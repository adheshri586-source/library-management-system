package library.database;

import library.backend.Book;
import library.backend.Loan;
import library.backend.User;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** File-backed persistence adapter. Data is stored in .library-data/library-data.ser. */
public final class LibraryDatabase implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final Path DATA_FILE = Path.of(".library-data", "library-data.ser");
    public final List<User> users = new ArrayList<>();
    public final List<Book> books = new ArrayList<>();
    public final List<Loan> loans = new ArrayList<>();
    private int userCounter = 6, bookCounter = 1, loanCounter = 1;

    public User user(String id) { return users.stream().filter(u -> u.id.equals(id)).findFirst().orElse(null); }
    public Book book(String id) { return books.stream().filter(b -> b.id.equals(id)).findFirst().orElse(null); }
    public String nextUserId() { return String.format("U%03d", userCounter++); }
    public String nextBookId() { return String.format("B%03d", bookCounter++); }
    public String nextLoanId() { return String.format("L%03d", loanCounter++); }

    public void save() {
        try {
            Files.createDirectories(DATA_FILE.getParent());
            try (ObjectOutputStream out = new ObjectOutputStream(Files.newOutputStream(DATA_FILE))) { out.writeObject(this); }
        } catch (IOException ex) { throw new UncheckedIOException("Could not save library database", ex); }
    }

    public static LibraryDatabase load() {
        if (Files.exists(DATA_FILE)) {
            try (ObjectInputStream in = new ObjectInputStream(Files.newInputStream(DATA_FILE))) {
                Object loaded = in.readObject();
                if (loaded instanceof LibraryDatabase database) return database;
            } catch (IOException | ClassNotFoundException ignored) { /* Re-seed a missing or unreadable local database. */ }
        }
        LibraryDatabase database = seed();
        database.save();
        return database;
    }

    private static LibraryDatabase seed() {
        LibraryDatabase db = new LibraryDatabase();
        db.users.add(new User("U000", "Library Admin", "admin@library.com", "admin123", "Admin", "Library"));
        User aarav = new User("U001", "Aarav Sharma", "aarav@college.com", "student123", "Student", "Computer Science"); aarav.memberCode="CS202501"; aarav.course="B.Tech CSE"; aarav.semester="3"; db.users.add(aarav);
        User meera = new User("U002", "Meera Verma", "meera@college.com", "student123", "Student", "Electronics"); meera.memberCode="EC202502"; meera.course="B.Tech ECE"; meera.semester="3"; db.users.add(meera);
        User kabir = new User("U003", "Kabir Singh", "kabir@college.com", "student123", "Student", "Computer Science"); kabir.memberCode="CS202503"; kabir.course="B.Tech CSE"; kabir.semester="3"; db.users.add(kabir);
        User neha = new User("U004", "Dr. Neha Kapoor", "neha@college.com", "faculty123", "Faculty", "Computer Science"); neha.memberCode="F101"; neha.designation="Associate Professor"; db.users.add(neha);
        User rahul = new User("U005", "Dr. Rahul Mehta", "rahul@college.com", "faculty123", "Faculty", "Mathematics"); rahul.memberCode="F102"; rahul.designation="Professor"; db.users.add(rahul);
        String[][] rows = {
            {"The Alchemist","Paulo Coelho","Fiction","5"},{"Atomic Habits","James Clear","Self Help","4"},
            {"Clean Code","Robert C. Martin","Programming","3"},{"Harry Potter and the Philosopher's Stone","J. K. Rowling","Fantasy","4"},
            {"Introduction to Algorithms","Cormen et al.","Computer Science","3"},{"Wings of Fire","A. P. J. Abdul Kalam","Biography","5"},
            {"Rich Dad Poor Dad","Robert Kiyosaki","Finance","4"},{"The Psychology of Money","Morgan Housel","Finance","3"},
            {"Pride and Prejudice","Jane Austen","Classic","3"},{"1984","George Orwell","Dystopian","3"},
            {"The Hobbit","J. R. R. Tolkien","Fantasy","3"},{"Python Crash Course","Eric Matthes","Programming","3"},
            {"Let Us C","Yashavant Kanetkar","Programming","4"},{"Data Structures and Algorithms","Narasimha Karumanchi","Computer Science","3"},
            {"Operating System Concepts","Silberschatz et al.","Computer Science","3"},{"Computer Networks","Andrew S. Tanenbaum","Computer Science","3"},
            {"The Great Gatsby","F. Scott Fitzgerald","Classic","3"},{"Sapiens","Yuval Noah Harari","History","3"},
            {"Ikigai","Hector Garcia","Self Help","3"},{"Think Like a Monk","Jay Shetty","Self Help","3"}
        };
        for (String[] row : rows) { int count=Integer.parseInt(row[3]); db.books.add(new Book(db.nextBookId(),row[0],row[1],row[2],count,count)); }
        LocalDate today=LocalDate.now();
        db.seedLoan("U001","B002",today.minusDays(5),today.plusDays(9),false,null);
        db.seedLoan("U002","B001",today.minusDays(3),today.plusDays(11),false,null);
        db.seedLoan("U003","B003",today.minusDays(13),today.minusDays(1),false,null);
        db.seedLoan("U004","B015",today.minusDays(10),today.plusDays(20),false,null);
        db.seedLoan("U001","B006",today.minusDays(32),today.minusDays(18),true,today.minusDays(17));
        db.seedLoan("U002","B009",today.minusDays(35),today.minusDays(21),true,today.minusDays(20));
        return db;
    }

    private void seedLoan(String userId,String bookId,LocalDate issued,LocalDate due,boolean returned,LocalDate returnedAt) {
        Loan loan=new Loan(nextLoanId(),userId,bookId,issued,due); loan.returned=returned; loan.returnDate=returnedAt; loans.add(loan);
        Book book=book(bookId); if(book!=null) book.available=Math.max(0,book.available-1);
    }
}
