--- inserting users into the database 
--- webpage: http://localhost:8081/register 
insert into user (username, password, firstName, lastName) values ("mmm05", "rando543", "Mia", "Martinez Martinez")
insert into user (username, password, firstName, lastName) values ("mellybelly", "twin1isgreat" , "Melanie", "Martinez")
insert into user (username, password, firstName, lastName) values ("booby07", "foodlover", "Bob", "Smith")
insert into user (username, password, firstName, lastName) values ("chayo76", "lovemom", "Maria", "Lopez")
insert into user (username, password, firstName, lastName) values ("juanwill", "trucklover" , "Juan", "Williams")

--- inserting posts created from users into posts table.
--- webpage: http://localhost:8081/
insert into posts (content, postDate, userId) values (?, ?, ?)

--- inserting comments made under posts into comment table.
insert into comment (postId, content, postDate, userId) values (?, ?, ?, ?)
--- updating the commentsCount variable (incrementing by 1) when comments are added under specifc post.
update posts set commentsCount = commentsCount + 1 where postId = ?
--- webpage: http://localhost:8081/post/5

--- selecting all information of Post as well as User information (firstName and lastName) from specifc post
select P.postId, P.content, P.postDate, P.userId, P.heartsCount, P.commentsCount, P.isHearted, P.isBookmarked, U.firstName, U.lastName from posts P, user U where P.userId = U.userId and P.postId = ?
--- selecting all comments (if any) made on specifc post
--- orders comments from oldest to latest
select C.postID, C.content, C.postDate, C.userId, U.firstName, U.lastName from comment C, user U  where C.userId = U.userId and C.postId = ? order by c.postDate ASC
-- done in conjuction to create ExpandedPost instance of a post where comments are visible
--- webpage: http://localhost:8081/post/5 

--- updating values (specifically isHearted and heartsCount) in posts table if user wants to heart a specifc post
--- isHearted is updated to true 
--- heartsCount is incremented by 1 
update posts set isHearted = true, heartsCount = heartsCount + 1 where postId = ?

--- updating values (specifcally isHearted and heartsCount) in posts if user wants to remove a heart from specifc post
--- isHearted is updated to false
--- heartsCount is decreased by 1
update posts set isHearted = false, heartsCount = heartsCount - 1 where postId = ?
--- webpage: http://localhost:8081/post/5 or http://localhost:8081/ 

--- updating values (specifically isBookmared) in posts table if a user wants to bookmark a specific post
--- isBookmarked is updated to true
update posts set isBookmarked = true where postId = ?

--- updating values (specifically isBookmarked) in posts table if a user wants to remove a bookmark from specific post
--- isBookmarked is updated to false
update posts set isBookmarked = false where postId = ?
--- webpage: http://localhost:8081/post/5 or http://localhost:8081/ 

--- selects all post information as well as user information on posts created by users that current user follows
--- sorts these posts in descending order -> most recent to oldest
--- necessary for the homepage as it shows post that current user follows 
select P.postId, P.content, P.postDate, P.userId, P.heartsCount, P.commentsCount, P.isHearted, P.isBookmarked, U.firstName, U.lastName from posts P, followStatus FS, user U where P.userId = U.userId and P.userId = FS.followeeId and FS.followerId = ? order by postDate DESC
--- webpage: http://localhost:8081/ 

--- selects all post information as well as user information on posts created by current logged in user 
--- sorts these posts in descending order -> most recent to oldest 
--- necessary for profile page to see specifc posts from user 
select P.postId, P.content, P.postDate, P.userId, P.heartsCount, P.commentsCount, P.isHearted, P.isBookmarked, U.firstName, U.lastName from posts P, user U where P.userId = U.userId and P.userId = ? order by postDate DESC
--- webpage: http://localhost:8081/profile 

--- selects all the users excluding the current logged in user
--- necessary for the People page 
--- obtains the userId, firstName, and lastName of all users except the current user 
--- obtains the lastActiveDate by determining when was the last time users posted a post 
--- obtains the isFollowed by determining if current user currently follows the user 
select U.userId, U.firstName, U.lastname, (select MAX(P.postDate) from posts P where P.userId = U.userId) as lastActiveDate, (select COUNT(*) from followStatus FS where FS.followerId = ? and FS.followeeId = U.userId) > 0 as isFollowed from user U where userId != ?
--- webpage: http://localhost:8081/people 


