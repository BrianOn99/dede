dede, a tool for file deduplication
===================================

This is an assignment of cs course, but it works quite good, I think.
If you upload 2 files with similar content, a lot of space can be saved
(deduplicated).

Remote storage is not implemented yet.

## Usage
```
java -jar program_name upload <min_chunk> <avg_chunk> <max_chunk> <prime d> <local_file_name> <local|remote>
java -jar program_name download <remote_file_name>
java -jar program_name delete <remote_file_name>
```

There is a handy command `java -jar program_name list <local|remote>` to
show what files are stored.

## Build
maven is used for project management. Make sure have maven installed.
mapdb (pure java database) is included as dependency.  It will be
downloaded automatically.

compile: `mvn compile`

run junit tests: `mvn test`

make a jar: `mvn package`

There are some unit test, but the coverage is not very good.  Better
than nothing.
