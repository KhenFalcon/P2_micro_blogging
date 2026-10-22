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
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import uga.menik.csx370.models.FollowableUser;
import uga.menik.csx370.models.SuggestedUser;
import uga.menik.csx370.models.User;

@Service
public class PeopleService {

    private final DataSource dataSource;

    @Autowired
    public PeopleService(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * Retrieves all users except the logged-in user.
     * Includes follow status and each user's most recent post date.
     */
    public List<FollowableUser> getFollowableUsers(String userIdToExclude) {
        List<FollowableUser> followableUsers = new ArrayList<>();

        String queryUsers =
                "SELECT U.userId, U.firstName, U.lastName, "
                + "(SELECT MAX(P.postDate) FROM posts P "
                + "WHERE P.userId = U.userId) AS lastActiveDate, "
                + "(SELECT COUNT(*) FROM followStatus FS "
                + "WHERE FS.followerId = ? AND FS.followeeId = U.userId) "
                + "> 0 AS isFollowed "
                + "FROM user U WHERE U.userId <> ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(queryUsers)) {

            stmt.setInt(1, Integer.parseInt(userIdToExclude));
            stmt.setInt(2, Integer.parseInt(userIdToExclude));

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String userId = rs.getString("userId");
                    String firstName = rs.getString("firstName");
                    String lastName = rs.getString("lastName");
                    boolean isFollowed = rs.getBoolean("isFollowed");

                    Timestamp sqlLastActiveDate =
                            rs.getTimestamp("lastActiveDate");

                    String lastActiveDate = "";

                    if (sqlLastActiveDate != null) {
                        DateTimeFormatter formatter =
                                DateTimeFormatter.ofPattern(
                                        "MMM d, yyyy, hh:mm a",
                                        Locale.ENGLISH);

                        lastActiveDate = sqlLastActiveDate
                                .toLocalDateTime()
                                .format(formatter);
                    }

                    followableUsers.add(new FollowableUser(
                            userId,
                            firstName,
                            lastName,
                            isFollowed,
                            lastActiveDate));
                }
            }

            return followableUsers;

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Unable to retrieve followable users.", e);
        }
    }

    private static final Pattern HASHTAG_PATTERN =
            Pattern.compile("(?<![\\p{L}\\p{N}_])#([\\p{L}\\p{N}_]+)");

    private static final int RECENT_POST_LIMIT = 20;

    /**
     * Extracts unique hashtags from a post, ignoring case.
     */
    private Set<String> extractHashtags(String content) {
        Set<String> hashtags = new HashSet<>();

        Matcher matcher = HASHTAG_PATTERN.matcher(
                content == null ? "" : content);

        while (matcher.find()) {
            hashtags.add(matcher.group(1).toLowerCase(Locale.ROOT));
        }

        return hashtags;
    }

    /**
     * Recommends users based on friends-of-friends and shared hashtags.
     * Excludes the logged-in user, users already followed, and candidates
     * with no recommendation signals.
     */
    public List<SuggestedUser> getSuggestedUsers(String currentUserId) {
        List<User> candidates = new ArrayList<>();

        // Follower ID -> set of users they follow.
        Map<String, Set<String>> following = new HashMap<>();

        // User ID -> hashtags from their recent posts.
        Map<String, Set<String>> hashtagsByUser = new HashMap<>();
        Map<String, Integer> postCounts = new HashMap<>();

        String usersSql =
                "SELECT userId, firstName, lastName "
                + "FROM user WHERE userId <> ?";

        String followsSql =
                "SELECT followerId, followeeId FROM followStatus";

        String postsSql =
                "SELECT userId, content FROM posts ORDER BY postDate DESC";

        try (Connection conn = dataSource.getConnection()) {

            // Retrieve candidates, excluding the current user.
            try (PreparedStatement stmt =
                         conn.prepareStatement(usersSql)) {

                stmt.setString(1, currentUserId);

                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        candidates.add(new User(
                                rs.getString("userId"),
                                rs.getString("firstName"),
                                rs.getString("lastName")));
                    }
                }
            }

            // Build the follow graph.
            try (PreparedStatement stmt =
                         conn.prepareStatement(followsSql);
                 ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {
                    String followerId = rs.getString("followerId");
                    String followeeId = rs.getString("followeeId");

                    following.computeIfAbsent(
                                    followerId, key -> new HashSet<>())
                            .add(followeeId);
                }
            }

            // Collect hashtags from each user's 20 most recent posts.
            try (PreparedStatement stmt =
                         conn.prepareStatement(postsSql);
                 ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {
                    String userId = rs.getString("userId");
                    int count = postCounts.getOrDefault(userId, 0);

                    if (count >= RECENT_POST_LIMIT) {
                        continue;
                    }

                    String content = rs.getString("content");

                    hashtagsByUser.computeIfAbsent(
                                    userId, key -> new HashSet<>())
                            .addAll(extractHashtags(content));

                    postCounts.put(userId, count + 1);
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Unable to retrieve suggested users.", e);
        }

        Set<String> currentFollowing =
                following.getOrDefault(currentUserId, Set.of());

        Set<String> currentHashtags =
                hashtagsByUser.getOrDefault(currentUserId, Set.of());

        List<SuggestedUser> suggestions = new ArrayList<>();

        // Score eligible candidates.
        for (User candidate : candidates) {
            String candidateId = candidate.getUserId();

            // Do not suggest users already followed.
            if (currentFollowing.contains(candidateId)) {
                continue;
            }

            // Count friends-of-friends.
            int mutualConnections = 0;

            for (String followedUserId : currentFollowing) {
                Set<String> theirFollowing =
                        following.getOrDefault(followedUserId, Set.of());

                if (theirFollowing.contains(candidateId)) {
                    mutualConnections++;
                }
            }

            // Count shared hashtags from recent posts.
            Set<String> candidateHashtags =
                    hashtagsByUser.getOrDefault(candidateId, Set.of());

            int sharedHashtags = 0;

            for (String hashtag : currentHashtags) {
                if (candidateHashtags.contains(hashtag)) {
                    sharedHashtags++;
                }
            }

            // Skip users with no recommendation signals.
            if (mutualConnections == 0 && sharedHashtags == 0) {
                continue;
            }

            suggestions.add(new SuggestedUser(
                    candidateId,
                    candidate.getFirstName(),
                    candidate.getLastName(),
                    mutualConnections,
                    sharedHashtags));
        }

        // Rank by total score, then individual signals.
        suggestions.sort(
                Comparator.comparingInt(
                                SuggestedUser::getRecommendationScore)
                        .reversed()
                        .thenComparing(
                                Comparator.comparingInt(
                                                SuggestedUser::getMutualConnections)
                                        .reversed())
                        .thenComparing(
                                Comparator.comparingInt(
                                                SuggestedUser::getSharedHashtags)
                                        .reversed())
                        .thenComparing(SuggestedUser::getFirstName)
                        .thenComparing(SuggestedUser::getLastName));

        return suggestions;
    }

    /**
     * Follows or unfollows a user by updating followStatus.
     */
    public boolean followUnfollowUser(
            int currentUserId,
            int targetUserId,
            boolean isFollowing) throws SQLException {

        String followSql =
                "INSERT INTO followStatus (followerId, followeeId) "
                + "VALUES (?, ?)";

        String unfollowSql =
                "DELETE FROM followStatus "
                + "WHERE followerId = ? AND followeeId = ?";

        String sql = isFollowing ? followSql : unfollowSql;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, currentUserId);
            stmt.setInt(2, targetUserId);

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        }
    }
}