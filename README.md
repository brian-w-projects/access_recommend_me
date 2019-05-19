# Access Recommend Me's public RESTful API

### This project demonstrates how to interact with my Twitter clone's RESTful API. Original site can be found [here](https://rec-me.herokuapp.com). You will need an account in order to make queries.

<pre>
Usage: Query rec-me.herokuapp.com's RESTful API

 -f,--file <file>     Read queries from specified file. (optional)

 -h,--help            Display this information

 -l,--login <login>   Username:Password (required)

 -w,--write <write>   Print data to specified file. (optional)

Make queries either form input file or from console as follows [translation in brackets]:

GET recs 10 			[get rec with id number 10]

GET recs 10,12,13,14 		[get rec with id number 10 12 13 and 14]

GET recs 10 comments 		[get page 1 of comments from rec 10]

GET recs 10 comments 2 		[get page 2 of comments from rec 10]

GET recs 10 comments 2,3,4 	[get page 2 3 and 4 of comments from rec 10]

GET comments 12 		[get comment with id number 20]

GET comments 10,11,12 		[get comments with id numbers 10 11 and 12]

GET users 2 			[get information on user with id number 2]

GET users 2,3,4 		[get information on user with id numbers 2 3 and 4]

GET users 2 recs 		[get page 1 of recs from user with id number 2]

GET users 2 recs 1,2,3 		[get page 1 2 and 3 of recs from user with id number 2]

GET users 2 comments 1,2,3 	[get page 1 2 and 3 of comments from user with id number 2]

GET users 2 following 		[get following info for user with id number 2]

GET users 2 followed_by 	[get followed_by info for user with id number 2]

GET users 2,3,4 recs		[get page 1 of recs from users 2 3 and 4]

GET users 2,3,4 comments	[get page 1 of comments from users 2 3 and 4]

GET users 2,3,4 following	[get following info for users 2 3 and 4]

GET users 2,3,4 followed_by 	[get followed_by info for users 2 3 and 4]

GET search recs -p user="sara" term="jkl" date="05/20/2017" [get page 1 of recs that match search terms]

GET search comments 2,3,4 -p user="sara" term="jkl" date="05/20/2017" [get pages 2 3 and 4 of comments that match search terms]

POST recs -p title="hello there how are you" text="so much stuff" public="True" [post a new rec]

PUT recs 10 -p title="hello there how are you" text="so much stuff" public="True" [put existing rec with id number 10]

DELETE recs 10 [delete rec with id number 10]

POST comments 15 -p text="what i'm writing" [post comment on rec with id number 15]

PUT comments 10	-p text="example" [put comment with id number 10]

DELETE comments 10 [delete comment with id number 10]

PUT users -p about_me="all about me" display="10" updates="False" [modify user's about me page]

POST follow 10 [follow/unfollow user with id number 10]
</pre>
