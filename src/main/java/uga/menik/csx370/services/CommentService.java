package uga.menik.csx370.services;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
// import java.sql.Timestamp;
// import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
// import java.util.Locale;

import javax.sql.DataSource;

import org.springframework.stereotype.Service;

import uga.menik.csx370.models.Comment;
import uga.menik.csx370.models.User;

@Service
public class CommentService {
    
    private final DataSource dataSource;
    private final UserService userService; 

    public CommentService(DataSource dataSource, UserService userService) {
        this.dataSource = dataSource;
        this.userService = userService;
    }

    /**
     * Gets the comments on a given post.
     * 
     * @param postId - the postId of selected post
     * @return A list of comment objects that respond to the provided postId
     */
    public List<Comment> getComments(String postId) {
        // Prep Query - Getting the comments that the specific post that corresponds to postId has
        final String getCommentsSql = "SELECT * FROM comment WHERE postId = ? ORDER BY postDate ASC";

        // Prep return object
        ArrayList<Comment> comments = new ArrayList<>();

        // Prep Query Params: postId = postId;

        // Connect to db - Querying comments and adding comments of specifc post to a list 
        try (Connection conn1 = dataSource.getConnection();
                PreparedStatement registerStmt1 = conn1.prepareStatement(getCommentsSql)) {

            // Insert Query Params
            registerStmt1.setString(1, postId);

            // Send Query
            try (ResultSet results = registerStmt1.executeQuery()) {
                // Compile response into list
                while (results.next()) {
                    String postid = String.valueOf(results.getInt("postId"));
                    String content = results.getString("content");

                    String parsepostDate = results.getString("postDate");
                    // Timestamp postDate = results.getTimestamp("postDate");

                    // DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM d, yyyy, hh:mm a", Locale.ENGLISH);
                    // String parsepostDate = "";
                    // if (postDate != null) {
                    //     parsepostDate = postDate.toLocalDateTime().format(formatter);
                    // }
                    // parsing postDate (current time of comment) to be in the format of formatter 

                    String userId = String.valueOf(results.getInt("userId"));
                    User user = userService.getUser(userId);

                    Comment c = new Comment(postid, content, parsepostDate, user);
                    comments.add(c);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return comments;
    }
}
