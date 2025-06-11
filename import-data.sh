# This script was intended to get raw IMDB datasets into Postgres
# The script worked on my mac setup.
#
# You may have to update your psql command with appropriate -U and -d flags and may have to
# provide appropriate permissions to new folders.
#
# Customise as you see fit and for your setup.
#
# Tables are NOT optomised, nor have any indexes been created
# The point is to "just get the data into Postgres"
#
# Remember to allow execution rights before trying to run it:
# chmod 755 imdb_postgres_setup.sh
#
# Just for interest's sake, below are the terminal commands for my Linux box:
# su - postgres
# curl -O https://gist.githubusercontent.com/IllusiveMilkman/2a7a6614193c74804db7650f6d3c2bd2/raw/c8c0b4dbac00cf7539dd5cc9670fe00b38430f7d/imdb_postgres_setup.sh
# chmod 755 imdb_postgres_setup.sh
# ./imdb_postgres_setup.sh
#
# If you don't know the password for postgres (new install, some default VM setups, etc)
# sudo passwd postgres
# set a new password, then continue above
#

printf "Script starting at %s. \n" "$(date)"

#printf "Removing old folders \n"
#rm -rf imdb-datasets/

#printf "Creating new folders \n"
#mkdir imdb-datasets/

printf "Downloading datasets from https://datasets.imdbws.com \n"
#cd imdb-datasets
#curl -O https://datasets.imdbws.com/name.basics.tsv.gz
#curl -O https://datasets.imdbws.com/title.akas.tsv.gz
#curl -O https://datasets.imdbws.com/title.basics.tsv.gz
#curl -O https://datasets.imdbws.com/title.crew.tsv.gz
#curl -O https://datasets.imdbws.com/title.episode.tsv.gz
#curl -O https://datasets.imdbws.com/title.principals.tsv.gz
#curl -O https://datasets.imdbws.com/title.ratings.tsv.gz
#
#printf "Unzipping datasets... \n"
#gzip -dk *.gz
#cd ..

#printf "Creating Database \n"
#psql -d 'postgres' -c "DROP DATABASE IF EXISTS movies;"
#psql -d 'postgres' -c "CREATE DATABASE movies;"

printf "Creating tables in imdb database \n"
PGPASSWORD="movies" psql -h localhost -p 5435 -U movies -d movies  -c "CREATE table title_ratings (tconst VARCHAR(10),average_rating NUMERIC,num_votes integer);"
PGPASSWORD="movies" psql -h localhost -p 5435 -U movies -d movies  -c "CREATE TABLE name_basics (nconst varchar(10), \"primaryName\" text, \"birthYear\" smallint, \"deathYear\" smallint, \"primaryProfession\" text[], \"knownForTitles\" text[] );"
PGPASSWORD="movies" psql -h localhost -p 5435 -U movies -d movies  -c "CREATE TABLE name_basics_tmp (nconst varchar(10), \"primaryName\" text, \"birthYear\" smallint, \"deathYear\" smallint, \"primaryProfession\" text, \"knownForTitles\" text );"
PGPASSWORD="movies" psql -h localhost -p 5435 -U movies -d movies  -c "CREATE TABLE title_akas (\"titleId\" TEXT, ordering INTEGER, title TEXT, region TEXT, language TEXT, types TEXT, attributes TEXT, \"isOriginalTitle\" BOOLEAN);"
PGPASSWORD="movies" psql -h localhost -p 5435 -U movies -d movies  -c "CREATE TABLE title_basics_tmp (tconst TEXT, \"titleType\" TEXT, \"primaryTitle\" TEXT, \"originalTitle\" TEXT, \"isAdult\" BOOLEAN, \"startYear\" SMALLINT, \"endYear\" SMALLINT, \"runtimeMinutes\" INTEGER, \"genres\" TEXT);"
PGPASSWORD="movies" psql -h localhost -p 5435 -U movies -d movies  -c "CREATE TABLE title_basics (tconst TEXT, \"titleType\" TEXT, \"primaryTitle\" TEXT, \"originalTitle\" TEXT, \"isAdult\" BOOLEAN, \"startYear\" SMALLINT, \"endYear\" SMALLINT, \"runtimeMinutes\" INTEGER, \"genres\" TEXT[]);"
PGPASSWORD="movies" psql -h localhost -p 5435 -U movies -d movies  -c "CREATE TABLE title_crew (tconst TEXT, directors TEXT, writers TEXT);"
PGPASSWORD="movies" psql -h localhost -p 5435 -U movies -d movies  -c "CREATE TABLE title_episode (const TEXT, \"parentTconst\" TEXT, \"seasonNumber\" TEXT, \"episodeNumber\" TEXT);"
PGPASSWORD="movies" psql -h localhost -p 5435 -U movies -d movies  -c "CREATE TABLE title_principals (tconst TEXT, ordering INTEGER, nconst TEXT, category TEXT, job TEXT, characters TEXT[]);"
PGPASSWORD="movies" psql -h localhost -p 5435 -U movies -d movies  -c "CREATE TABLE title_principals_tmp (tconst TEXT, ordering INTEGER, nconst TEXT, category TEXT, job TEXT, characters TEXT);"

