package dao;

import controllers.CustomResponse;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by parallels on 3/20/16.
 */
public interface ForumDAO {
    void truncateTable();

    int count();

    CustomResponse create(String forumString);

    CustomResponse details(String forumShortName, final List<String> related);
}
