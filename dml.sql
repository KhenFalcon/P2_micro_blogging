--- Bookmark Service Queries ----------------------------------------------------------------------
--- Location: BookmarkServices.getPosts()
--- Webpage: http://localhost:8081/bookmarks
--- Description: Gets the posts that are bookmarked by a specific userId
SELECT * FROM posts WHERE postId IN (SELECT postId FROM bookmarks WHERE userId = ?) ORDER BY postDate DESC;

-- Location: BookmarkServices.isBookmarked()
--- Webpage: http://localhost:8081/, http://localhost:8081/post/{postId}, 
--- Webpage: http://localhost:8081/hashtagsearch?
--- Description: Checks if a post is bookmarked by a specific user
SELECT * FROM bookmarks WHERE userId = ? AND postId = ?;

--- Location: BookmarkServices.addBookmark()
--- Webpage: http://localhost:8081/{postId}/bookmark/{isAdd}
--- Description: Adds a new entry to bookmarks so the above query will return true (a non-empty relation)
INSERT INTO bookmarks (userId, postId) VALUES (?, ?);

--- Location: BookmarkServices.removeBookmark()
--- Webpage: http://localhost:8081/{postId}/bookmark/{isAdd}
--- Description: Undos the above query
DELETE FROM bookmarks WHERE userId = ? AND postId = ?;

--- Comment Service Queries -----------------------------------------------------------------------
--- Location: CommentService.getComments()
--- Webpage: http://localhost:8081/{postId}
--- Description: Gets the comments of a specific post
SELECT * FROM comment WHERE postId = ? ORDER BY postDate ASC;

--- Hashtag Service Queries -----------------------------------------------------------------------
--- Location: HashtagService.getPosts()
--- Webpage: http://localhost:8081/hashtagsearch?
--- Description: Gets posts that contain a certain hashtag substring
SELECT * FROM posts WHERE content LIKE ?;

--- Hearts Service Queries ------------------------------------------------------------------------
--- Location: HeartsService.isHearted()
--- Webpage: http://localhost:8081/, http://localhost:8081/post/{postId}, 
--- Webpage: http://localhost:8081/bookmarks, http://localhost:8081/hashtagsearch?
--- Description: Checks if a post is heart-ed (liked) by a specific user
SELECT * FROM hearts WHERE userId = ? AND postId = ?;

--- Location: HeartsService.addHeart()
--- Webpage: http://localhost:8081/{postId}/heart/{isAdd}
--- Description: Adds a heart a new entry to hearts so the above query will return true (a non-empty relation)
INSERT INTO hearts (userId, postId) VALUES (?, ?);

--- Location: HeartsService.removeHeart()
--- Webpage: http://localhost:8081/{postId}/heart/{isAdd}
--- Description: Undos the above query
DELETE FROM hearts WHERE userId = ? AND postId = ?

--- People Service Queries ------------------------------------------------------------------------
--- Location: PeopleService.getFollowableUsers()
--- webpage: http://localhost:8081/people 
--- selects all the users excluding the current logged in user necessary for the People page 
--- obtains the userId, firstName, and lastName of all users except the current user 
--- obtains the lastActiveDate by determining when was the last time users posted a post 
--- obtains the isFollowed by determining if current user currently follows the user 
select U.userId, U.firstName, U.lastname, (select MAX(P.postDate) from posts P where P.userId = U.userId) as lastActiveDate, (select COUNT(*) from followStatus FS where FS.followerId = ? and FS.followeeId = U.userId) > 0 as isFollowed from user U where userId != ?

--- Location: PeopleService.getFollowableUsers()
--- webpage: http://localhost:8081/people 
--- The following three queries are used together to get the suggested users for the people's page:
--- 1) gets all followable users (not including the user currently logged in)
SELECT userId, firstName, lastName FROM user WHERE userId <> ?;
--- 2) gets the entire follower table that will then be used to map results of the other two queries
SELECT followerId, followeeId FROM followStatus;
--- 3) gets the post-contents of users that is then parsed into hashtags. This will then be used to
--- compare common hashtags of the users of the first query with the current user's
SELECT userId, content FROM posts ORDER BY postDate DESC;

