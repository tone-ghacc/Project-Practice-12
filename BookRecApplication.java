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
            switch (searchMethod) {
                case 1:
                    System.out.println("Enter search word");
                    String searchWord = scanner.nextLine();
                    SearchTitle searchTitle = new SearchTitle(conn);
                    searchTitle.searchMethod(searchWord, userID);
                    break;
                case 2:
                    // キーワード検索の実装
                    break;
                case 3:
                    // タグ検索の実装
                    break;
                default:
                    System.out.println("Invalid search method.");
            }

            // 詳細を見たい本のpublicationIDの入力
            System.out.print("Enter the publicationID of the book you want to see details: ");
            int publicationID = scanner.nextInt();

            // readingの情報の確認と表示
            // SQLクエリの実行
            pstmt = conn.prepareStatement("SELECT have, impression, date FROM reading WHERE publicationID = ?");
            pstmt.setInt(1, publicationID);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                // 登録されている場合
                boolean have = rs.getBoolean("have");
                String impression = rs.getString("impression");
                Date date = rs.getDate("date");
                System.out.println("Have: " + have);
                System.out.println("Impression: " + impression);
                System.out.println("Date: " + date);
            } else {
                // 登録されていない場合
                System.out.println("No information available.");
            }

            // システムの終了
            System.out.println("Exiting the system...");

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
