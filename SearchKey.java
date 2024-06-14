import java.sql.*;

public class SearchKey extends Search {

    private Connection conn = null;

    public SearchKey(Connection newConn) {
        conn = newConn;
    }

    @Override
    protected void search(String s, int id) {
        String sql = "SELECT p.publicationID, st.series, pu.name AS publisherName, a.name AS authorName " +
                "FROM publication p " +
                "JOIN publishing pb ON p.ISBNnumber = pb.ISBNnumber " +
                "JOIN publisher pu ON pb.publisherID = pu.ID " +
                "JOIN writing w ON p.publicationID = w.publicationID " +
                "JOIN author a ON w.authorID = a.ID " +
                "JOIN story st ON p.publicationID = st.publicationID " +
                "WHERE pu.name LIKE ? OR a.name LIKE ?";
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            pstmt = conn.prepareStatement(sql);
            // 検索文字列を設定します。
            pstmt.setString(1, "%" + s + "%");
            pstmt.setString(2, "%" + s + "%");

            rs = pstmt.executeQuery();

            // 結果をループして表示します。
            while (rs.next()) {
                System.out.println("Publication ID: " + rs.getInt("publicationID") +
                        ", Series: " + rs.getString("series") +
                        ", Publisher Name: " + rs.getString("publisherName") +
                        ", Author Name: " + rs.getString("authorName"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (rs != null)
                    rs.close();
                if (pstmt != null)
                    pstmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