--- webpage: http://localhost:8081/{userId}/follow/{isFollow}
--- Location: PeopleService.followUnfollowUser()
--- The two of these queries are used to add & remove follower/followee relation-entries from the
--- follower table respectively
INSERT INTO followStatus (followerId, followeeId) VALUES (?, ?);
DELETE FROM followStatus WHERE followerId = ? AND followeeId = ?;

--- Post Service Queries --------------------------------------------------------------------------
--- webpage: http://localhost:8081/
--- Location: PostService.getPosts()
--- Description: Gets the posts from a specific user.
SELECT * FROM posts WHERE userId = ?;

--- webpage: http://localhost:8081/{postId}
--- Location: PostService.getExpandedPost()
--- Gets the posts from a specific user. (used in a different context, but should be removed for the
--- sake of the previous query. Can't because of time constraints)
SELECT * FROM posts P, user U WHERE P.userId = U.userId AND P.postId = ?;

--- Location: PostService.comment()
--- Webpage: http://localhost:8081/post/{postId}
--- inserting comments made under posts into comment table.
INSERT INTO comment (postId, content, postDate, userId) VALUES (?, ?, ?, ?);

--- Location: PostService.comment()
--- Webpage: http://localhost:8081/post/{postId}
--- updating the commentsCount variable (incrementing by 1) when comments are added under specifc post.
UPDATE posts SET commentsCount = commentsCount + 1 WHERE postId = ?;

--- Location: PostService.addRemoveHeart()
--- Webpage: http://localhost:8081/post/{postId}/heart/{isAdd}
--- Description: Increments the post's heartsCount when the current user adds a heart.
UPDATE posts SET heartsCount = heartsCount + 1 WHERE postId = ?;

--- Location: PostService.addRemoveHeart()
--- Webpage: http://localhost:8081/post/{postId}/heart/{isAdd}
--- Description: Decrements the post's heartsCount when the current user removes a heart.
UPDATE posts SET heartsCount = heartsCount - 1 WHERE postId = ?;

--- Location: PostService.getPostsFromFollowedUsers()
--- webpage: http://localhost:8081/
--- Description: Gets the IDs of users followed by the current user so their posts can be retrieved.
SELECT followeeId FROM followStatus WHERE followerId = ?;

--- Location: PostService.createPost()
--- webpage: http://localhost:8081/
--- inserting posts created from users into posts table.
INSERT INTO posts (content, postDate, userId) VALUES (?, ?, ?);

--- Location: PostService.postsFromCurrentUser()
--- webpage: http://localhost:8081/profile
--- selects all post information as well as user information on posts created by current logged in user 
--- sorts these posts in descending order -> most recent to oldest 
--- necessary for profile page to see specifc posts from user 
SELECT P.postId, P.content, P.postDate, P.userId, P.heartsCount, P.commentsCount, P.isHearted, P.isBookmarked, U.firstName, U.lastName FROM posts P, user U WHERE P.userId = U.userId AND P.userId = ? ORDER BY postDate DESC;

--- User Service Queries ---------------------------------------------------------------------------
--- Location: UserService.authenticate()
--- Webpage: http://localhost:8081/login
--- Description: Finds the account associated with a username to authenticate the user.
SELECT * FROM user WHERE username = ?;

--- Location: UserService.registerUser()
--- Webpage: http://localhost:8081/register
--- Description: Adds a new user account.
INSERT INTO user (username, password, firstName, lastName) VALUES (?, ?, ?, ?);

--- Location: UserService.getUser()
--- Webpage: User details used by posts, comments, and profile pages
--- Description: Gets a user by their userId.
SELECT * FROM user WHERE userId = ?;





--- inserting users into the database 
--- webpage: http://localhost:8081/register 
--- Description: Not used. An example of what new users would look like
insert into user (username, password, firstName, lastName) values ("mmm05", "rando543", "Mia", "Martinez Martinez")
insert into user (username, password, firstName, lastName) values ("mellybelly", "twin1isgreat" , "Melanie", "Martinez")
insert into user (username, password, firstName, lastName) values ("booby07", "foodlover", "Bob", "Smith")
insert into user (username, password, firstName, lastName) values ("chayo76", "lovemom", "Maria", "Lopez")
insert into user (username, password, firstName, lastName) values ("juanwill", "trucklover" , "Juan", "Williams")