package uga.menik.csx370.services;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import uga.menik.csx370.models.Post;
import uga.menik.csx370.models.User;

@Service
public class PostService {
    private final DataSource dataSource;
    private final UserService userService;
    private final BookmarksService bookmarksService;

    @Autowired
    public PostService(DataSource dataSource, UserService userService, BookmarksService bookmarksService) {
        this.dataSource = dataSource;
        this.userService = userService;
        this.bookmarksService = bookmarksService;
    }

     /**
     * TEMP: This function should query and return all posts.
     */
    public List<Post> getPosts(String userIdToExclude) {
        // Write an SQL query to find the users that are not the current user.
        final String getPostsSql = "select * from posts";

        // Run the query with a datasource.
        // See UserService.java to see how to inject DataSource instance and
        // use it to run a query.
        List<Post> posts = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
                PreparedStatement getPostsStmt = conn.prepareStatement(getPostsSql)) {

            try (ResultSet rs = getPostsStmt.executeQuery()) {
                // Use the query result to create a list of followable users.
                // See UserService.java to see how to access rows and their attributes
                // from the query result.
                // Check the following createSampleFollowableUserList function to see 
                // how to create a list of FollowableUsers.
                while (rs.next()) {
                    String postId = rs.getString("postId");
                    String userId = rs.getString("userId"); // use to get post's user
                    String content = rs.getString("content");
                    String postDate = rs.getString("postDate");
                    User postUser = userService.getUser(userId); // Get the user object for the post's user
                    
                    // TODO: Placeholder values
                    int heartsCount = 0;
                    int commentsCount = 0;
                    boolean isHearted = false;

                    boolean isBookmarked = bookmarksService.isBookmarked(postId);

                    posts.add(new Post(postId, content, postDate, postUser, heartsCount, commentsCount, isHearted, isBookmarked));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Replace the following line and return the list you created.
        return posts;
    }

    /**
     * TEMP: This function should insert a new post into the database. Uncomment the provided code once the database schema is ready.
     */
    public void createPost(String postText) {
        if (postText.equals(""))
            return;
        
        // Write an SQL query to insert a new post into the database.
        final String createPostSql = "INSERT INTO posts (postId, content, postDate, userId, heartsCount, commentsCount, isHearted, isBookmarked) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        String postId = getNewPostId(); // Generate a new post ID based on the current timestamp
        String content = postText;
        String postDate = Timestamp.valueOf(java.time.LocalDateTime.now()).toString(); // Get the current timestamp as a string
        User user = userService.getLoggedInUser(); // Get the logged in user object
        int heartsCount = 0;
        int commentsCount = 0;
        boolean isHearted = false;
        boolean isBookmarked = false;

        try (Connection conn = dataSource.getConnection();
                PreparedStatement createPostStmt = conn.prepareStatement(createPostSql)) {

            createPostStmt.setString(1, postId);
            createPostStmt.setString(2, content);
            createPostStmt.setString(3, postDate);
            createPostStmt.setString(4, user.getUserId());
            createPostStmt.setInt(5, heartsCount);
            createPostStmt.setInt(6, commentsCount);
            createPostStmt.setBoolean(7, isHearted);
            createPostStmt.setBoolean(8, isBookmarked);

            createPostStmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * TEMP: This function should generate a new, unused postId based on the current posts made in 
     * the DB. Should also consider a better way to generate a new postId (time-based?). For now, 
     * this function will return a new postId based on the maximum postId in the database. The 
     * theoretical limit is 2,147,483,647 (the maximum value of a 32-bit signed integer). If the 
     * maximum postId is already at this limit, this function will return -1 to indicate that no 
     * new postId can be generated. This is a temporary solution and should be improved in the future.
     */
    public String getNewPostId() {
        // Write an SQL query to find the maximum postId in the database.
        final String getMaxPostIdSql = "SELECT MAX(postId) AS maxPostId FROM posts";

        try (Connection conn = dataSource.getConnection();
                PreparedStatement getMaxPostIdStmt = conn.prepareStatement(getMaxPostIdSql)) {

            try (ResultSet rs = getMaxPostIdStmt.executeQuery()) {
                if (rs.next()) {
                    int newPostIdInt = rs.getInt("maxPostId") + 1; // Increment the max postId by 1
                    return "" + newPostIdInt; // Return the new postId as a string
                }
            }
        } catch (NullPointerException e) {
            // db has no posts (so starting id is 1)
            return "1";
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return "-1"; // Default to "-1" if there is an error
    }
}
