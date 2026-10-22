package uga.menik.csx370.models;

public class SuggestedUser extends FollowableUser {

    private final int mutualConnections;
    private final int sharedHashtags;
    private final int recommendationScore;

    public SuggestedUser(
            String userId,
            String firstName,
            String lastName,
            int mutualConnections,
            int sharedHashtags) {

        super(userId, firstName, lastName, false, "");

        this.mutualConnections = mutualConnections;
        this.sharedHashtags = sharedHashtags;
        this.recommendationScore = mutualConnections + sharedHashtags;
    }

    public int getMutualConnections() {
        return mutualConnections;
    }

    public int getSharedHashtags() {
        return sharedHashtags;
    }

    public int getRecommendationScore() {
        return recommendationScore;
    }

}