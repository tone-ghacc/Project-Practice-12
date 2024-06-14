import java.sql.*;

public class SearchTag extends Search {

    private Connection conn = null;

    public SearchTag(Connection newConn) {
        conn = newConn;
    }

    @Override
    protected void search(String tag, int id) {
        String sql = "SELECT p.publicationID, st.series, pu.name AS publisherName, a.name AS authorName " +
                "FROM tagging tg " +
                "JOIN publication p ON tg.publicationID = p.publicationID " +
                "JOIN story st ON p.publicationID = st.publicationID " +
                "JOIN writing w ON p.publicationID = w.publicationID " +
                "JOIN author a ON w.authorID = a.ID " +
                "JOIN publishing pb ON p.ISBNnumber = pb.ISBNnumber " +
                "JOIN publisher pu ON pb.publisherID = pu.ID " +
                "WHERE tg.tag = ?";

        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            pstmt = conn.prepareStatement(sql);
            // 検索タグを設定します。
            pstmt.setString(1, tag);

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
