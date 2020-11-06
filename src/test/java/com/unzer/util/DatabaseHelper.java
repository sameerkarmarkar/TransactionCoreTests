package com.unzer.util;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import oracle.jdbc.pool.OracleDataSource;
import org.apache.commons.lang3.StringUtils;

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

    public static DatabaseHelper INSTANCE = new DatabaseHelper();

    @SneakyThrows
    private DatabaseHelper() {
        OracleDataSource ods = new OracleDataSource();
        ods.setDriverType("thin");
        ods.setServerName(config.getProperty("db.host"));
        ods.setPortNumber(Integer.valueOf(config.getProperty("db.port")));
        ods.setServiceName(config.getProperty("db.sid"));
        ods.setUser(config.getProperty("db.user"));
        ods.setPassword(config.getProperty("db.password"));
        conn = ods.getConnection();
        log.info("Established connection with the database");
    }

    @SneakyThrows
    public static String getTransactionStatus(String shortId) {
        String query = "Select ID_TXN_STATUS from HPC.HPC_TXNS where STR_SHORT_ID = '"+shortId+"'";
        return executeAndGetResult(query);
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
}
