package com.unzer.util;

import com.unzer.constants.TransactionType;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import oracle.jdbc.pool.OracleDataSource;
import org.apache.commons.lang3.StringUtils;
import org.awaitility.core.Predicate;

import java.sql.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@Slf4j
public class DatabaseHelper {
    private final static Configuration config = Configuration.INSTANCE;
    private static Connection conn;

    static {
        if (conn == null) {
            try {
                OracleDataSource ods = new OracleDataSource();
                ods.setDriverType("thin");
                ods.setServerName(config.getProperty("db.host"));
                ods.setPortNumber(Integer.valueOf(config.getProperty("db.port")));
                ods.setServiceName(config.getProperty("db.sid"));
                ods.setUser(config.getProperty("db.user"));
                ods.setPassword(config.getProperty("db.password"));
                conn = ods.getConnection();
                log.info("Database connection successful");
            } catch (SQLException sqlException) {
                log.error("problems while connecting to database");
            }
        }
    }

    @SneakyThrows
    public static String getTransactionStatus(String shortId) {
        String query = "Select ID_TXN_STATUS from HPC.HPC_TXNS where STR_SHORT_ID = '"+shortId+"'";
        return Eventually.get(() -> executeAndGetResult(query));
    }

    @SneakyThrows
    public static String getTransactionResult(String shortId) {
        String query = "Select ID_RESULT from HPC.HPC_TXNS where STR_SHORT_ID = '"+shortId+"'";
        return Eventually.get(() -> executeAndGetResult(query));
    }

    @SneakyThrows
    public static String getInitiation(String shortId) {
        String databaseId = getDatabaseId(shortId);
        String query = "Select STR_INITIATION from HPC.HPC_TXN_COFS where ID_TXN = '"+databaseId+"'";
        return Eventually.get(() -> executeAndGetResult(query));
    }

    @SneakyThrows
    public static String getInitialSubsequent(String shortId) {
        String databaseId = getDatabaseId(shortId);
        String query = "Select STR_INITIAL_SUBSEQUENT from HPC.HPC_TXN_COFS where ID_TXN = '"+databaseId+"'";
        return Eventually.get(() -> executeAndGetResult(query));
    }

    @SneakyThrows
    public static boolean isScheduled(String shortId) {
        String databaseId = getDatabaseId(shortId);
        String query = "Select STR_SCHEDULED from HPC.HPC_TXN_COFS where ID_TXN = '"+databaseId+"'";
        return Eventually.get(() -> executeAndGetResult(query)).equals("SCHEDULED");
    }

    @SneakyThrows
    public static String getGiccMessage(String shortId) {
        String databaseId = getDatabaseId(shortId);
        String query = "Select STR_LOG from HPC.HPC_TXN_HISTORY where id_txn = '"+databaseId+"' and STR_LOG like '%isomsg%'";
        String isoMessage = executeAndGetResult(query);
        if (isoMessage.isEmpty()) log.warn("no isoMessage found for short id {}", shortId);
        return isoMessage;
    }

    @SneakyThrows
    public static String getDatabaseId(String shortId) {
        String query = "select id from HPC.HPC_TXNS where STR_SHORT_ID = '"+shortId+"'";
        String id = executeAndGetResult(query);
        if (id.isEmpty()) log.error("no record found for short id {}", shortId);
        return id;

    }

    @SneakyThrows
    public static String getEci(String shortId) {
        String databaseId = getDatabaseId(shortId);
        String query = "Select STR_ECI from HPC.HPC_TXNS_3DSEC where id ='"+databaseId+"'";
        String eci = executeAndGetResult(query);
        if (eci.isEmpty()) log.warn("no eci found for short id {}", shortId);
        return eci;
    }

    @SneakyThrows
    public static String getCavv(String shortId) {
        String databaseId = getDatabaseId(shortId);
        String query = "Select STR_VERIFICATION_ID from HPC.HPC_TXNS_3DSEC where id ='"+databaseId+"'";
        String cavv = executeAndGetResult(query);
        if (cavv.isEmpty()) log.warn("no cavv found for short id {}", shortId);
        return cavv;
    }

    @SneakyThrows
    public static String getDsTransId(String shortId) {
        String databaseId = getDatabaseId(shortId);
        String query = "Select STR_DS_TRANSACTION_ID from HPC.HPC_TXNS_3DSEC where id ='"+databaseId+"'";
        String dsTransId = executeAndGetResult(query);
        if (dsTransId.isEmpty()) log.warn("no dsTransId found for short id {}", shortId);
        return dsTransId;
    }

    @SneakyThrows
    public static String getScheduledTransactionShortId(String shortId) {
        String query = "Select ID_ROOT_TXN from HPC.HPC_TXNS where STR_SHORT_ID = '"+shortId+"'";
        String rootId = executeAndGetResult(query);
        String query2 = "Select STR_SHORT_ID from HPC.HPC_TXNS where ID_ROOT_TXN = '"+rootId+"' and ID_TXN_SOURCE_TYPE = 'SCH'";
        return Eventually.get(() -> executeAndGetResult(query2), 120, 10);
    }

    public static String getTransactionType(String shortId) {
        String query = "Select ID_TXN_TYPE from HPC.HPC_TXNS where STR_SHORT_ID = '"+shortId+"'";
        return Eventually.get(() -> executeAndGetResult(query));
    }

    @SneakyThrows
    private static String executeAndGetResult(String query) {
        Statement statement = conn.createStatement();
        ResultSet rs = statement.executeQuery(query);
        int numberOfRecords = 0;

        String output = StringUtils.EMPTY;
        while(rs.next()){
            output  = rs.getString(1);
            numberOfRecords ++;
        }

        log.info("found {} records for the query {}", numberOfRecords, query);
        log.info("returning the column value from last record. value >> {}", output);
        return output;
    }

    @SneakyThrows
    private ResultSet execute(String query) {
        Statement statement = conn.createStatement();
        ResultSet rs = statement.executeQuery(query);
        return rs;
    }

    @SneakyThrows
    public static void closeConnection() {
        if (conn != null && !conn.isClosed())
            conn.close();
        conn = null;
        log.info("Closed the database connection and set conn to null");
    }
}
