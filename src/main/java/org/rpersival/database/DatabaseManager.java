package org.rpersival.database;

import java.sql.*;

public class DatabaseManager {
    private static final String JDBC = "jdbc:h2:./src/main/resources/data/comrades;DB_CLOSE_DELAY=-1";

    private static final String USERS_TABLE = "users";
    private static final String ID_COLUMN = "id";
    private static final String STATUS_COLUMN = "user_status";
    private static final String DESCRIPTION_COLUMN = "description";
    private static final String LINK_COLUMN = "link";
    private static final String RECEIVER_COLUMN = "receiver";

    private static final Connection connection;

    static {
        try {
            connection = DriverManager.getConnection(JDBC, "sa", "");
            createTables();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("An error occurred while connecting to the database.");
        }
    }

    public static void closeConnection() {
        System.out.println("Closing connection...");
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("An error occurred while disconnecting from database.");
        }
    }

    public static void createTables() throws SQLException {
        String createTableSQL = "CREATE TABLE IF NOT EXISTS " + USERS_TABLE + " (" +
                ID_COLUMN + " BIGINT PRIMARY KEY, " +
                STATUS_COLUMN + " VARCHAR(31), " +
                DESCRIPTION_COLUMN + " TEXT, " +
                LINK_COLUMN + " VARCHAR(31), " +
                RECEIVER_COLUMN + " BIGINT REFERENCES " + USERS_TABLE + " (" + ID_COLUMN + "));";

        try (PreparedStatement preparedStatement = connection.prepareStatement(createTableSQL)) {
            preparedStatement.execute();
        }
    }

    public static void addUser(long userId) throws SQLException {
        String insertSQL = "INSERT INTO " + USERS_TABLE + " (" + ID_COLUMN + ", " + STATUS_COLUMN + ") VALUES (?, ?)";
        try (PreparedStatement preparedStatement = connection.prepareStatement(insertSQL)) {
            preparedStatement.setLong(1, userId);
            preparedStatement.setString(2, UserStatus.EMPTY.toString());
            preparedStatement.executeUpdate();
        }
    }

    public static void setDescription(long userId, String description) throws SQLException {
        updateTarget(userId, DESCRIPTION_COLUMN, description);
    }

    public static void setLink(long userId, String link) throws SQLException {
        updateTarget(userId, LINK_COLUMN, link);
    }

    public static void setReceiver(long userId, long receiverId) throws SQLException {
        updateTarget(userId, RECEIVER_COLUMN, receiverId);
    }

    public static void setStatus(long userId, UserStatus newStatus) throws SQLException {
        updateTarget(userId, STATUS_COLUMN, newStatus.name());
    }

    public static String getDescription(long userId) throws SQLException {
        return selectTarget(userId, DESCRIPTION_COLUMN, String.class);
    }

    public static Long getReceiver(long userId) throws SQLException {
        return selectTarget(userId, RECEIVER_COLUMN, Long.class);
    }

    public static String getLink(long userId) throws SQLException {
        return selectTarget(userId, LINK_COLUMN, String.class);
    }

    public static UserStatus getStatus(long userId) throws SQLException {
        String status = selectTarget(userId, STATUS_COLUMN, String.class);
        if (status != null) {
            return UserStatus.valueOf(status);
        }

        return UserStatus.EMPTY;
    }

    public static boolean hasDescription(long userId) throws SQLException {
        return DatabaseManager.getDescription(userId) != null;
    }

    public static boolean hasLink(long userId) throws SQLException {
        return DatabaseManager.getLink(userId) != null;
    }

    public static boolean exists(long userId) throws SQLException {
        String selectSQL = "SELECT 1 FROM " + USERS_TABLE + " WHERE " + ID_COLUMN + " = ? LIMIT 1";
        try (PreparedStatement preparedStatement = connection.prepareStatement(selectSQL)) {
            preparedStatement.setLong(1, userId);
            return preparedStatement.executeQuery().next();
        }
    }

    private static <T> void updateTarget(long userId, String target, T object) throws SQLException {
        String updateSQL = "UPDATE " + USERS_TABLE + " SET " + target + " = ? WHERE " + ID_COLUMN + " = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(updateSQL)) {
            preparedStatement.setObject(1, object);
            preparedStatement.setLong(2, userId);
            preparedStatement.executeUpdate();
        }
    }

    private static <T> T selectTarget(long userId, String target, Class<T> clazz) throws SQLException {
        String selectSQL = "SELECT " + target + " FROM " + USERS_TABLE + " WHERE " + ID_COLUMN + " = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(selectSQL)) {
            preparedStatement.setLong(1, userId);
            ResultSet resultSet = preparedStatement.executeQuery();
            return resultSet.next() ? resultSet.getObject(target, clazz) : null;
        }
    }
}
