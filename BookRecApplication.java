import java.sql.*;
import java.util.Scanner;

public class BookRecApplication {
    // JDBCドライバの登録
    static final String JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";
    static final String DB_URL = "jdbc:mysql://localhost/bookrec?useSSL=false";

    // データベースのユーザー名とパスワード
    static final String USER = "root";
    static final String PASS = "";

    public static void main(String[] args) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        Scanner scanner = new Scanner(System.in);

        try {
            // JDBCドライバの登録
            Class.forName(JDBC_DRIVER);

            // コネクションの確立
            System.out.println("Connecting to database...");
            conn = DriverManager.getConnection(DB_URL, USER, PASS);

            // アプリケーションの起動
            System.out.println("+++ Welcome to BookRec Application! +++");

            // userIDの入力
            System.out.print("Enter your userID (4 digits): ");
            int userID = scanner.nextInt();
            scanner.nextLine(); // 改行回避

            // クエリにuserIDをセット
            pstmt = conn.prepareStatement("SELECT EXISTS(SELECT 1 FROM user WHERE userID = ?)");
            pstmt.setInt(1, userID);
            rs = pstmt.executeQuery();

            // 結果の取得と表示
            if (rs.next()) {
                if (rs.getBoolean(1)) {
                    System.out.println("userID " + userID + " exists.");
                } else {
                    System.out.println("userID " + userID + " does not exist. Register? (y/n):");
                    if ("y".equalsIgnoreCase(scanner.nextLine())) {
                        System.out.println("Enter the your name");
                        String un = scanner.nextLine();
                        if (registerUser(conn, userID, un)) { // 登録処理
                            System.out.println("New userID " + userID + " is registered.");
                        } else {
                            System.out.println("Registration failed.");
                        }
                    } else {
                        System.out.println("Let's make an ID!");
                        return;
                    }
                }
            } else {
                return;
            }

            // 管理者かどうかの確認
            boolean isAdmin = (userID / 1000 == 9);
            if (isAdmin) {
                // 管理者ページの実装
                System.out.println("管理者ページです");
                return;
            }

            // 本データの検索方法の選択
            System.out.println("-------------------------");
            System.out.println("Choose the search method:");
            System.out.println("1. Title search");
            System.out.println("2. Keyword search (includes publisher and author names)");
            System.out.println("3. Tag search");
            System.out.println("-------------------------");
            int searchMethod = scanner.nextInt();
            scanner.nextLine();

            // 検索結果の取得と表示
            rs = null;
            String searchWord;
            switch (searchMethod) {
                case 1:
                    System.out.println("Enter search word");
                    searchWord = scanner.nextLine();
                    SearchTitle searchTitle = new SearchTitle(conn);
                    searchTitle.searchMethod(searchWord, userID);
                    break;
                case 2:
                    System.out.println("Enter search word");
                    searchWord = scanner.nextLine();
                    SearchKey searchKey = new SearchKey(conn);
                    searchKey.searchMethod(searchWord, userID);
                    break;
                case 3:
                    System.out.println("Enter search word");
                    searchWord = scanner.nextLine();
                    SearchTag searchTag = new SearchTag(conn);
                    searchTag.searchMethod(searchWord, userID);
                    break;
                default:
                    System.out.println("Invalid search method.");
            }

            // 詳細を見たい本のpublicationIDの入力
            System.out.print("Enter the publicationID of the book you want to see details: ");
            int publicationID = scanner.nextInt();

            // readingの情報の確認と表示
            // SQLクエリの実行
            pstmt = conn.prepareStatement(
                    "SELECT have, impression, date FROM reading WHERE publicationID = ? AND userID = ?");
            pstmt.setInt(1, publicationID);
            pstmt.setInt(2, userID);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                // 登録されている場合
                boolean have = rs.getBoolean("have");
                String impression = rs.getString("impression");
                Date date = rs.getDate("date");
                System.out.println("Have: " + have);
                System.out.println("Impression: " + impression);
                System.out.println("Date: " + date);

                // 情報を上書きするかたずねる
                System.out.print("Do you want to update the information? (yes/no): ");
                String answer = scanner.next();
                if ("yes".equalsIgnoreCase(answer)) {
                    // 感想の更新
                    System.out.print("Enter your new impression: ");
                    scanner.nextLine(); // 改行文字を消費
                    String newImpression = scanner.nextLine();
                    System.out.print("Do you have the book? (true/false): ");
                    boolean newHave = scanner.nextBoolean();

                    // 更新クエリの実行
                    pstmt = conn.prepareStatement(
                            "UPDATE reading SET impression = ?, have = ?, date = CURRENT_DATE WHERE publicationID = ? AND userID = ?");
                    pstmt.setString(1, newImpression);
                    pstmt.setBoolean(2, newHave);
                    pstmt.setInt(3, publicationID);
                    pstmt.setInt(4, userID);
                    pstmt.executeUpdate();
                    System.out.println("Information updated successfully.");
                }
            } else {
                // 登録されていない場合
                System.out.println("No information available.");
                // 登録を促す
                System.out.print("Do you want to register your impression? (yes/no): ");
                String answer = scanner.next();
                if ("yes".equalsIgnoreCase(answer)) {
                    // 感想の登録
                    System.out.print("Enter your impression: ");
                    scanner.nextLine(); // 改行文字を消費
                    String impression = scanner.nextLine();
                    System.out.print("Do you have the book? (true/false): ");
                    boolean have = scanner.nextBoolean();

                    // 登録クエリの実行
                    pstmt = conn.prepareStatement(
                            "INSERT INTO reading (userID, publicationID, have, impression, date) VALUES (?, ?, ?, ?, CURRENT_DATE)");
                    pstmt.setInt(1, userID);
                    pstmt.setInt(2, publicationID);
                    pstmt.setBoolean(3, have);
                    pstmt.setString(4, impression);
                    pstmt.executeUpdate();
                    System.out.println("Information registered successfully.");
                }
            }

        } catch (SQLException se) {
            // JDBCのエラー処理
            se.printStackTrace();
        } catch (Exception e) {
            // Class.forNameのエラー処理
            e.printStackTrace();
        } finally {
            // リソースの解放
            try {
                if (conn != null)
                    conn.close();
                scanner.close();
            } catch (SQLException se) {
                se.printStackTrace();
            }
        }
    }

    private static boolean registerUser(Connection conn, int userID, String un) throws SQLException {
        // 新しいユーザーの登録クエリ
        String sql = "INSERT INTO user (userID, name) VALUES (?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userID);
            pstmt.setString(2, un);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        }
    }
}
