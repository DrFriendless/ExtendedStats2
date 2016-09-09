select geek,bggid,country from users where country = 'Australia' into outfile '/tmp/data/users.csv'
FIELDS TERMINATED BY ',' OPTIONALLY ENCLOSED BY '"' LINES TERMINATED BY '\n';
select geek,game,rating,owned,want,wish,trade,comment,prevowned,wanttobuy,wanttoplay,preordered from geekgames where geek in (select geek from users where country = 'Australia') into outfile '/tmp/data/geekgames.csv'
FIELDS TERMINATED BY ',' OPTIONALLY ENCLOSED BY '"' LINES TERMINATED BY '\n';
select game,geek,playDate,quantity,basegame,raters,ratingsTotal,location from plays where geek in (select geek from users where country = 'Australia') into outfile '/tmp/data/plays.csv'
FIELDS TERMINATED BY ',' OPTIONALLY ENCLOSED BY '"' LINES TERMINATED BY '\n';
select basegame,expansion from expansions into outfile '/tmp/data/expansions.csv'
FIELDS TERMINATED BY ',' OPTIONALLY ENCLOSED BY '"' LINES TERMINATED BY '\n';
select bggid,name,minPlayers,maxPlayers from games into outfile '/tmp/data/games.csv'
FIELDS TERMINATED BY ',' OPTIONALLY ENCLOSED BY '"' LINES TERMINATED BY '\n';
