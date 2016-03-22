package dao;

import controllers.CustomResponse;

import java.util.List;

/**
 * Created by parallels on 3/20/16.
 */
public interface PostDAO {
    void truncateTable();

    int count();

    CustomResponse create(String postString);

    CustomResponse details(String postId, List<String> related);

    CustomResponse list(String forumShortName, String threadId, String since,
                        String limit, String order);

    CustomResponse removeOrRestore(String postString, String action);

    CustomResponse vote(String voteString);
}
