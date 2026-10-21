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

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import uga.menik.csx370.models.SuggestedUser;
import uga.menik.csx370.models.User;

@Service
public class PeopleService {

    private final DataSource dataSource;

    @Autowired
    public PeopleService(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public List<FollowableUser> getFollowableUsers(String userIdToExclude) {
        List<FollowableUser> followableUsers = new ArrayList<>();

        String sql = "SELECT userId, firstName, lastName FROM user WHERE userId <> ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, userIdToExclude);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String userId = rs.getString("userId");
                    String firstName = rs.getString("firstName");
                    String lastName = rs.getString("lastName");

                    followableUsers.add(new FollowableUser(
                            userId, firstName, lastName, false, ""));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Unable to retrieve followable users.", e);
        }

        return followableUsers;
    }
    private static final Pattern HASHTAG_PATTERN =
            Pattern.compile("(?<![\\p{L}\\p{N}_])#([\\p{L}\\p{N}_]+)");

    private Set<String> extractHashtags(String content) {
        Set<String> hashtags = new HashSet<>();

        Matcher matcher = HASHTAG_PATTERN.matcher(
                content == null ? "" : content);

        while (matcher.find()) {
            hashtags.add(matcher.group(1).toLowerCase(Locale.ROOT));
        }

        return hashtags;
    }

    private int intersectionSize(Set<String> first, Set<String> second) {
        Set<String> smaller = first.size() <= second.size() ? first : second;
        Set<String> larger = first.size() <= second.size() ? second : first;

        int count = 0;

        for (String value : smaller) {
            if (larger.contains(value)) {
                count++;
            }
        }

        return count;
    }

    public List<SuggestedUser> getSuggestedUsers(String currentUserId) {
        List<User> candidates = new ArrayList<>();

        Map<String, Set<String>> following = new HashMap<>();
        Map<String, Set<String>> hashtagsByUser = new HashMap<>();

        String usersSql =
                "SELECT userId, firstName, lastName FROM user WHERE userId <> ?";

        String followsSql =
                "SELECT followerId, followeeId FROM followStatus";

        String postsSql =
                "SELECT userId, content FROM posts";

        try (Connection conn = dataSource.getConnection()) {

            // Get every potential candidate except the logged-in user.
            try (PreparedStatement stmt = conn.prepareStatement(usersSql)) {
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

            // Build a map of each user's followed accounts.
            try (PreparedStatement stmt = conn.prepareStatement(followsSql);
                 ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {
                    String followerId = rs.getString("followerId");
                    String followeeId = rs.getString("followeeId");

                    following.computeIfAbsent(
                            followerId, key -> new HashSet<>()).add(followeeId);
                }
            }

            // Extract unique hashtags from each user's posts.
            try (PreparedStatement stmt = conn.prepareStatement(postsSql);
                 ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {
                    String userId = rs.getString("userId");
                    String content = rs.getString("content");

                    hashtagsByUser.computeIfAbsent(
                                    userId, key -> new HashSet<>())
                            .addAll(extractHashtags(content));
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

        for (User candidate : candidates) {
            String candidateId = candidate.getUserId();

            // Do not recommend accounts the current user already follows.
            if (currentFollowing.contains(candidateId)) {
                continue;
            }

            Set<String> candidateFollowing =
                    following.getOrDefault(candidateId, Set.of());

            Set<String> candidateHashtags =
                    hashtagsByUser.getOrDefault(candidateId, Set.of());

            int mutualConnections = intersectionSize(
                    currentFollowing, candidateFollowing);

            int sharedHashtags = intersectionSize(
                    currentHashtags, candidateHashtags);

            suggestions.add(new SuggestedUser(
                    candidateId,
                    candidate.getFirstName(),
                    candidate.getLastName(),
                    mutualConnections,
                    sharedHashtags));
        }

        // Highest combined score first, then use each component as a tiebreaker.
        suggestions.sort(
                Comparator.comparingInt(SuggestedUser::getRecommendationScore)
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
}
