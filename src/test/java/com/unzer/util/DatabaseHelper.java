package com.unzer.util;

import lombok.SneakyThrows;
import oracle.jdbc.pool.OracleDataSource;

import java.sql.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class DatabaseHelper {
    static final String JDBC_DRIVER = "oracle.jdbc.OracleDriver";
    static final String DB_URL = "jdbc:oracle:thin:@//db.integration.hpchd.loc:1521/hpcint01";
    static final String USER = "HPC_SAMEER_KARMARKAR";
    static final String PASS = "duHdfPQVwjmz26cb";
    private Connection conn;

    public static DatabaseHelper INSTANCE = new DatabaseHelper();

    @SneakyThrows
    private DatabaseHelper() {
        OracleDataSource ods = new OracleDataSource();
        ods.setURL(DB_URL);
        ods.setUser(USER);
        ods.setPassword(PASS);

        conn = ods.getConnection();

    }

    @SneakyThrows
    public String getGiccMessage(String shortId) {
        String databaseId = getDatabaseId(shortId);
        String query = "Select STR_LOG from HPC.HPC_TXN_HISTORY where id_txn = '"+databaseId+"' and STR_LOG like '%isomsg%'";
        String isoMessage = executeAndGetResult(query);
        assertThat("Unable to find iso message for short id"+shortId,isoMessage, is(not(emptyOrNullString())));
        return isoMessage;
    }

    @SneakyThrows
    public String getDatabaseId(String shortId) {
        String query = "select id from HPC.HPC_TXNS where STR_SHORT_ID = '"+shortId+"'";
        String id = executeAndGetResult(query);
        assertThat("Unable to find id for short id"+shortId,id, is(not(emptyOrNullString())));
        return id;

    }

    @SneakyThrows
    public String getEci(String shortId) {
        String databaseId = getDatabaseId(shortId);
        String query = "Select STR_ECI from HPC.HPC_TXNS_3DSEC where id ='"+databaseId+"'";
        String eci = executeAndGetResult(query);
        assertThat("Unable to find eci for short id"+shortId,eci, is(not(emptyOrNullString())));
        return eci;
    }

    @SneakyThrows
    public String getCavv(String shortId) {
        String databaseId = getDatabaseId(shortId);
        String query = "Select STR_VERIFICATION_ID from HPC.HPC_TXNS_3DSEC where id ='"+databaseId+"'";
        String cavv = executeAndGetResult(query);
        assertThat("Unable to find CAVV for short id"+shortId,cavv, is(not(emptyOrNullString())));
        return cavv;
    }

    @SneakyThrows
    public String getDsTransId(String shortId) {
        String databaseId = getDatabaseId(shortId);
        String query = "Select STR_DS_TRANSACTION_ID from HPC.HPC_TXNS_3DSEC where id ='"+databaseId+"'";
        String dsTransId = executeAndGetResult(query);
        assertThat("Unable to find DS_TRANS_ID for short id"+shortId,dsTransId, is(not(emptyOrNullString())));
        return dsTransId;
    }

    @SneakyThrows
    private String executeAndGetResult(String query) {
        Statement statement = conn.createStatement();
        ResultSet rs = statement.executeQuery(query);

        String output = "";
        while(rs.next()){
            output  = rs.getString(1);
        }

        return output;
    }

    @SneakyThrows
    private ResultSet execute(String query) {
        Statement statement = conn.createStatement();
        ResultSet rs = statement.executeQuery(query);
        return rs;
    }
}
