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

    @Autowired
    public BookmarksService(DataSource dataSource, UserService userService) {
        this.dataSource = dataSource;
        this.userService = userService;
    }

    /**
     * Get posts that are bookmarked by the provided user
     * @param userId
     * @return
     */
    public List<Post> getPosts(String userId) {
        // Write an SQL query to find the users that are not the current user.
        final String getPostsSql = "SELECT * FROM posts WHERE postId IN (SELECT postId FROM bookmarks WHERE userId = ?)";

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
                    String postDate = rs.getString("postDate");
                    User postUser = userService.getUser(postUserId); // Get the user object for the post's user

                    // TODO: Placeholder values
                    int heartsCount = 0;
                    int commentsCount = 0;
                    boolean isHearted = false;

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
}
