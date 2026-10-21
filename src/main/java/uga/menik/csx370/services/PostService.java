package uga.menik.csx370.services;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import uga.menik.csx370.models.Comment;
import uga.menik.csx370.models.ExpandedPost;
import uga.menik.csx370.models.Post;
import uga.menik.csx370.models.User;

@Service
public class PostService {
    private final DataSource dataSource;
    private final UserService userService;
    private final BookmarksService bookmarksService;
    private final CommentService commentService;

    @Autowired
    public PostService(DataSource dataSource, UserService userService, BookmarksService bookmarksService, CommentService commentService) {
        this.dataSource = dataSource;
        this.userService = userService;
        this.bookmarksService = bookmarksService;
        this.commentService = commentService;
    }

    /**
     * Gets all the posts from a specified user (Unsorted).
     * 
     * @param userId the userId of the desired user to get all their posts of 
     * @return A list of posts all sharing the same userId. Returns an empty list if something goes wrong.
     */
    public List<Post> getPosts(String userId) {
        // Write an SQL query to find the users that are not the current user.
        final String getPostsSql = "SELECT * FROM posts WHERE userId = ? LIMIT 50"; // limit in case of larger scaling

        // prep query params: userId = userId

        // prep return object
        List<Post> posts = new ArrayList<>();

        // Run the query
        try (Connection conn = dataSource.getConnection();
                PreparedStatement getPostsStmt = conn.prepareStatement(getPostsSql)) {

            // Insert query params
            getPostsStmt.setString(1, userId);

            // Send the query
            try (ResultSet rs = getPostsStmt.executeQuery()) {

                // Parse response into list of posts
                while (rs.next()) {
                    String postId = rs.getString("postId");
                    String content = rs.getString("content");
                    String postDate = rs.getString("postDate");
                    User postUser = userService.getUser(userId); // Get the user object for the post's user
                    int heartsCount = rs.getInt("heartsCount");
                    int commentsCount = rs.getInt("commentsCount");
                    boolean isHearted = rs.getBoolean("isHearted"); // DEPRECIATED: not in SQL db anymore. Needs it's own table
                    boolean isBookmarked = bookmarksService.isBookmarked(postId); // isHearted needs to follow a similar format

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
     * Allows for us to see ExpandedPost which has all attributes of BasicPost and Post, but you can see comments
     * Returns ExpandedPost instance of post to show comments.
     * 
     * Gets the expanded post of the provided postId.
     * 
     * @param postId - The postId of the desired expandedPost
     * @return An Expanded Post object or null if something goes wrong.
     */
    public ExpandedPost getExpandedPost(int postId) {
        // Prep Query - getting the corresponding post according to postId
        String expandpost = "SELECT * FROM posts P, user U WHERE P.userId = U.userId AND P.postId = ?";

        // Prep return object
        ExpandedPost expandedPost = null;

        // Prep Query params: postId = postId

        // Connect to db - creating ExpandedPost based on sql query
        try (Connection conn = dataSource.getConnection();
                PreparedStatement registerStmt = conn.prepareStatement(expandpost)) {

            // Insert Query Params
            registerStmt.setInt(1, postId);

            // Send Query
            try (ResultSet result = registerStmt.executeQuery()) {

                // Parse response into an ExpandedPost object 
                if (result.next()) {
                    String postid = String.valueOf(result.getInt("postId"));
                    String content = result.getString("content");
                    String parsepostDate = result.getString("postDate");
                    // Timestamp postDate = result.getTimestamp("postDate");

                    // String parsepostDate = "";
                    // if (postDate != null) {
                    //     parsepostDate = postDate.toLocalDateTime().format(formatter);
                    //     // parsing postDate (current time of comment) to be in the format of formatter 
                    // }

                    String userId = result.getString("userId");
                    int heartsCount = result.getInt("heartsCount");
                    int commentsCount = result.getInt("commentsCount");
                    boolean isHearted = result.getBoolean("isHearted");
                    boolean isBookmarked = bookmarksService.isBookmarked(postid);

                    User user = userService.getUser(userId);
                    List<Comment> comments = commentService.getComments("" + postId);

                    expandedPost = new ExpandedPost(postid, content, parsepostDate, user, heartsCount, commentsCount,
                            isHearted, isBookmarked, comments);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return expandedPost;
    }

     /**
     * Allows the user to add or remove a heart from a post.
     * 
     * Updates the heart (like) count of a given post. A given user can only add a single heart to 
     * any given post and can only remove a max of one heart from a any post, given that the same 
     * user had previously already heart-ed (liked) it. This update also affects the graphical 
     * indication that shows if the user had already liked the post.
     * 
     * The logic to see if a user had previously liked the post (whether the user can like or unlike
     * the post) is left outside the scope of this method.
     * 
     * @param isAdd - A boolean representing to like or unlike the post (+1 or -1 the heartCount respectively)
     * @param postId - The postId of the post to (un)like.
     * @returns True if the action was a success, false otherwise.
     */
    public boolean addRemoveHeart(boolean isAdd, int postId) {
        String addHeart = "UPDATE posts SET isHearted = true, heartsCount = heartsCount + 1 WHERE postId = ?";
        String removeHeart = "UPDATE posts SET isHearted = false, heartsCount = heartsCount - 1 WHERE postId = ?";

        String heartQuery;

        if (isAdd) {
            heartQuery = addHeart;
        } else {
            heartQuery = removeHeart;
        }

        try (Connection conn = dataSource.getConnection();
                PreparedStatement registerStmt = conn.prepareStatement(heartQuery)) {
            registerStmt.setInt(1, postId);

            int rowsAffected = registerStmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Gets all the posts from all the users that the given user is currently following.
     * 
     * @param followerUserId - The userId of the user that is following the other(s)
     * @return A list of posts where each userId is followed by the provided userId. Returns an empty
     * list if something goes wrong.
     */
    public List<Post> getPostsFromFollowedUsers(String followerUserId) {
        final String getFolloweesSql = "SELECT followeeId FROM followStatus WHERE followerId = ?";
        
        // prep query params
        String userId = followerUserId;

        // prep return object
        List<Post> posts = new ArrayList<>();

        // Run the query 
        try (Connection conn = dataSource.getConnection();
                PreparedStatement getFolloweesStmt = conn.prepareStatement(getFolloweesSql)) {

            // Insert query params
            getFolloweesStmt.setString(1, userId);

            // Send the query
            try (ResultSet rs = getFolloweesStmt.executeQuery()) {

                // Parse users and get their posts
                while (rs.next()) {
                    String followeeUserId = rs.getString("followeeId");
                    List<Post> followeePosts = getPosts(followeeUserId);
                    posts.addAll(followeePosts);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Return all the posts from each user
        return posts;
    }

    /**
     * Creates POST query to add a new post to the SQL database. Assumes the post-er is the currently logged-in user.
     * 
     * @param postText - The text-contents of the post to add to the database
     * @return True if the post was successful, false otherwise.
     */
    public boolean createPost(String postText) {
        if (postText.equals(""))
            return false;

        // Write an SQL query to insert a new post into the database.
        final String createPostSql = "insert into posts (content, postDate, userId) values (?, ?, ?)";

        // Prep query params
        String content = postText;
        Timestamp postDate = getNow();
        String user = userService.getLoggedInUser().getUserId();

        // Run the query
        try (Connection conn = dataSource.getConnection();
                PreparedStatement createPostStmt = conn.prepareStatement(createPostSql)) {

            // Insert query params
            createPostStmt.setString(1, content);
            createPostStmt.setTimestamp(2, postDate);
            createPostStmt.setString(3, user);

            // Send the query
            int rowsAffected = createPostStmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Gets now as a SQL Timestamp object. Formmated as "yyy-MM-dd HH:mm:ss".
     * @return Now as a SQL TimeStamp object
     */
    private Timestamp getNow() {
        // Specify the format for postDate to be in the sql database
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);

        // Parsing the current time into that format 
        String parsepostDate = LocalDateTime.now().format(formatter);

        // Return formatted time as Timestamp
        LocalDateTime postDate = LocalDateTime.parse(parsepostDate, formatter);
        return Timestamp.valueOf(postDate);
    }
}
