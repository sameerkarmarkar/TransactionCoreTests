package com.unzer.helpers;

import com.unzer.chef.DataChef;
import com.unzer.util.Configuration;
import com.unzer.util.Eventually;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import net.hpcsoft.adapter.payonxml.ProcessingType;
import net.hpcsoft.adapter.payonxml.StatusType;
import oracle.jdbc.pool.OracleDataSource;
import org.apache.commons.lang3.StringUtils;

import java.sql.*;

import static org.hamcrest.MatcherAssert.assertThat;

@Slf4j
public class DatabaseHelper {
    private final static Configuration config = Configuration.INSTANCE;
    private static Connection conn;

    private static void initialize() {
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
    public static String getTransactionReason(String shortId) {
        String query = "Select ID_REASON from HPC.HPC_TXNS where STR_SHORT_ID = '"+shortId+"'";
        return Eventually.get(() -> executeAndGetResult(query));
    }

    @SneakyThrows
    public static String getTransactionErrrorCode(String shortId) {
        String query = "Select ID_ERROR_CODE from HPC.HPC_TXNS where STR_SHORT_ID = '"+shortId+"'";
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
    public static String getMessageSentToConnector(String shortId) {
        String databaseId = getDatabaseId(shortId);
        String query = "Select STR_LOG from HPC.HPC_TXN_HISTORY where id_txn = '"+databaseId+"' and ID_TXN_STATUS_NEW = '21'";
        String isoMessage = Eventually.get(() -> executeAndGetResult(query), 10, 1);;
        if (isoMessage.isEmpty()) log.warn("no isoMessage found for short id {}", shortId);
        return isoMessage;
    }

    @SneakyThrows
    public static String getGiccResponse(String shortId) {
        String databaseId = getDatabaseId(shortId);
        String query = "Select STR_LOG from HPC.HPC_TXN_HISTORY where id_txn = '"+databaseId+"' and ID_TXN_STATUS_NEW = '22'";
        String isoMessage = Eventually.get(() -> executeAndGetResult(query), 10, 1);;
        if (isoMessage.isEmpty()) log.warn("no isoMessage found for short id {}", shortId);
        return isoMessage;
    }

    @SneakyThrows
    public static String getDatabaseId(String shortId) {
        String query = "select id from HPC.HPC_TXNS where STR_SHORT_ID = '"+shortId+"'";
        String id = Eventually.get(() -> executeAndGetResult(query), 10, 1);
        if (id.isEmpty()) log.error("no record found for short id {}", shortId);
        return id;

    }

    @SneakyThrows
    public static String getEci(String shortId) {
        String databaseId = getDatabaseId(shortId);
        String query = "Select STR_ECI from HPC.HPC_TXNS_3DSEC where id ='"+databaseId+"'";
        String eci = Eventually.get(() -> executeAndGetResult(query), 10, 1);
        if (eci.isEmpty()) log.warn("no eci found for short id {}", shortId);
        return eci;
    }

    @SneakyThrows
    public static String getCavv(String shortId) {
        String databaseId = getDatabaseId(shortId);
        String query = "Select STR_VERIFICATION_ID from HPC.HPC_TXNS_3DSEC where id ='"+databaseId+"'";
        String cavv = Eventually.get(() -> executeAndGetResult(query), 10, 1);
        if (cavv.isEmpty()) log.warn("no cavv found for short id {}", shortId);
        return cavv;
    }

    @SneakyThrows
    public static String getDsTransId(String shortId) {
        String databaseId = getDatabaseId(shortId);
        String query = "Select STR_DS_TRANSACTION_ID from HPC.HPC_TXNS_3DSEC where id ='"+databaseId+"'";
        String dsTransId = Eventually.get(() -> executeAndGetResult(query), 10, 1);
        if (dsTransId.isEmpty()) log.warn("no dsTransId found for short id {}", shortId);
        return dsTransId;
    }

    @SneakyThrows
    public static String getScheduledTransactionShortId(String shortId) {
        String query = "Select ID_ROOT_TXN from HPC.HPC_TXNS where STR_SHORT_ID = '"+shortId+"'";
        String rootId = Eventually.get(() -> executeAndGetResult(query), 10, 1);
        String query2 = "Select STR_SHORT_ID from HPC.HPC_TXNS where ID_ROOT_TXN = '"+rootId+"' and ID_TXN_SOURCE_TYPE = 'SCH'";
        return Eventually.get(() -> executeAndGetResult(query2), 120, 10);
    }

    @SneakyThrows
    public static String getImplicitTransactionUniqueId(String shortId, String transactionType) {
        String query = "Select ID_ROOT_TXN from HPC.HPC_TXNS where STR_SHORT_ID = '"+shortId+"'";
        String rootId = Eventually.get(() -> executeAndGetResult(query), 10, 1);
        String query2 = "Select STR_LONG_ID from HPC.HPC_TXNS where ID_ROOT_TXN = '"+rootId+"' and ID_TXN_TYPE = '"+ transactionType +"'";
        return Eventually.get(() -> executeAndGetResult(query2), 10, 1);
    }

    @SneakyThrows
    public static ProcessingType getTransactionProcessing(String shortId) {
        String result = getTransactionResult(shortId);
        String statusCode = getTransactionStatus(shortId);
        String reasonCode = getTransactionReason(shortId);
        String errorCode = getTransactionErrrorCode(shortId);
        String returnValue = StringUtils.EMPTY;
        String reasonValue = StringUtils.EMPTY;
        String statusValue = executeAndGetResult("Select STR_TXN_STATUS from HPC.HPC_TXN_STATUS where ID = '"+statusCode+"'");
        String query = "Select STR_DESCRIPTION, STR_REASON from HPC.HPC_ERROR_CODES where ID_STATUS = '"+statusCode+"' and ID_REASON='"+reasonCode+"' and ID_ERROR_CODE='"+errorCode+"'";
        ResultSet rs = execute(query);
        while(rs.next()){
            returnValue  = rs.getString(1);
            reasonValue =  rs.getString(2);
        }
        rs.close();

        ProcessingType processingType = new ProcessingType();
        processingType.setResult(result);
        processingType.setCode(statusCode);
        processingType.setStatus(DataChef.statusType(statusCode, statusValue));
        processingType.setReturn(DataChef.returnType(errorCode, returnValue));
        processingType.setReason(DataChef.reasonType(reasonCode, reasonValue));

        return processingType;
    }

    public static String getTransactionType(String shortId) {
        String query = "Select ID_TXN_TYPE from HPC.HPC_TXNS where STR_SHORT_ID = '"+shortId+"'";
        return Eventually.get(() -> executeAndGetResult(query));
    }

    @SneakyThrows
    private static String executeAndGetResult(String query) {
        initialize();
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
        rs.close();
        return output;
    }

    @SneakyThrows
    private static ResultSet execute(String query) {
        initialize();
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
