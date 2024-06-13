import java.sql.*;

public class SearchTitle extends Search {
    private Connection connect = null;

    public SearchTitle(Connection newConn) {
        connect = newConn;
    }

    @Override
    protected void search(String titlePart, int id) {
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            String sql = "SELECT p.publicationID, s.series, pu.name, a.name " +
                    "FROM publication p " +
                    "JOIN story s ON p.publicationID = s.publicationID " +
                    "JOIN writing w ON p.publicationID = w.publicationID " +
                    "JOIN author a ON w.authorID = a.ID " +
                    "JOIN publishing pb ON p.ISBNnumber = pb.ISBNnumber " +
                    "JOIN publisher pu ON pb.publisherID = pu.ID " +
                    "WHERE s.series LIKE ?";

            pstmt = connect.prepareStatement(sql);
            pstmt.setString(1, "%" + titlePart + "%");
            rs = pstmt.executeQuery();

            while (rs.next()) {
                int publicationID = rs.getInt("p.publicationID");
                String series = rs.getString("s.series");
                String publisherName = rs.getString("pu.name");
                String authorName = rs.getString("a.name");

                System.out.println("Publication ID: " + publicationID);
                System.out.println("Series: " + series);
                System.out.println("Publisher Name: " + publisherName);
                System.out.println("Author Name: " + authorName);
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
