package library.frontend;

import javafx.application.Application;
import library.backend.Book;
import library.backend.Loan;
import library.backend.User;
import library.backend.LibraryService;
import library.database.LibraryDatabase;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public class LibraryManagementSystem extends Application {
    private final LibraryService service = new LibraryService(LibraryDatabase.load());
    private Stage stage;
    private User currentUser;
    private BorderPane dashboard;
    private String currentPage = "Overview";
    private TableView<Book> bookTable;
    private TableView<Loan> loanTable;
    private TextField bookSearch;

    @Override
    public void start(Stage primaryStage) {
        stage = primaryStage;
        stage.setTitle("Library Desk");
        stage.setMinWidth(920);
        stage.setMinHeight(640);
        showLogin();
        stage.show();
    }

    private void showLogin() {
        VBox brand = new VBox(18);
        brand.getStyleClass().add("login-brand");
        Label mark = new Label("▤"); mark.getStyleClass().add("brand-mark");
        Label title = new Label("Library Desk"); title.getStyleClass().add("brand-title");
        Label tagline = new Label("A quieter place to find your next idea."); tagline.getStyleClass().add("brand-tagline");
        Label motif = new Label("BOOKS   •   LEARN   •   GROW"); motif.getStyleClass().add("brand-motif");
        Region spacer = new Region(); VBox.setVgrow(spacer, Priority.ALWAYS);
        brand.getChildren().addAll(mark, title, tagline, spacer, motif);

        VBox form = new VBox(10);
        form.setMaxWidth(380);
        form.getStyleClass().add("login-card");
        Label eyebrow = new Label("MEMBER ACCESS"); eyebrow.getStyleClass().add("eyebrow");
        Label welcome = new Label("Welcome back"); welcome.getStyleClass().add("page-title");
        Label sub = new Label("Sign in to continue to your library."); sub.getStyleClass().add("muted");
        TextField email = new TextField(); email.setPromptText("you@college.com");
        PasswordField password = new PasswordField(); password.setPromptText("Enter password");
        ComboBox<String> role = new ComboBox<>(FXCollections.observableArrayList("Student", "Faculty", "Admin"));
        role.getSelectionModel().select("Student"); role.setMaxWidth(Double.MAX_VALUE);
        Button login = primaryButton("Sign in"); login.setMaxWidth(Double.MAX_VALUE);
        Label demo = new Label("Admin: admin@library.com / admin123\nStudent: aarav@college.com / student123\nFaculty: neha@college.com / faculty123");
        demo.getStyleClass().add("demo-note");
        Button register = linkButton("New here? Create a member account");
        form.getChildren().addAll(eyebrow, welcome, sub, fieldLabel("Email address"), email,
                fieldLabel("Password"), password, fieldLabel("Sign in as"), role, login, register, demo);
        login.setOnAction(e -> {
            User found = service.authenticate(email.getText().trim(), password.getText(), role.getValue());
            if (found == null) alert("Login failed", "Email, password, or account type is incorrect.");
            else { currentUser = found; showDashboard(); }
        });
        register.setOnAction(e -> showRegistration());
        password.setOnAction(e -> login.fire());

        HBox root = new HBox(0, brand, new StackPane(form));
        root.getStyleClass().add("login-root");
        HBox.setHgrow(brand, Priority.ALWAYS);
        HBox.setHgrow(root.getChildren().get(1), Priority.ALWAYS);
        Scene scene = new Scene(root, 1040, 700);
        applyStyles(scene);
        stage.setScene(scene);
        stage.centerOnScreen();
    }

    private void showRegistration() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.initOwner(stage); dialog.initModality(Modality.WINDOW_MODAL); dialog.setTitle("Create member account");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.CANCEL, ButtonType.OK);
        TextField name = new TextField(), email = new TextField(), memberId = new TextField(), department = new TextField();
        TextField course = new TextField(), semester = new TextField(), designation = new TextField();
        PasswordField password = new PasswordField();
        ComboBox<String> role = new ComboBox<>(FXCollections.observableArrayList("Student", "Faculty")); role.getSelectionModel().selectFirst();
        GridPane grid = formGrid();
        addFormRow(grid, 0, "Full name", name); addFormRow(grid, 1, "Email", email); addFormRow(grid, 2, "Password", password);
        addFormRow(grid, 3, "Role", role); addFormRow(grid, 4, "Student roll / Faculty ID", memberId);
        addFormRow(grid, 5, "Department", department); addFormRow(grid, 6, "Course (students)", course);
        addFormRow(grid, 7, "Semester (students)", semester); addFormRow(grid, 8, "Designation (faculty)", designation);
        dialog.getDialogPane().setContent(grid);
        if (dialog.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) return;
        String cleanedEmail = email.getText().trim();
        if (name.getText().isBlank() || cleanedEmail.isBlank() || password.getText().isBlank() || memberId.getText().isBlank()) {
            alert("Missing details", "Please fill in name, email, ID, and password."); return;
        }
        if (!cleanedEmail.contains("@") || password.getText().length() < 6) {
            alert("Check your details", "Enter a valid email and a password with at least 6 characters."); return;
        }
        if (service.emailExists(cleanedEmail)) {
            alert("Email already registered", "Use a different email address."); return;
        }
        User user = service.register(name.getText().trim(), cleanedEmail, password.getText(), role.getValue(),
                memberId.getText().trim(), department.getText().trim(), course.getText().trim(),
                semester.getText().trim(), designation.getText().trim());
        if (user == null) { alert("Email already registered", "Use a different email address."); return; }
        alert("Registration complete", "Your account is ready. Sign in with your new credentials.");
    }

    private void showDashboard() {
        dashboard = new BorderPane(); dashboard.getStyleClass().add("app-root");
        dashboard.setLeft(sidebar()); dashboard.setTop(topbar()); renderPage("Overview");
        Scene scene = new Scene(dashboard, 1180, 760); applyStyles(scene); stage.setScene(scene); stage.centerOnScreen();
    }

    private VBox sidebar() {
        VBox side = new VBox(8); side.getStyleClass().add("sidebar"); side.setPrefWidth(230);
        VBox brandText = new VBox(new Label("Library Desk"), new Label("Campus collection"));
        HBox brand = new HBox(11, new Label("▤"), brandText); brand.getStyleClass().add("side-brand"); side.getChildren().add(brand);
        Region gap = new Region(); gap.setMinHeight(18); side.getChildren().add(gap);
        List<String> pages = new ArrayList<>(List.of("Overview", "Books", "My Loans", "History"));
        if (currentUser.role.equals("Admin")) pages.addAll(List.of("Members", "Transactions"));
        for (String page : pages) {
            Button button = new Button(iconFor(page) + "   " + page); button.getStyleClass().add("nav-button");
            button.setMaxWidth(Double.MAX_VALUE); if (page.equals(currentPage)) button.getStyleClass().add("selected");
            button.setOnAction(e -> renderPage(page)); side.getChildren().add(button);
        }
        Region push = new Region(); VBox.setVgrow(push, Priority.ALWAYS);
        Label note = new Label("A shared space for curious minds.\n\n" + service.books.size() + " titles in the collection");
        note.getStyleClass().add("side-note"); side.getChildren().addAll(push, note); return side;
    }

    private HBox topbar() {
        Label eyebrow = new Label("LIBRARY WORKSPACE"); eyebrow.getStyleClass().add("eyebrow");
        Label title = new Label(currentPage); title.getStyleClass().add("page-title");
        VBox titleBox = new VBox(2, eyebrow, title); Region spacer = new Region(); HBox.setHgrow(spacer, Priority.ALWAYS);
        Label name = new Label(currentUser.name + "\n" + currentUser.role + (currentUser.department.isBlank() ? "" : " · " + currentUser.department));
        name.getStyleClass().add("user-name"); Button signOut = secondaryButton("Sign out");
        signOut.setOnAction(e -> { currentUser = null; showLogin(); });
        HBox bar = new HBox(16, titleBox, spacer, name, signOut); bar.setAlignment(Pos.CENTER_LEFT); bar.getStyleClass().add("topbar"); return bar;
    }

    private void renderPage(String page) {
        currentPage = page; dashboard.setLeft(sidebar()); dashboard.setTop(topbar());
        Node content = switch (page) {
            case "Books" -> booksPage(); case "My Loans" -> myLoansPage(); case "History" -> historyPage(false);
            case "Members" -> membersPage(); case "Transactions" -> historyPage(true); default -> overviewPage();
        };
        ScrollPane scroll = new ScrollPane(content); scroll.setFitToWidth(true); scroll.getStyleClass().add("content-scroll"); dashboard.setCenter(scroll);
    }

    private Node overviewPage() {
        VBox body = contentBody();
        VBox welcome = new VBox(5, new Label("Good to see you, " + currentUser.name.split(" ")[0] + "."),
                new Label(currentUser.role.equals("Admin") ? "Keep the collection organized and your members supported." : "Find something worth getting lost in today."));
        welcome.getStyleClass().add("welcome-banner");
        int copies = service.books.stream().mapToInt(b -> b.total).sum();
        int available = service.books.stream().mapToInt(b -> b.available).sum();
        int active = (int) service.loans.stream().filter(l -> !l.returned).count();
        HBox stats = new HBox(14, statCard("Book titles", service.books.size()), statCard("Total copies", copies),
                statCard("Available now", available), statCard(currentUser.role.equals("Admin") ? "Active loans" : "My active loans", currentUser.role.equals("Admin") ? active : activeLoans(currentUser)));
        for (Node node : stats.getChildren()) HBox.setHgrow(node, Priority.ALWAYS);
        VBox snapshot = card(); snapshot.getChildren().addAll(sectionTitle(currentUser.role.equals("Admin") ? "Collection snapshot" : "Your current loans"),
                new Label(currentUser.role.equals("Admin") ? "Recently added titles from the collection." : "Books currently checked out to your account."));
        if (currentUser.role.equals("Admin")) snapshot.getChildren().add(bookTableView(service.books.subList(Math.max(0, service.books.size() - 5), service.books.size())));
        else snapshot.getChildren().add(loanTableView(activeLoansFor(currentUser)));
        body.getChildren().addAll(welcome, stats, snapshot); return body;
    }

    private Node booksPage() {
        VBox panel = card(); HBox header = new HBox(12);
        VBox heading = new VBox(4, sectionTitle("Browse collection"), new Label("Search by title, author, category, or book ID."));
        Region spacer = new Region(); HBox.setHgrow(spacer, Priority.ALWAYS);
        bookSearch = new TextField(); bookSearch.setPromptText("Search the catalog"); bookSearch.setPrefWidth(240);
        Button clear = secondaryButton("Clear"); clear.setOnAction(e -> { bookSearch.clear(); refreshBookTable(); });
        header.getChildren().addAll(heading, spacer, bookSearch, clear);
        bookTable = bookTableView(filteredBooks());
        bookSearch.textProperty().addListener((obs, old, value) -> refreshBookTable());
        HBox actions = new HBox(9); Button borrow = primaryButton("Borrow selected"), giveBack = secondaryButton("Return selected");
        actions.getChildren().addAll(borrow, giveBack);
        borrow.setOnAction(e -> issueBook(bookTable.getSelectionModel().getSelectedItem()));
        giveBack.setOnAction(e -> returnSelectedBook(bookTable.getSelectionModel().getSelectedItem()));
        if (currentUser.role.equals("Admin")) {
            Button add = primaryButton("＋ Add book"), delete = dangerButton("Delete selected");
            add.setOnAction(e -> addBook()); delete.setOnAction(e -> deleteBook(bookTable.getSelectionModel().getSelectedItem())); actions.getChildren().addAll(add, delete);
        }
        panel.getChildren().addAll(header, bookTable, actions); return pageWith(panel);
    }

    private TableView<Book> bookTableView(List<Book> books) {
        TableView<Book> table = new TableView<>(); table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        TableColumn<Book, String> title = new TableColumn<>("Book"); title.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().title + "  ·  " + c.getValue().author));
        TableColumn<Book, String> category = new TableColumn<>("Category"); category.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().category));
        TableColumn<Book, Integer> total = new TableColumn<>("Copies"); total.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().total));
        TableColumn<Book, Integer> available = new TableColumn<>("Available"); available.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().available));
        TableColumn<Book, String> id = new TableColumn<>("ID"); id.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().id));
        table.getColumns().addAll(title, category, total, available, id); table.setItems(FXCollections.observableArrayList(books));
        table.setPlaceholder(new Label("No books match your search.")); table.setPrefHeight(400); return table;
    }

    private List<Book> filteredBooks() {
        String query = bookSearch == null ? "" : bookSearch.getText().trim().toLowerCase(Locale.ROOT);
        return service.books.stream().filter(b -> query.isBlank() || (b.id + " " + b.title + " " + b.author + " " + b.category).toLowerCase(Locale.ROOT).contains(query)).toList();
    }

    private void refreshBookTable() { if (bookTable != null) bookTable.setItems(FXCollections.observableArrayList(filteredBooks())); }

    private Node myLoansPage() {
        VBox panel = card(); panel.getChildren().addAll(sectionTitle("Books on your account"),
                new Label("Students: 3 books for 14 days · Faculty: 5 books for 30 days · Student late fine: ₹2/day"));
        loanTable = loanTableView(activeLoansFor(currentUser)); Button returnButton = secondaryButton("Return selected loan");
        returnButton.setOnAction(e -> returnLoan(loanTable.getSelectionModel().getSelectedItem())); panel.getChildren().addAll(loanTable, returnButton);
        return pageWith(panel);
    }

    private TableView<Loan> loanTableView(List<Loan> loans) {
        TableView<Loan> table = new TableView<>(); table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        TableColumn<Loan, String> member = new TableColumn<>("Member"); member.setCellValueFactory(c -> new ReadOnlyStringWrapper(userName(c.getValue().userId)));
        TableColumn<Loan, String> book = new TableColumn<>("Book"); book.setCellValueFactory(c -> new ReadOnlyStringWrapper(bookTitle(c.getValue().bookId)));
        TableColumn<Loan, String> issued = new TableColumn<>("Issued"); issued.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().issueDate.toString()));
        TableColumn<Loan, String> due = new TableColumn<>("Due date"); due.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().dueDate.toString()));
        TableColumn<Loan, String> status = new TableColumn<>("Status"); status.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().returned ? "Returned" : "Issued"));
        TableColumn<Loan, Integer> fine = new TableColumn<>("Fine (₹)"); fine.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(service.fine(c.getValue())));
        table.getColumns().addAll(member, book, issued, due, status, fine); table.setItems(FXCollections.observableArrayList(loans));
        table.setPlaceholder(new Label("No loans to show.")); table.setPrefHeight(360); return table;
    }

    private Node historyPage(boolean allMembers) {
        VBox panel = card(); panel.getChildren().addAll(sectionTitle(allMembers ? "All transactions" : "Borrowing history"),
                new Label(allMembers ? "Circulation activity across the library." : "Your past and active loans, with calculated late fees."));
        List<Loan> rows = allMembers ? service.loans : service.loans.stream().filter(l -> l.userId.equals(currentUser.id)).toList();
        panel.getChildren().add(loanTableView(rows)); return pageWith(panel);
    }

    private Node membersPage() {
        VBox panel = card(); panel.getChildren().addAll(sectionTitle("Library members"), new Label(service.users.size() + " registered accounts"));
        TableView<User> table = new TableView<>(FXCollections.observableArrayList(service.users)); table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        TableColumn<User, String> name = new TableColumn<>("Name"); name.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().name));
        TableColumn<User, String> email = new TableColumn<>("Email"); email.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().email));
        TableColumn<User, String> role = new TableColumn<>("Role"); role.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().role));
        TableColumn<User, String> department = new TableColumn<>("Department"); department.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().department));
        TableColumn<User, Integer> active = new TableColumn<>("Active loans"); active.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(activeLoans(c.getValue())));
        table.getColumns().addAll(name, email, role, department, active); panel.getChildren().add(table); return pageWith(panel);
    }

    private void addBook() {
        Dialog<ButtonType> dialog = new Dialog<>(); dialog.initOwner(stage); dialog.setTitle("Add a book");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.CANCEL, ButtonType.OK);
        TextField title = new TextField(), author = new TextField(), category = new TextField(); Spinner<Integer> copies = new Spinner<>(1, 999, 1);
        GridPane grid = formGrid(); addFormRow(grid, 0, "Book title", title); addFormRow(grid, 1, "Author", author);
        addFormRow(grid, 2, "Category", category); addFormRow(grid, 3, "Copies", copies); dialog.getDialogPane().setContent(grid);
        if (dialog.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) return;
        if (title.getText().isBlank() || author.getText().isBlank() || category.getText().isBlank()) { alert("Missing details", "Enter a title, author, and category."); return; }
        int count = copies.getValue(); service.addBook(title.getText().trim(), author.getText().trim(), category.getText().trim(), count);
        renderPage("Books");
    }

    private void deleteBook(Book book) {
        if (book == null) { alert("Select a book", "Choose a row before deleting a title."); return; }
        if (service.hasActiveLoans(book)) { alert("Book is currently borrowed", "Return all active copies before deleting this title."); return; }
        if (confirm("Delete book", "Delete ‘" + book.title + "’ from the collection?")) { service.deleteBook(book); renderPage("Books"); }
    }

    private void issueBook(Book book) {
        if (book == null) { alert("Select a book", "Choose a book from the catalog first."); return; }
        if (book.available < 1) { alert("No copies available", "This title is currently checked out."); return; }
        int limit = currentUser.role.equals("Faculty") ? 5 : 3;
        if (service.activeLoans(currentUser) >= limit) { alert("Borrowing limit reached", "Your limit is " + limit + " active books."); return; }
        if (service.activeLoan(currentUser, book) != null) { alert("Already borrowed", "You already have an active loan for this title."); return; }
        Loan loan = service.borrow(currentUser, book);
        alert("Book borrowed", "Due date: " + loan.dueDate); renderPage("Books");
    }

    private void returnSelectedBook(Book book) {
        if (book == null) { alert("Select a book", "Choose a title from the catalog first."); return; }
        returnLoan(service.activeLoan(currentUser, book));
    }

    private void returnLoan(Loan loan) {
        if (loan == null) { alert("No active loan", "There is no active loan selected for your account."); return; }
        int fine = service.returnLoan(loan);
        alert("Book returned", "Fine: ₹" + fine); renderPage(currentPage);
    }

    private int activeLoans(User user) { return (int) service.loans.stream().filter(l -> l.userId.equals(user.id) && !l.returned).count(); }
    private List<Loan> activeLoansFor(User user) { return service.loans.stream().filter(l -> l.userId.equals(user.id) && !l.returned).toList(); }
    private String bookTitle(String id) { Book book = service.book(id); return book == null ? id : book.title; }
    private String userName(String id) { User user = service.user(id); return user == null ? id : user.name; }
    private VBox contentBody() { VBox body = new VBox(18); body.getStyleClass().add("content-body"); return body; }
    private VBox pageWith(Node content) { VBox body = contentBody(); body.getChildren().add(content); return body; }
    private VBox card() { VBox card = new VBox(14); card.getStyleClass().add("card"); return card; }

    private VBox statCard(String label, int value) {
        Label name = new Label(label); name.getStyleClass().add("muted");
        Label count = new Label(Integer.toString(value)); count.getStyleClass().add("stat-value");
        VBox card = new VBox(6, name, count); card.getStyleClass().add("stat-card"); return card;
    }
    private Label sectionTitle(String text) { Label label = new Label(text); label.getStyleClass().add("section-title"); return label; }
    private Label fieldLabel(String text) { Label label = new Label(text); label.getStyleClass().add("field-label"); return label; }
    private Button primaryButton(String text) { Button b = new Button(text); b.getStyleClass().add("primary-button"); return b; }
    private Button secondaryButton(String text) { Button b = new Button(text); b.getStyleClass().add("secondary-button"); return b; }
    private Button dangerButton(String text) { Button b = new Button(text); b.getStyleClass().add("danger-button"); return b; }
    private Button linkButton(String text) { Button b = new Button(text); b.getStyleClass().add("link-button"); return b; }

    private GridPane formGrid() {
        GridPane grid = new GridPane(); grid.setHgap(12); grid.setVgap(10); grid.setPadding(new Insets(10));
        ColumnConstraints first = new ColumnConstraints(); first.setMinWidth(145);
        ColumnConstraints second = new ColumnConstraints(); second.setHgrow(Priority.ALWAYS);
        grid.getColumnConstraints().addAll(first, second); return grid;
    }
    private void addFormRow(GridPane grid, int row, String label, Control control) {
        grid.add(fieldLabel(label), 0, row); control.setMaxWidth(Double.MAX_VALUE); grid.add(control, 1, row);
    }
    private String iconFor(String page) {
        return switch (page) { case "Overview" -> "◫"; case "Books" -> "▤"; case "My Loans" -> "↗"; case "History" -> "◷"; case "Members" -> "♙"; default -> "⇄"; };
    }
    private void applyStyles(Scene scene) {
        scene.getRoot().setStyle("-fx-background-color: #f4f6f5; -fx-font-family: 'Segoe UI'; -fx-font-size: 14px;");
        styleNode(scene.getRoot());
    }
    private void styleNode(Node node) {
        var classes = node.getStyleClass();
        if (node instanceof Region region) {
            if (classes.contains("sidebar") || classes.contains("login-brand")) region.setStyle("-fx-background-color: #173c35; -fx-padding: 24;");
            else if (classes.contains("topbar") || classes.contains("card") || classes.contains("stat-card") || classes.contains("login-card")) region.setStyle("-fx-background-color: white; -fx-padding: 20; -fx-background-radius: 12; -fx-border-color: #e4e9e6; -fx-border-radius: 12;");
            else if (classes.contains("content-body")) region.setStyle("-fx-padding: 24;");
            else if (classes.contains("welcome-banner")) region.setStyle("-fx-background-color: #e2eee9; -fx-padding: 22; -fx-background-radius: 12;");
        }
        if (node instanceof Label label) {
            if (classes.contains("page-title") || classes.contains("section-title") || classes.contains("stat-value")) label.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #173c35;");
            else if (classes.contains("muted") || classes.contains("field-label") || classes.contains("eyebrow")) label.setStyle("-fx-text-fill: #64746e;");
        }
        if (node instanceof Button button) {
            if (classes.contains("primary-button")) button.setStyle("-fx-background-color: #1f6b59; -fx-text-fill: white; -fx-background-radius: 7; -fx-padding: 10 16;");
            else if (classes.contains("danger-button")) button.setStyle("-fx-background-color: #a83c3c; -fx-text-fill: white; -fx-background-radius: 7; -fx-padding: 10 16;");
            else if (classes.contains("secondary-button")) button.setStyle("-fx-background-color: #e8eeeb; -fx-text-fill: #23483e; -fx-background-radius: 7; -fx-padding: 10 16;");
            else if (classes.contains("nav-button")) button.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-alignment: CENTER_LEFT; -fx-padding: 10 12;");
        }
        if (node instanceof Pane pane) for (Node child : pane.getChildren()) styleNode(child);
        else if (node instanceof ScrollPane scroll && scroll.getContent() != null) styleNode(scroll.getContent());
    }
    private void alert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, message, ButtonType.OK); alert.initOwner(stage);
        alert.setTitle(title); alert.setHeaderText(null); alert.showAndWait();
    }
    private boolean confirm(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, message, ButtonType.YES, ButtonType.NO); alert.initOwner(stage);
        alert.setTitle(title); alert.setHeaderText(null); return alert.showAndWait().orElse(ButtonType.NO) == ButtonType.YES;
    }

    public static void main(String[] args) { launch(args); }

}
