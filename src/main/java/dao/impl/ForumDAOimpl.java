package dao.impl;

import controllers.CustomResponse;
import dao.ForumDAO;
import dataSets.ForumDataSet;
import dataSets.PostDataSet;
import dataSets.ThreadDataSet;
import dataSets.UserDataSet;
import main.Main;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ForumDAOimpl implements ForumDAO{
    private ObjectMapper mapper;

    public ForumDAOimpl() {
        mapper = new ObjectMapper();
        mapper.getJsonFactory().configure(JsonGenerator.Feature.ESCAPE_NON_ASCII, true);
    }

    public void truncateTable() {
        try (Connection connection = Main.connection.getConnection()) {
            Statement stmt = connection.createStatement();
            stmt.execute("SET FOREIGN_KEY_CHECKS = 0;");
            stmt.execute("TRUNCATE TABLE forum;");
            stmt.execute("SET FOREIGN_KEY_CHECKS = 1;");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public int count() {
        try (Connection connection = Main.connection.getConnection()) {
            Statement stmt = connection.createStatement();
            ResultSet resultSet = stmt.executeQuery("SELECT COUNT(*) FROM forum;");
            int count = resultSet.getInt(1);
            return count;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public CustomResponse create(String forumString) {
        try {
            JsonNode json = mapper.readValue(forumString, JsonNode.class);
            try {
                ForumDataSet forum = new ForumDataSet(
                        json.get("name").getTextValue(),
                        json.get("short_name").getTextValue(),
                        json.get("user").getTextValue()
                );

                try (Connection connection = Main.connection.getConnection()) {
                    String query = "INSERT INTO forum (name, short_name, user) VALUES(?,?,?)";
                    PreparedStatement stmt = connection.prepareStatement(query);
                    stmt.setString(1, forum.getName());
                    stmt.setString(2, forum.getShort_name());
                    stmt.setString(3, (String)forum.getUser());
                    stmt.execute();
                    ResultSet generatedKeys = stmt.getGeneratedKeys();
                    if (generatedKeys.next())
                        forum.setId(generatedKeys.getInt(1));

                    return new CustomResponse(forum, CustomResponse.OK);
                } catch (SQLException e) {
                    if (e.getErrorCode() == 1062) {
                        return details(json.get("short_name").getTextValue(), new ArrayList<>());
                    } else {
                        return new CustomResponse("UNKNOWN ERROR", CustomResponse.UNKNOWN_ERROR);
                    }
                }
            } catch (Exception e) {
                return new CustomResponse("INCORRECT REQUEST", CustomResponse.INCORRECT_REQUEST);
            }
        } catch (IOException e) {
            return new CustomResponse("INVALID REQUEST", CustomResponse.INVALID_REQUEST);
        }
    }

    public CustomResponse details(String forumShortName, final List<String> related) {
        if (forumShortName == null || (!related.isEmpty() && !related.get(0).equals("user"))) {
            return new CustomResponse("INCORRECT REQUEST", CustomResponse.INCORRECT_REQUEST);
        }

        ForumDataSet forum;
        try (Connection connection = Main.connection.getConnection()) {
            String query = "SELECT * FROM forum WHERE short_name=?";
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setString(1,forumShortName);
            ResultSet resultSet = stmt.executeQuery();
            if (resultSet.next()) {
                forum = new ForumDataSet(resultSet);
            } else {
                return new CustomResponse("NOT FOUND", CustomResponse.NOT_FOUND);
            }
            if (related.contains("user")) {
                forum.setUser(new UserDAOimpl().details((String)forum.getUser()).getResponse());
            }

            return new CustomResponse(forum, CustomResponse.OK);
        } catch (SQLException e) {
            return new CustomResponse("UNKNOWN ERROR", CustomResponse.UNKNOWN_ERROR);
        }
    }

    public CustomResponse listPosts(String forumShortName,
                                    final List<String> related,
                                    String since,
                                    String limit,
                                    String order) {
        if (forumShortName == null || (order != null && !order.equals("asc") && !order.equals("desc")))
            return new CustomResponse("INCORRECT REQUEST", CustomResponse.INCORRECT_REQUEST);
        for (String str : related) {
            if (!str.equals("forum") && !str.equals("thread") && !str.equals("user"))
                return new CustomResponse("INCORRECT REQUEST", CustomResponse.INCORRECT_REQUEST);
        }
        order = (order == null) ? "desc" : order;

        try (Connection connection = Main.connection.getConnection()) {
            StringBuilder queryBuilder = new StringBuilder();
            queryBuilder.append("SELECT * FROM post WHERE forum=?");
            if (since != null) queryBuilder.append(" AND date >=?");
            queryBuilder.append(" ORDER BY date");
            if (!order.equals("asc")) queryBuilder.append(" DESC");
            if (limit != null) queryBuilder.append(" LIMIT ?");

            PreparedStatement stmt = connection.prepareStatement(queryBuilder.toString());
            stmt.setString(1, forumShortName);
            int stmtParam = 2;
            if (since != null) stmt.setString(stmtParam++, since);
            if (limit != null) stmt.setInt(stmtParam, new Integer(limit));

            List<PostDataSet> posts = new ArrayList<>();
            ResultSet resultSet = stmt.executeQuery();
            while (resultSet.next()) {
                PostDataSet post = new PostDataSet(resultSet);

                if (related.contains("forum"))
                    post.setForum(details(forumShortName, new ArrayList<>()).getResponse());
                if (related.contains("thread")) {
                    Integer thread = (Integer)post.getThread();
                    post.setThread(new ThreadDAOimpl().details(thread.toString(), new ArrayList<>()).getResponse());
                }
                if (related.contains("user")) {
                    String user = (String)post.getUser();
                    post.setUser(new UserDAOimpl().details(user).getResponse());
                }

                posts.add(post);
            }

            return new CustomResponse(posts, CustomResponse.OK);
        } catch (SQLException e){
            return new CustomResponse("UNKNOWN ERROR", CustomResponse.UNKNOWN_ERROR);
        }
    }

    public CustomResponse listThreads(String forumShortName,
                                      final List<String> related,
                                      String since,
                                      String limit,
                                      String order) {
        if (forumShortName == null || (order != null && !order.equals("asc") && !order.equals("desc")))
            return new CustomResponse("INCORRECT REQUEST", CustomResponse.INCORRECT_REQUEST);
        for(String str : related) {
            if (!str.equals("forum") && !str.equals("user"))
                return new CustomResponse("INCORRECT REQUEST", CustomResponse.INCORRECT_REQUEST);
        }
        order = (order == null) ? "desc" : order;

        try (Connection connection = Main.connection.getConnection()) {
            StringBuilder queryBuilder = new StringBuilder();
            queryBuilder.append("SELECT * FROM thread WHERE forum=?");
            if (since != null) queryBuilder.append(" AND date >=?");
            queryBuilder.append(" ORDER BY date");
            if (!order.equals("asc")) queryBuilder.append(" DESC");
            if (limit != null) queryBuilder.append(" LIMIT ?");

            PreparedStatement stmt = connection.prepareStatement(queryBuilder.toString());
            stmt.setString(1, forumShortName);
            int stmtParam = 2;
            if (since != null) stmt.setString(stmtParam++, since);
            if (limit != null) stmt.setInt(stmtParam, new Integer(limit));

            ResultSet resultSet = stmt.executeQuery();
            List<ThreadDataSet> threads = new ArrayList<>();
            while (resultSet.next()) {
                ThreadDataSet thread = new ThreadDataSet(resultSet);

                if (related.contains("forum"))
                    thread.setForum(details(forumShortName, new ArrayList<>()).getResponse());
                if (related.contains("user")) {
                    String user = (String)thread.getUser();
                    thread.setUser(new UserDAOimpl().details(user).getResponse());
                }

                threads.add(thread);
            }

            return new CustomResponse(threads, CustomResponse.OK);
        } catch (SQLException e){
            return new CustomResponse("UNKNOWN ERROR", CustomResponse.UNKNOWN_ERROR);
        }
    }

    public CustomResponse listUsers(String forumShortName, String since_id, String limit, String order) {
        if (forumShortName == null || (order != null && !order.equals("asc") && !order.equals("desc")))
            return new CustomResponse("INCORRECT REQUEST", CustomResponse.INCORRECT_REQUEST);
        order = (order == null) ? "desc" : order;

        try (Connection connection = Main.connection.getConnection()) {
            List<UserDataSet> users = new ArrayList<>();

            StringBuilder queryBuilder = new StringBuilder();
            queryBuilder.append("SELECT u.* FROM user u");
            queryBuilder.append(" JOIN user_forum uf ON u.email=uf.user");
            queryBuilder.append(" WHERE uf.forum = ?");
            if (since_id != null) queryBuilder.append(" AND u.id >= ?");
            queryBuilder.append(" ORDER BY u.name");
            if (order.equals("desc")) queryBuilder.append(" DESC");
            if (limit != null) queryBuilder.append(" LIMIT ?");

            PreparedStatement stmt = connection.prepareStatement(queryBuilder.toString());
            stmt.setString(1, forumShortName);
            int param = 2;
            if (since_id != null) stmt.setString(param++, since_id);
            if (limit != null) stmt.setInt(param, new Integer(limit));

            ResultSet resultSet = stmt.executeQuery();
            while (resultSet.next()) {
                UserDataSet user = new UserDataSet(resultSet);
                new UserDAOimpl().setFollowers(connection, user);
                new UserDAOimpl().setFollowing(connection, user);
                new UserDAOimpl().setSubscriptions(connection, user);
                if ( since_id == null || since_id != null && user.getId() >= new Integer(since_id) )
                    users.add(user);
            }

            return new CustomResponse(users, CustomResponse.OK);
        } catch (SQLException e){
            return new CustomResponse("UNKNOWN ERROR", CustomResponse.UNKNOWN_ERROR);
        }
    }
}
