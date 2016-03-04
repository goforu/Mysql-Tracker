import java.sql.*;
import java.util.Arrays;

/**
 * Created by goforu on 2016/3/1.
 */
public class GenTriggerSql {

    private staic String DATABASE_URL = "databae_url";//Set databse url
    private staic String DATABASE_NAME = "name";//database name
    private staic String DATABASE_PASSWORD = "password";//database password

    private static String HISTORY_TABLE = "tbl_history";
    private static String HISTORY_TABLE_COLUMN = "action_result";
    private static String[] FILTER_COLUMNS = {};//Set your filter columns, changes will be ignored

    public static void main(String[] args) {
        try {
            Connection con = null; 
            Class.forName("com.mysql.jdbc.Driver").newInstance(); 
            con = DriverManager.getConnection(DATABASE_URL + "?allowMultiQueries=true", DATABASE_NAME, DATABASE_PASSWORD); 

            //Retrive table columns
            DatabaseMetaData md = con.getMetaData();
            ResultSet rst = md.getTables(null, null, "%", null);

            Statement stmt; 
            stmt = con.createStatement();

            stmt.execute(genCreateHistoryTable());//Create history table
            while (rst.next()) {
                if (rst.getString("TABLE_NAME").equals(HISTORY_TABLE_COLUMN))
                    continue;
                stmt.execute(genDropTriggerSql(rst.getString("TABLE_NAME") + "_u"));//Delete 'update' trigger
                stmt.execute(genDropTriggerSql(rst.getString("TABLE_NAME") + "_d"));//Delete 'delete' trigger
                genTrigger(stmt, rst.getString("TABLE_NAME"));//Create trigger
            }
        } catch (Exception e) {
            System.out.print("MYSQL ERROR:" + e.getMessage());
        }

    }

    /**
     * generate 'create table' sql
     *
     * @return
     */
    private static String genCreateHistoryTable() {
        String sqlExe = "DROP TABLE IF EXISTS " + HISTORY_TABLE + ";\n" +
                "CREATE TABLE " + HISTORY_TABLE + "(id BIGINT PRIMARY KEY AUTO_INCREMENT, action_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " + HISTORY_TABLE_COLUMN + " VARCHAR(5000));";
        System.out.println(sqlExe);
        return sqlExe;
    }

    /**
     * Create Triggers
     *
     * @param stmt
     * @param tableName
     * @throws SQLException
     */
    private static void genTrigger(Statement stmt, String tableName) throws SQLException {
        ResultSet rs = stmt.executeQuery("SELECT * FROM " + tableName);
        ResultSetMetaData rsmd = rs.getMetaData();
        String sqlUExe = genUTriggerSql(tableName, rsmd);
        System.out.print(sqlUExe);
        stmt.execute(sqlUExe);
        String sqlDExe = genDTriggerSql(tableName, rsmd);
        System.out.print(sqlDExe);
        stmt.execute(sqlDExe);
    }

    private static String genDropTriggerSql(String triggerName) {
        String sqlExe = "DROP TRIGGER IF EXISTS " + triggerName + ";";
        System.out.println(sqlExe);
        return sqlExe;
    }

    /**
     * Generate 'update trigger' sql
     *
     * @param originTable
     * @param rsmd
     * @return
     * @throws SQLException
     */
    private static String genUTriggerSql(String originTable, ResultSetMetaData rsmd) throws SQLException {
        String sqlexe = "CREATE TRIGGER " + originTable + "_u AFTER UPDATE ON " + originTable + " FOR EACH ROW \n" +
                "BEGIN \n" +
                "IF ";
        for (int i = 1; i <= rsmd.getColumnCount(); i++) {
            if (Arrays.asList(FILTER_COLUMNS).contains(rsmd.getColumnName(i)))//filter condition
                continue;
            sqlexe += genExeConditionSql(rsmd.getColumnName(i)) + "OR ";
        }

        sqlexe += "false THEN \n" +
                "INSERT INTO " + HISTORY_TABLE + " (" + HISTORY_TABLE_COLUMN + ") \n" +
                "VALUES( " +
                "CONCAT(CONCAT('Update@" + originTable + ":',NEW.id,'|||'),";

        for (int i = 1; i <= rsmd.getColumnCount(); i++) {
//            if (Arrays.asList(FILTER_COLUMNS).contains(rsmd.getColumnName(i)))//filter record
//                continue;
            sqlexe += genItemUpdateSql(rsmd.getColumnName(i)) + ",";
        }
        sqlexe += "'') );\n" +
                "END IF;\n" +
                "END;\n";
        return sqlexe;
    }

    /**
     * Generate 'delete trigger' sql
     *
     * @param originTable
     * @param rsmd
     * @return
     * @throws SQLException
     */
    private static String genDTriggerSql(String originTable, ResultSetMetaData rsmd) throws SQLException {
        String sqlexe = "CREATE TRIGGER " + originTable + "_d BEFORE DELETE ON " + originTable + " FOR EACH ROW \n" +
                "BEGIN \n" +
                "INSERT INTO " + HISTORY_TABLE + " (" + HISTORY_TABLE_COLUMN + ") \n" +
                "VALUES( " +
                "CONCAT(CONCAT('Delete@" + originTable + ":',OLD.id,'|||')";

        for (int i = 1; i <= rsmd.getColumnCount(); i++) {
            sqlexe += genItemDeleteSql(rsmd.getColumnName(i));
        }
        sqlexe += ") );\n" +
                "END;\n";
        return sqlexe;
    }

    private static String genExeConditionSql(String targetColumn) {
        return "(OLD." + targetColumn + " IS NOT NULL AND NOT OLD." + targetColumn + " <=>'' AND NOT OLD." + targetColumn + " <=> NEW." + targetColumn + ") ";
    }

    private static String genItemUpdateSql(String targetColumn) {
        return "IF(NOT OLD." + targetColumn + " <=> NEW." + targetColumn + ",CONCAT_WS('->',CONCAT('" + targetColumn + "::',IFNULL(OLD." + targetColumn + ",'')),CONCAT(IFNULL(NEW." + targetColumn + ",''),'|'))" + ",'')";
    }

    private static String genItemDeleteSql(String targetColumn) {
        return ",CONCAT(CONCAT('" + targetColumn + "::',IFNULL(OLD." + targetColumn + ",'')),'|')";
    }
}