printf "Inserting data into tables \n"
PGPASSWORD="movies" psql -h localhost -p 5435 -U movies -d movies -c "COPY title_ratings FROM '/home/pg/title.ratings.tsv' DELIMITER E'\t' QUOTE E'\b' NULL '\N' CSV HEADER"
PGPASSWORD="movies" psql -h localhost -p 5435 -U movies -d movies -c "COPY name_basics_tmp FROM '/home/pg/name.basics.tsv' DELIMITER E'\t' QUOTE E'\b' NULL '\N' CSV HEADER"
PGPASSWORD="movies" psql -h localhost -p 5435 -U movies -d movies -c "COPY title_akas FROM '/home/pg/title.akas.tsv' DELIMITER E'\t' QUOTE E'\b' NULL '\N' CSV HEADER"
PGPASSWORD="movies" psql -h localhost -p 5435 -U movies -d movies -c "COPY title_basics_tmp FROM '/home/pg/title.basics.tsv' DELIMITER E'\t' QUOTE E'\b' NULL '\N' CSV HEADER"
PGPASSWORD="movies" psql -h localhost -p 5435 -U movies -d movies -c "COPY title_crew FROM '/home/pg/title.crew.tsv' DELIMITER E'\t' QUOTE E'\b' NULL '\N' CSV HEADER"
PGPASSWORD="movies" psql -h localhost -p 5435 -U movies -d movies -c "COPY title_episode FROM '/home/pg/title.episode.tsv' DELIMITER E'\t' QUOTE E'\b' NULL '\N' CSV HEADER"
PGPASSWORD="movies" psql -h localhost -p 5435 -U movies -d movies -c "COPY title_principals_tmp FROM '/home/pg/title.principals.tsv' DELIMITER E'\t' QUOTE E'\b' NULL '\N' CSV HEADER"

PGPASSWORD="movies" psql -h localhost -p 5435 -U movies -d movies -c "insert into title_basics(tconst, \"titleType\", \"primaryTitle\", \"originalTitle\", \"isAdult\", \"startYear\", \"endYear\", \"runtimeMinutes\", genres) select  tconst, \"titleType\", \"primaryTitle\", \"originalTitle\", \"isAdult\", \"startYear\", \"endYear\", \"runtimeMinutes\", STRING_TO_ARRAY(genres, ',') from title_basics_tmp;"
PGPASSWORD="movies" psql -h localhost -p 5435 -U movies -d movies -c "insert into name_basics(nconst, \"primaryName\", \"birthYear\", \"deathYear\", \"primaryProfession\", \"knownForTitles\") select nconst, \"primaryName\", \"birthYear\", \"deathYear\", STRING_TO_ARRAY(\"primaryProfession\", ','), STRING_TO_ARRAY(\"knownForTitles\", ',') from name_basics_tmp;"
PGPASSWORD="movies" psql -h localhost -p 5435 -U movies -d movies -c "insert into title_principals(tconst, ordering, nconst, category, job, characters) select tconst, ordering, nconst, category, job, array (SELECT d AS txt_arr FROM   jsonb_array_elements_text(characters::jsonb) AS d where d is not null) from title_principals_tmp;"

PGPASSWORD="movies" psql -h localhost -p 5435 -U movies -d movies  -c "ALTER TABLE title_ratings ADD PRIMARY KEY (tconst);"
PGPASSWORD="movies" psql -h localhost -p 5435 -U movies -d movies  -c "ALTER TABLE name_basics ADD PRIMARY KEY (nconst);"
PGPASSWORD="movies" psql -h localhost -p 5435 -U movies -d movies  -c "ALTER TABLE title_basics ADD PRIMARY KEY (tconst);"
PGPASSWORD="movies" psql -h localhost -p 5435 -U movies -d movies  -c "ALTER TABLE title_crew ADD PRIMARY KEY (tconst);"
PGPASSWORD="movies" psql -h localhost -p 5435 -U movies -d movies  -c "ALTER TABLE title_episode ADD PRIMARY KEY (const);"
PGPASSWORD="movies" psql -h localhost -p 5435 -U movies -d movies  -c "CREATE INDEX title_principals_tconst_idx on title_principals(tconst);"
PGPASSWORD="movies" psql -h localhost -p 5435 -U movies -d movies  -c "CREATE INDEX title_akas_titleid_idx on title_akas(titleId);"
PGPASSWORD="movies" psql -h localhost -p 5435 -U movies -d movies  -c "CREATE INDEX title_episode_parent_tconst_idx on title_episode(\"parentTconst\");"
PGPASSWORD="movies" psql -h localhost -p 5435 -U movies -d movies  -c "CREATE INDEX title_basics_original_title_idx on title_basics(\"originalTitle\");"


printf "Done! "

printf "Script done at %s. \n" "$(date)"