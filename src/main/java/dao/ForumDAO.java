package dao;

import controllers.CustomResponse;

import java.util.List;

/**
 * Created by parallels on 3/20/16.
 */
public interface ForumDAO {
    void truncateTable();

    int count();

    CustomResponse create(String forumString);

    CustomResponse details(String forumShortName, final List<String> related);

    CustomResponse listPosts(String forumShortName,
                             final List<String> related,
                             String since,
                             String limit,
                             String order);

    CustomResponse listThreads(String forumShortName,
                               final List<String> related,
                               String since,
                               String limit,
                               String order);

    @SuppressWarnings("MethodParameterNamingConvention")
    CustomResponse listUsers(String forumShortName, String since_id, String limit, String order);
}
