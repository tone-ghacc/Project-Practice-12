import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SearchTitle extends Search {
    @Override
    protected void search(String title, int userid) {
        try {
            // 接続するのはメインでやっちゃって良さそう
            Connection conn = DriverManager.getConnection(
                    "jdbc:mysql://localhost/bookrec?useSSL=false&characterEncoding=utf8&useServerPrepStmts=true",
                    "root", "");

            // SQL文のところ
            PreparedStatement st = conn.prepareStatement(
                    "SELECT publicationID,series,author.name,publisher.name,have FROM story,writing,author,publishing,publication,publisher,reading WHERE series LIKE %?% AND userID=? AND story.publicationID=reading.publicationID AND story.publicationID=writing.publicationID AND writing.authorID=author.ID AND story.publicationID=publication.publicationID AND publication.ISBNnumber=publishing.ISBNnumber AND publishing.publisherID=publisher.ID");

            st.setString(1, title); // ここでSQLの ? の場所に値を埋め込んでいる
            st.setInt(2, userid); // 二番目の?に入れてる．なんで0始まりじゃないの(怒)

            // SQLを実行して、実行結果をResultSetに入れる
            ResultSet rs = st.executeQuery();

            // 結果を一行ずつ見て、必要な処理(ここでは表示)をする
            while (rs.next()) {
                int id = rs.getInt("publicationID");
                String series = rs.getString("series");
                String author = rs.getString("author.name");
                String publisher = rs.getString("publisher.name");
                boolean have = rs.getBoolean("have");
                System.out.println(id + "\t" + series + "\t" + author + "\t" + publisher + "\t" + have);
            }

            // 終了処理
            rs.close();
            st.close();

            // メインで接続やってるならこれはクローズしなくて良さそう
            conn.close();
        } catch (SQLException se) {
            System.out.println("SQL Error: " + se.toString() + " "
                    + se.getErrorCode() + " " + se.getSQLState());
        } catch (Exception e) {
            System.out.println("Error: " + e.toString() + e.getMessage());
        }
    }
}