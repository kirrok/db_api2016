package executor;

import handlers.TResultHandler;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

/**
 * Created by a.serebrennikova
 */
public class TExecutor {
    public static <T> T execQuery(Connection connection, String query, TResultHandler<T> handler)
            throws SQLException {
        Statement stmt = connection.createStatement();
        stmt.execute(query);
        ResultSet result = stmt.getResultSet();
        T value = handler.handle(result);
        result.close();
        stmt.close();

        return value;
    }

    public static void execQuery(Connection connection, String query) throws SQLException{
        Statement stmt = connection.createStatement();
        stmt.execute(query);
        stmt.close();
    }



}