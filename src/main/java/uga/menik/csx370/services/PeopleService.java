/**
Copyright (c) 2026 Sami Menik, PhD. All rights reserved.

This is a project developed by Dr. Menik to give the students an opportunity to apply database concepts learned in the class in a real world project. Permission is granted to host a running version of this software and to use images or videos of this work solely for the purpose of demonstrating the work to potential employers. Any form of reproduction, distribution, or transmission of the software's source code, in part or whole, without the prior written consent of the copyright owner, is strictly prohibited.
*/
package uga.menik.csx370.services;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import uga.menik.csx370.models.FollowableUser;

/**
 * This service contains people related functions.
 */
@Service
public class PeopleService {

    private final DataSource dataSource;

    @Autowired
    public PeopleService(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * This function should query and return all users that 
     * are followable. The list should not contain the user 
     * with id userIdToExclude.
     */
    public List<FollowableUser> getFollowableUsers(String userIdToExclude) {
        // Write an SQL query to find the users that are not the current user.
        String queryUsers = "SELECT U.userId, U.firstName, U.lastname, "
                + "(SELECT MAX(P.postDate) FROM posts P WHERE P.userId = U.userId) AS lastActiveDate, "
                + "(SELECT COUNT(*) FROM followStatus FS WHERE FS.followerId = ? AND FS.followeeId = U.userId) > 0 AS isFollowed "
                + "FROM user U "
                + "WHERE userId != ?";
        List<FollowableUser> followable_users = new ArrayList<>();

        // Run the query with a datasource.
        // See UserService.java to see how to inject DataSource instance and
        // use it to run a query.

        try (Connection conn = dataSource.getConnection();
                PreparedStatement registerStmt = conn.prepareStatement(queryUsers)) {
            // Following lines replace the placeholders 1-4 with values.
            registerStmt.setInt(1, Integer.valueOf(userIdToExclude));
            registerStmt.setInt(2, Integer.valueOf(userIdToExclude));

            try (ResultSet resultexceptUserId = registerStmt.executeQuery()) {
                // returns rows of select query

                while (resultexceptUserId.next()) {
                    String userId = resultexceptUserId.getString("userId");
                    String firstName = resultexceptUserId.getString("firstName");
                    String lastName = resultexceptUserId.getString("lastName");
                    boolean isFollowed = resultexceptUserId.getBoolean("isFollowed");

                    Timestamp sqlLastActiveDate = resultexceptUserId.getTimestamp("lastActiveDate");
                    String lastActiveDate = "";
                    if (sqlLastActiveDate != null) {
                        // specifying the format we want postDate to be in 
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM d, yyyy, hh:mm a",
                                Locale.ENGLISH);

                        // parsing postDate (current time of comment) to be in the format of formatter
                        lastActiveDate = sqlLastActiveDate.toLocalDateTime().format(formatter);
                    }

                    FollowableUser user = new FollowableUser(userId, firstName, lastName, isFollowed, lastActiveDate);
                    followable_users.add(user);
                }
            }
            return followable_users;

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return followable_users;
    }

    /**
     * Does the logic to follow or unfollow a specifc user 
     */
    public boolean followUnfollowUser(int currentUserId, int targetUserId, boolean isFollowing) throws SQLException{
        String following = "INSERT INTO followStatus (followerId, followeeId) VALUES (?, ?)";
        String unfollow = "DELETE FROM followStatus WHERE followerId = ? AND followeeId = ?";

        String followUnfollow;
        if (isFollowing) {
            followUnfollow = following;
        } else {
            followUnfollow = unfollow;
        }

        try (Connection conn = dataSource.getConnection();
                PreparedStatement registerStmt = conn.prepareStatement(followUnfollow)) {
            registerStmt.setInt(1, currentUserId);
            registerStmt.setInt(2, targetUserId);

            int rowsAffected = registerStmt.executeUpdate();
            return rowsAffected > 0; 
        }
    }
}
