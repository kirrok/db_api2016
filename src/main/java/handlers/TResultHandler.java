package handlers;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by a.serebrennikova
 */
public interface TResultHandler<T> {
    T handle(ResultSet resultSet) throws SQLException;
}