package dao;

import controllers.CustomResponse;

/**
 * Created by parallels on 3/20/16.
 */
public interface UserDAO {
    void truncateTable();

    int count();

    CustomResponse details(String email);

    CustomResponse create(String userString);
}
