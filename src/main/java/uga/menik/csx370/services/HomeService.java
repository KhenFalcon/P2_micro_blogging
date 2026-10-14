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
public class HomeService {
    private final DataSource dataSource;

    @Autowired
    public HomeService(DataSource dataSource) {
        this.dataSource = dataSource;
    }

     /**
     * TEMP: This function should query and return all posts.
     */
    public List<Post> getPosts(String userIdToExclude) {
        // Write an SQL query to find the users that are not the current user.
        final String getPostsSql = "select * from basicPost";

        // Run the query with a datasource.
        // See UserService.java to see how to inject DataSource instance and
        // use it to run a query.
        List<Post> posts = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
                PreparedStatement getPostsStmt = conn.prepareStatement(getPostsSql)) {

            // getUsersStmt.setString(1, ?txtReplacement);

            try (ResultSet rs = getPostsStmt.executeQuery()) {
                // Use the query result to create a list of followable users.
                // See UserService.java to see how to access rows and their attributes
                // from the query result.
                // Check the following createSampleFollowableUserList function to see 
                // how to create a list of FollowableUsers.
                while (rs.next()) {
                    String postId = rs.getString("postId");
                    int userId = rs.getInt("userId"); // use to get post's user
                    String content = rs.getString("content");
                    String postDate = rs.getString("postDate");
                    
                    // TODO: Placeholder values
                    User postUser = null; 
                    int heartsCount = 0;
                    int commentsCount = 0;
                    boolean isHearted = false;
                    boolean isBookmarked = false;

                    posts.add(new Post(postId, content, postDate, postUser, heartsCount, commentsCount, isHearted, isBookmarked));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Replace the following line and return the list you created.
        return posts;
    }
}
