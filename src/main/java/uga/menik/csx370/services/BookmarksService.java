package uga.menik.csx370.services;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import uga.menik.csx370.models.Post;
import uga.menik.csx370.models.User;

@Service 
public class BookmarksService {
    private final DataSource dataSource;
    private final UserService userService;
    private final HeartsService heartsService;

    @Autowired
    public BookmarksService(DataSource dataSource, UserService userService, HeartsService heartsService) {
        this.dataSource = dataSource;
        this.userService = userService;
        this.heartsService = heartsService;
    }

    /**
     * Get posts that are bookmarked by the provided user
     * @param userId
     * @return A list of posts that are bookmarked by the provided user
     */
    public List<Post> getPosts(String userId) {
        // Write an SQL query to find the users that are not the current user.
        final String getPostsSql = "SELECT * FROM posts WHERE postId IN (SELECT postId FROM bookmarks WHERE userId = ?) ORDER BY postDate DESC";

        // Run the query with a datasource.
        List<Post> posts = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
                PreparedStatement getPostsStmt = conn.prepareStatement(getPostsSql)) {

            getPostsStmt.setString(1, userId);

            try (ResultSet rs = getPostsStmt.executeQuery()) {
                // Use the query result to create a list of posts
                while (rs.next()) {
                    String postId = rs.getString("postId");
                    String postUserId = rs.getString("userId"); // use to get post's user
                    String content = rs.getString("content");
                    String postDate = PostService.convertTimeStamp(rs.getTimestamp("postDate"));
                    User postUser = userService.getUser(postUserId); // Get the user object for the post's user
                    int heartsCount = rs.getInt("heartsCount");
                    int commentsCount = rs.getInt("commentsCount");
                    boolean isHearted = heartsService.isHearted(postId);
                    boolean isBookmarked = true;

                    posts.add(new Post(postId, content, postDate, postUser, heartsCount, commentsCount, isHearted,
                            isBookmarked));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Replace the following line and return the list you created.
        return posts;
    }

    /**
     * Checks if the post associated with the provided postId is bookmarked by the current logged-in user.
     * @param postId
     * @return true if the provided post (ID) is bookmarked by the current logged-in user, false otherwise.
     */
    public boolean isBookmarked(String postId) {
        // Prep Query - Write an SQL query to find the users that are not the current user.
        final String getBookmarkSql = "SELECT * FROM bookmarks WHERE userId = ? AND postId = ?";

        // Prep Query Params
        // postId = postId;
        String userId = userService.getLoggedInUser().getUserId();

        // Connect to DB - Run the query with a datasource.
        try (Connection conn = dataSource.getConnection();
                PreparedStatement getBookmarkStmt = conn.prepareStatement(getBookmarkSql)) {

            // Insert Query Params
            getBookmarkStmt.setString(1, userId);
            getBookmarkStmt.setString(2, postId);

            // Send Query
            try (ResultSet rs = getBookmarkStmt.executeQuery()) {
                return rs.next(); // Return true/false based on if there is an appropriate entry in bookmarks
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Replace the following line and return the list you created.
        return false;
    }

     /**
     * Adds a bookmark associated with the provided postId for the current logged-in user.
     * @param postId
     */    
    public void addBookmark(String postId) {        
        // Prep Query - Write an SQL query to insert a new post-bookmark into the database.
        final String createPostSql = "INSERT INTO bookmarks (userId, postId) VALUES (?, ?)";

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
     * Removes the bookmark associated with the provided postId for the current logged-in user.
     * @param postId
     */
    public void removeBookmark(String postId) {
        // Prep Query - Write an SQL query to insert a new post-bookmark into the database.
        final String createPostSql = "DELETE FROM bookmarks WHERE userId = ? AND postId = ?";

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
