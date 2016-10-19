package dataSets;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by parallels on 3/20/16.
 */
@SuppressWarnings({"unused", "InstanceMethodNamingConvention", "InstanceVariableNamingConvention"})
public class ForumDataSet {
    private int id;

    private String name;

    private String short_name;

    private Object user;

    @SuppressWarnings("MethodParameterNamingConvention")
    public ForumDataSet(int id, String name, String short_name, Object user) {
        this.id = id;
        this.name = name;
        this.short_name = short_name;
        this.user = user;
    }

    @SuppressWarnings("MethodParameterNamingConvention")
    public ForumDataSet(String name, String short_name, Object user) {
        this(-1, name, short_name, user);
    }

    public ForumDataSet(ResultSet resultSet) throws SQLException {
        this(
                resultSet.getInt("id"),
                resultSet.getString("name"),
                resultSet.getString("short_name"),
                resultSet.getObject("user")
        );
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getShort_name() {
        return short_name;
    }

    @SuppressWarnings("MethodParameterNamingConvention")
    public void setShort_name(String short_name) {
        this.short_name = short_name;
    }

    public Object getUser() {
        return user;
    }

    public void setUser(Object user) {
        this.user = user;
    }
}
