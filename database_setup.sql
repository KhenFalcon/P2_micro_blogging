-- Create the database.
create database if not exists csx370_mb_platform;

-- Use the created database.
use csx370_mb_platform;

-- Create the user table.
create table if not exists user (
    userId int auto_increment,
    username varchar(255) not null,
    password varchar(255) not null,
    firstName varchar(255) not null,
    lastName varchar(255) not null,
    primary key (userId),
    unique (username),
    constraint userName_min_length check (char_length(trim(userName)) >= 2),
    constraint firstName_min_length check (char_length(trim(firstName)) >= 2),
    constraint lastName_min_length check (char_length(trim(lastName)) >= 2)
);

create table if not exists basicPost (
    postId int,
    content varchar(255) not null,
    postDate datetime not null,
    userId int,
    primary key (postID), 
    foreign key (userId) references user(userId)
);
-- can get userId from user object - double check this

create table if not exists post(
    heartsCount int,
    commentsCount int,
    isHearted boolean,
    isBookmarked boolean, 
    postId int,
    primary key (postId),
    foreign key (postId) references basicPost(postId)
);

create table if not exists expandedPost(
    postId int, 
    primary key (postId),
    foreign key (postId) references post(postId)
);

create table if not exists comments(
    commentId int,
    postId int, 
    content varchar(255) not null,
    postDate datetime not null,
    userId int not null,
    primary key (commentId),
    foreign key (postId) references basicPost(postId),
    foreign key (userId) references user(userId)
);
