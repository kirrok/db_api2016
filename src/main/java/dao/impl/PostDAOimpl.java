package dao.impl;

import controllers.CustomResponse;
import dao.PostDAO;
import dataSets.PostDataSet;
import executor.TExecutor;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by parallels on 3/20/16.
 */
public class PostDAOimpl implements PostDAO{
    Connection connection;
    ObjectMapper mapper;

    public PostDAOimpl(Connection connection) {
        this.connection = connection;
        mapper = new ObjectMapper();
    }

    public void truncateTable() {
        try {
            TExecutor.execQuery(connection, "SET FOREIGN_KEY_CHECKS = 0;");
            TExecutor.execQuery(connection, "TRUNCATE TABLE post;");
            TExecutor.execQuery(connection, "SET FOREIGN_KEY_CHECKS = 1;");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public int count() {
        try {
            int count = TExecutor.execQuery(connection, "SELECT COUNT(*) FROM post WHERE isDeleted=0;", resultSet -> {
                resultSet.next();
                return resultSet.getInt(1);
            });
            return count;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public CustomResponse create(String postString) {
        CustomResponse response = new CustomResponse();
        PostDataSet post;

        try {
            JsonNode json = mapper.readValue(postString, JsonNode.class);
            if (!json.has("date") || !json.has("thread") || !json.has("message")
                    || !json.has("user") || !json.has("forum")) {
                response.setResponse("INCORRECT REQUEST");
                response.setCode(CustomResponse.INCORRECT_REQUEST);
                return response;
            }
            try {
                post = new PostDataSet(json);

            } catch (Exception e) {
                response.setResponse("INCORRECT REQUEST");
                response.setCode(CustomResponse.INCORRECT_REQUEST);
                return response;
            }
        } catch (IOException e) {
            response.setResponse("INVALID REQUEST");
            response.setCode(CustomResponse.INVALID_REQUEST);
            return response;
        }

        try {
            System.out.println(post.getMessage());
            String query = "INSERT INTO post (date, thread, message, user, forum, parent, isApproved, isHighlighted, isEdited, isSpam, isDeleted) VALUES(?,?,?,?,?,?,?,?,?,?,?);";
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setString(1, post.getDate());
            stmt.setInt(2, (Integer)post.getThread());
            stmt.setString(3, post.getMessage());
            stmt.setString(4, (String)post.getUser());
            stmt.setString(5, (String)post.getForum());
            stmt.setObject(6, post.getParent());
            stmt.setBoolean(7, post.getIsApproved());
            stmt.setBoolean(8, post.getIsHighlighted());
            stmt.setBoolean(9, post.getIsEdited());
            stmt.setBoolean(10, post.getIsSpam());
            stmt.setBoolean(11, post.getIsDeleted());
            stmt.execute();
            ResultSet generatedKeys = stmt.getGeneratedKeys();
            if (generatedKeys.next())
                post.setId(generatedKeys.getInt(1));
            stmt.close();

            response.setResponse(post);
            response.setCode(CustomResponse.OK);
            return response;
        } catch (SQLException e) {
            if (e.getErrorCode() == 1062) {
                response.setCode(CustomResponse.ALREADY_EXIST);
                return response;
            } else {
                response.setResponse("UNKNOWN ERROR");
                response.setCode(CustomResponse.UNKNOWN_ERROR);
                return response;
            }
        }
    }

    public CustomResponse details(String postId, final List<String> related) {
        CustomResponse response = new CustomResponse();

        if (postId == null) {
            response.setResponse("INCORRECT REQUEST");
            response.setCode(CustomResponse.INCORRECT_REQUEST);
            return response;
        }

        for (String str : related) {
            if (!str.equals("user") && !str.equals("forum") & !str.equals("thread")) {
                response.setResponse("INCORRECT REQUEST");
                response.setCode(CustomResponse.INCORRECT_REQUEST);
                return response;
            }
        }

        PostDataSet post;
        try {
            String query = "SELECT * FROM post WHERE id=?";
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setString(1, postId);
            ResultSet resultSet = stmt.executeQuery();
            if (resultSet.next()) {
                post = new PostDataSet(resultSet);
            } else {
                response.setResponse("NOT FOUND");
                response.setCode(CustomResponse.NOT_FOUND);
                return response;
            }
            if (related.contains("user")) {
                post.setUser(new UserDAOimpl(connection).details((String)post.getUser()).getResponse());
            }
            if (related.contains("thread")) {
                post.setThread(new ThreadDAOimpl(connection).details(post.getThread().toString(), new ArrayList<>()).getResponse());
            }
            if (related.contains("forum")) {
                post.setForum(new ForumDAOimpl(connection).details((String)post.getForum(), new ArrayList<>()).getResponse());
            }
            stmt.close();

            response.setResponse(post);
            response.setCode(CustomResponse.OK);
            return response;
        } catch (SQLException e) {
            response.setResponse("UNKNOWN ERROR");
            response.setCode(CustomResponse.UNKNOWN_ERROR);
            return response;
        }
    }
}
