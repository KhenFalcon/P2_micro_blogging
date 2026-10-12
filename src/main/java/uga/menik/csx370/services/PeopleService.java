/**
Copyright (c) 2026 Sami Menik, PhD. All rights reserved.

This is a project developed by Dr. Menik to give the students an opportunity to apply database concepts learned in the class in a real world project. Permission is granted to host a running version of this software and to use images or videos of this work solely for the purpose of demonstrating the work to potential employers. Any form of reproduction, distribution, or transmission of the software's source code, in part or whole, without the prior written consent of the copyright owner, is strictly prohibited.
*/
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
        final String getUsersSql = "select * from user where userId != ?";

        // Run the query with a datasource.
        // See UserService.java to see how to inject DataSource instance and
        // use it to run a query.
        List<FollowableUser> users = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
                PreparedStatement getUsersStmt = conn.prepareStatement(getUsersSql)) {

            getUsersStmt.setString(1, userIdToExclude);

            try (ResultSet rs = getUsersStmt.executeQuery()) {
                // Use the query result to create a list of followable users.
                // See UserService.java to see how to access rows and their attributes
                // from the query result.
                // Check the following createSampleFollowableUserList function to see 
                // how to create a list of FollowableUsers.
                while (rs.next()) {
                    String userId = rs.getString("userId");
                    String firstName = rs.getString("firstName");
                    String lastName = rs.getString("lastName");

                    // TODO: Placeholder values
                    boolean isFollowed = false;
                    String lastActiveDate = "N/A";
                    
                    users.add(new FollowableUser(userId, firstName, lastName, isFollowed, lastActiveDate));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Replace the following line and return the list you created.
        return users;
    }

}
