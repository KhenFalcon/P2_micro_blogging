package uga.menik.csx370.services;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;

import uga.menik.csx370.models.Post;
import uga.menik.csx370.models.User;

public class HashtagService {
    private final DataSource dataSource;
    private final UserService userService;
    private final BookmarksService bookmarksService;

    @Autowired
    public HashtagService(DataSource dataSource, UserService userService, BookmarksService bookmarksService) {
        this.dataSource = dataSource;
        this.userService = userService;
        this.bookmarksService = bookmarksService;
    }

    /**
     * Get posts that contain ANY of the specified hashtags. Hashtags should be substrings starting 
     * with the '#' character and contain no spaces.
     * 
     * Valid Examples: "#Test" "#Fireworks" "#101Dalmations"
     * Invalid Examples: "Test" "Fireworks" "#101 Dalmations"
     * 
     * @param hashtages - A single string or array of strings representing hashtags to search for
     * @return A list of posts that contain any of the specified hashtags
     */
    public List<Post> getPostsWithAny(String... hashtags) {
        List<Post> posts = new ArrayList<>();
        for (String h : hashtags) {
            posts.addAll(getPosts(h));
        }

        return posts;
    }   

    /**
     * Get posts that contain ALL of the specified hashtages. Hashtags should be substrings starting 
     * with the '#' character and contain no spaces.
     * 
     * Valid Examples: "#Test" "#Fireworks" "#101Dalmations"
     * Invalid Examples: "Test" "Fireworks" "#101 Dalmations"
     * 
     * @param hashtags - A single string or array of strings representing hashtags to search for
     * @return A list of posts that contain all of the specified hashtags
     */
    public List<Post> GetPostsWithAll(String... hashtags) {
        List<Post> posts = new ArrayList<>();
        posts.addAll(getPosts(hashtags[0])); // Get posts with the first hashtag

        for (int i = 1; i < hashtags.length; i++) {
            List<Post> temp = new ArrayList<>();
            temp.addAll(getPosts(hashtags[i])); // Get posts with the current hashtag

            posts.retainAll(temp); // Keep only posts that contain all specified hashtags
        }

        return posts;
    }

    private List<Post> getPosts(String hashtag) {
        // Write an SQL query to find the users that are not the current user.
        final String getPostsSql = "SELECT * FROM posts WHERE content LIKE ?";

        // Run the query with a datasource.
        List<Post> posts = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
                PreparedStatement getPostsStmt = conn.prepareStatement(getPostsSql)) {

            getPostsStmt.setString(1, "%" + hashtag + "%");

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

                    boolean isBookmarked = bookmarksService.isBookmarked(postId);

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
