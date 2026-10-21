package uga.menik.csx370.services;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class HeartsService {
    private final DataSource dataSource;
    private final UserService userService;

    @Autowired
    public HeartsService(DataSource dataSource, UserService userService) {
        this.dataSource = dataSource;
        this.userService = userService;
    }

    /**
     * Checks if the currently logged-in user has liked (heart-ed) a given post. 
     * 
     * @param postId - The postId of the post to check if the user has liked (heart-ed)
     * @return True if the currently logged-in user has liked the post, false otherwise. 
     */
    public boolean isLiked(String postId) {
        // Prep Query - Write an SQL query to insert a check if a user-likes-post entry exists in DB
        final String getLikekSql = "SELECT * FROM hearts WHERE userId = ? AND postId = ?";

        // Prep Query Params
        // postId = postId;
        String userId = userService.getLoggedInUser().getUserId();

        // Connect to DB - Run the query with a datasource.
        try (Connection conn = dataSource.getConnection();
                PreparedStatement getLikeStmt = conn.prepareStatement(getLikekSql)) {

            // Insert Query Params
            getLikeStmt.setString(1, userId);
            getLikeStmt.setString(2, postId);

            // Send Query
            try (ResultSet rs = getLikeStmt.executeQuery()) {
                return rs.next(); // Return true/false based on if there is an appropriate entry in hearts
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Adds a new heart-relation entry to the database. It uses the provided postId and the userId 
     * of the currently logged in user, and essentially creates a "user likes post" statement in 
     * the hearts table.
     * 
     * @param postId - The postId of the post the currently logged-in user wants to like.
     */
    public void addLike(String postId) {
        // Prep Query - Write an SQL query to insert a new user-likes-post entry into the database.
        final String createPostSql = "INSERT INTO hearts (userId, postId) VALUES (?, ?)";

        // Prep Query Params
        // postId = postId;
        String userId = userService.getLoggedInUser().getUserId();

        // Connect to DB
        try (Connection conn = dataSource.getConnection();
                PreparedStatement createPostStmt = conn.prepareStatement(createPostSql)) {

            // Insert Query Params
            createPostStmt.setString(1, userId);
            createPostStmt.setString(2, postId);

            // Send Query
            createPostStmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Removes any heart-relation entries from the Databse that uses both the logged in user's ID 
     * AND the provided postId. This method assumes that such an entry already exists in the 
     * hearts table, but no error will occur if this is not the case. 
     * 
     * @param postId - The postId of the post the corrently logged in user no longer likes.
     */
    public void remomveLike(String postId) {
        // Write an SQL query to insert a new post-bookmark into the database.x
        final String createPostSql = "DELETE FROM hearts WHERE userId = ? AND postId = ?";

        // Prep Query Params
        // postId = postId;
        String userId = userService.getLoggedInUser().getUserId();

        // Connect to DB
        try (Connection conn = dataSource.getConnection();
                PreparedStatement createPostStmt = conn.prepareStatement(createPostSql)) {


            // Insert Query Params
            createPostStmt.setString(1, userId);
            createPostStmt.setString(2, postId);

            // Send Query
            createPostStmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}