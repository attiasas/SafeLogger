package smile.random.safelogger.logic;

import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

import smile.random.safelogger.logic.models.PreviewRecord;
import smile.random.safelogger.logic.models.RecordArchive;

/**
 * Author : Assaf Attias
 * Handles All communication with the Database (SQLite)
 */
public class SQLHandler extends SQLiteOpenHelper {

    private final String TABLE_KEY = "CREATE TABLE IF NOT EXISTS PASSKEYS(" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "salt BLOB, " +
            "hashPassword BLOB)";

    private final String TABLE_LOG = "CREATE TABLE IF NOT EXISTS LOGGER(" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "logName VARCHAR, " +
            "userName VARCHAR, " +
            "password BLOB, " +
            "iv BLOB, " +
            "day INTEGER, " +
            "month INTEGER, " +
            "year INTEGER)";

    private final String TABLE_ARCHIVE = "CREATE TABLE IF NOT EXISTS ARCHIVE(" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "logId INTEGER, " +
            "startDay INTEGER, " +
            "startMonth INTEGER, " +
            "startYear INTEGER, " +
            "endDay INTEGER, " +
            "endMonth INTEGER, " +
            "endYear INTEGER, " +
            "password BLOB, " +
            "iv BLOB, " +
            "CONSTRAINT FK_log FOREIGN KEY (logId) REFERENCES LOGGER(id) ON DELETE CASCADE)";

    /**
     * Constructor
     * @param context - context to attach and init the handler
     */
    public SQLHandler(Context context) {
        super(context,"SMILELOGGERDB.sqlite",null,1);
        init();
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) { }
    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) { }

    /**
     * initialize database tables
     */
    private void init()
    {
        SQLiteDatabase database = getWritableDatabase();

        database.execSQL(TABLE_KEY);
        database.execSQL(TABLE_LOG);
        database.execSQL(TABLE_ARCHIVE);

        database.close();
    }

    /**
     * Check if the user has an authentication key or not (first time)
     * @return true if exists, false otherwise
     */
    public boolean hasKey()
    {
        SQLiteDatabase database = getReadableDatabase();
        int nRows = (int)DatabaseUtils.queryNumEntries(database, "PASSKEYS");
        database.close();
        return nRows > 0;
    }

    /**
     * Get the current authentication key information
     * @return
     */
    public byte[][] getKey()
    {
        if (!hasKey())
            return null;

        SQLiteDatabase database = getReadableDatabase();

        String sql = "SELECT hashPassword,salt,id FROM PASSKEYS ORDER BY id DESC LIMIT 0, 1";
        Cursor data = database.rawQuery(sql, null);

        byte[][] res = new byte[2][];
        while (data.moveToNext())
        {
            res[C.HASHED] = data.getBlob(0);
            res[C.SALT] = data.getBlob(1);

        }
        data.close();
        database.close();

        return res;
    }

    /**
     * Update the user authenticate-key
     * @param key - hashed password to save
     * @param kSalt - salt used to generate the key
     * @return true if the database has been updated successfully, false otherwise
     */
    public boolean updateKey(byte[] key, byte[] kSalt)
    {
        SQLiteDatabase database = getWritableDatabase();

        String sql = "INSERT INTO PASSKEYS VALUES(NULL,?,?)";
        SQLiteStatement statement = database.compileStatement(sql);
        statement.clearBindings();

        statement.bindBlob(1,kSalt);
        statement.bindBlob(2,key);
        long nId = statement.executeInsert();

        statement.close();
        database.close();

        return nId != -1;
    }

    /**
     * Get the current date information representation {Day,Month,Year}
     * @return double array with the digit representation of the current date.
     */
    private double[] getCurrentDate()
    {
        Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
        int day = calendar.get(Calendar.DATE);
        int month = calendar.get(Calendar.MONTH) + 1;
        int year = calendar.get(Calendar.YEAR);

        return new double[]{(double)day,(double)month,(double)year};
    }

    /**
     * Insert new log record into the database given its information.
     * @param logName - the name of the log
     * @param userName - the user-name used in the log
     * @param encrypted - the password encrypted with the user current secret used in the log
     * @param sIv - the initial vector that was used to encrypt the password
     * @return true if the record was added successfully, false otherwise
     */
    public boolean insertRecord(String logName, String userName, byte[] encrypted, byte[] sIv)
    {
        SQLiteDatabase database = getWritableDatabase();

        String sql = "INSERT INTO LOGGER VALUES(NULL,?,?,?,?,?,?,?)";
        SQLiteStatement statement = database.compileStatement(sql);
        statement.clearBindings();

        double[] currDate = getCurrentDate();

        statement.bindString(1,logName);
        statement.bindString(2,userName);
        statement.bindBlob(3,encrypted);
        statement.bindBlob(4,sIv);
        statement.bindDouble(5,currDate[C.DAY]);
        statement.bindDouble(6,currDate[C.MONTH]);
        statement.bindDouble(7,currDate[C.YEAR]);

        long nId = statement.executeInsert();

        statement.close();
        database.close();

        return nId != -1;
    }

    /**
     * Check if a given log-name is already exists in the database
     * @param name - the log name to check
     * @return true if the log-name is unique and not found in the database, false otherwise
     */
    public boolean isLogNameUnique(String name)
    {
        SQLiteDatabase database = getReadableDatabase();

        String sql = "SELECT count(*) FROM LOGGER WHERE logName= ?";
        SQLiteStatement statement = database.compileStatement(sql);
        statement.clearBindings();

        statement.bindString(1,name);

        long count = statement.simpleQueryForLong();

        statement.close();
        database.close();

        return count == 0;
    }

    /**
     * Get all the records from the database as an object representation.
     * Optional get only one record base on a given id, if id = -1 returns all records
     * @param id_where - id of the record to return, if -1 then returns all the records
     * @return list of records stored in the database as object representation
     */
    public List<PreviewRecord> getRecords(int id_where)
    {
        SQLiteDatabase database = getReadableDatabase();

        ArrayList<PreviewRecord> result = new ArrayList<>();

        String sql = id_where == -1 ? "SELECT * FROM LOGGER" : "SELECT * FROM LOGGER WHERE id = ?";

        Cursor cursor = database.rawQuery(sql, id_where == -1 ? null : new String[]{String.valueOf(id_where)});
        while (cursor.moveToNext())
        {
            int id = cursor.getInt(0);
            String logName = cursor.getString(1);
            String userName = cursor.getString(2);
            byte[] encrypted = cursor.getBlob(3);
            byte[] sIv = cursor.getBlob(4);
            int day = cursor.getInt(5);
            int month = cursor.getInt(6);
            int year = cursor.getInt(7);

            result.add(new PreviewRecord(id,logName,userName,encrypted,sIv,day,month,year));
        }

        cursor.close();
        database.close();
        return result;
    }

    /**
     * Get all the records from the database as an object representation.
     * @return list of records stored in the database as object representation
     */
    public List<PreviewRecord> getAllRecords()
    {
        return getRecords(-1);
    }

    /**
     * Get all the archives of all records from the database as an object representation.
     * @return list of archives stored in the database as object representation
     */
    public List<RecordArchive> getAllRecordArchives()
    {
        return getRecordArchives(-1);
    }

    /**
     * Get all the archives of a given record from the database as an object representation.
     * Optional get only one record base on a given id, if id = -1 returns all records
     * @param id_where - id of the record to return its archives, if -1 then returns all the archives of all the records
     * @return list of archives stored in the database as object representation
     */
    public List<RecordArchive> getRecordArchives(int id_where)
    {
        SQLiteDatabase database = getReadableDatabase();
        ArrayList<RecordArchive> result = new ArrayList<>();

        String sql = id_where == -1 ? "SELECT * FROM ARCHIVE" : "SELECT * FROM ARCHIVE WHERE logId = ? ORDER BY id DESC";
        Cursor cursor = database.rawQuery(sql, id_where == -1 ? null : new String[]{String.valueOf(id_where)});

        while (cursor.moveToNext())
        {
            int id = cursor.getInt(0);
            int logId = cursor.getInt(1);
            int sDay = cursor.getInt(2);
            int sMonth = cursor.getInt(3);
            int sYear = cursor.getInt(4);
            int eDay = cursor.getInt(5);
            int eMonth = cursor.getInt(6);
            int eYear = cursor.getInt(7);
            byte[] encrypted = cursor.getBlob(8);
            byte[] sIv = cursor.getBlob(9);

            result.add(new RecordArchive(id,logId,sDay,sMonth,sYear,eDay,eMonth,eYear,encrypted,sIv));
        }

        cursor.close();
        database.close();
        return result;
    }

    /**
     * Delete a given Log from the database resulting in deleting all of its archive as well.
     * @param id - the id of the log needed to be deleted
     * @return true if the log was removed, false otherwise.
     */
    public boolean deleteRecord(int id)
    {
        SQLiteDatabase database = getWritableDatabase();
        // query to delete record using id
        String sql = "DELETE FROM LOGGER WHERE id=?";

        SQLiteStatement statement = database.compileStatement(sql);
        statement.clearBindings();
        statement.bindDouble(1,(double)id);

        int effectedRows = statement.executeUpdateDelete();

        statement.close();
        database.close();

        return effectedRows > 0;
    }

    /**
     * Update a given record information to the given input, if parameter is null this field will
     * not change its values, if password has change will insert the previous into archive
     * @param record - a record to update its information
     * @param logNameNew - the new log name
     * @param userNameNew - the new user-name of the log
     * @param encrypted - the new chiper-text password (after encryption)
     * @param sIv - the new initial vector that was used for the encryption
     * @return true if the record has been update successfully, false otherwise
     */
    public boolean updateRecord(PreviewRecord record, String logNameNew, String userNameNew, byte[] encrypted, byte[] sIv)
    {
        if(record == null || record.getPassword() == null)
            return false;

        SQLiteDatabase database = getWritableDatabase();

        database.beginTransaction();
        try
        {
            if(logNameNew != null) // log name needs update
            {
                String sql = "UPDATE LOGGER SET logName=? WHERE id=?";
                SQLiteStatement statement = database.compileStatement(sql);
                statement.clearBindings();
                statement.bindString(1,logNameNew);
                statement.bindDouble(2,record.getId());
                statement.execute();
                statement.close();
            }
            if(userNameNew != null) // log user-name needs update
            {
                String sql = "UPDATE LOGGER SET userName=? WHERE id=?";
                SQLiteStatement statement = database.compileStatement(sql);
                statement.clearBindings();
                statement.bindString(1,userNameNew);
                statement.bindDouble(2,record.getId());
                statement.execute();
                statement.close();
            }
            if(encrypted != null || sIv != null) // new password
            {
                double[] currDate = getCurrentDate();

                // insert old to archive
                String archiveSql = "INSERT INTO ARCHIVE VALUES(NULL,?,?,?,?,?,?,?,?,?)";
                SQLiteStatement insertStatment = database.compileStatement(archiveSql);
                insertStatment.bindDouble(1,record.getId());
                insertStatment.bindDouble(2,record.getDay());
                insertStatment.bindDouble(3,record.getMonth());
                insertStatment.bindDouble(4,record.getYear());
                insertStatment.bindDouble(5,currDate[C.DAY]);
                insertStatment.bindDouble(6,currDate[C.MONTH]);
                insertStatment.bindDouble(7,currDate[C.YEAR]);
                insertStatment.bindBlob(8,record.getEncrypted());
                insertStatment.bindBlob(9,record.getsIv());
                insertStatment.execute();
                insertStatment.close();

                // update main log-record
                String sql = "UPDATE LOGGER SET iv=?, password=?, day=?, month=?, year=? WHERE id=?";
                SQLiteStatement statement = database.compileStatement(sql);
                statement.clearBindings();
                statement.bindBlob(1,sIv);
                statement.bindBlob(2,encrypted);
                statement.bindDouble(3,currDate[C.DAY]);
                statement.bindDouble(4,currDate[C.MONTH]);
                statement.bindDouble(5,currDate[C.YEAR]);
                statement.bindDouble(6,record.getId());
                statement.execute();
                statement.close();
            }

            database.setTransactionSuccessful();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            database.endTransaction();
            database.close();
            return false;
        }
        database.endTransaction();
        database.close();

        return true;
    }

    /**
     * Create an sql statement that can update the record and archive's password and iv fields.
     * @param recordIdList - the record's id list
     * @param archiveIdList - the archive's id list
     * @return array size of 2, each entry is sql statement, index 0 = record update statement, index 1 for archive update.
     */
    private String[] createSqlFromLists(int[] recordIdList, int[] archiveIdList)
    {
        String sqlRecords = "UPDATE LOGGER SET password = (case ";
        String sqlRecordsIv = "end), iv = (case ";

        for(int i = 0; i < recordIdList.length; i++)
        {
            sqlRecords += "when id=? then ? ";
            sqlRecordsIv += "when id=? then ? ";
        }
        sqlRecords += sqlRecordsIv + "end)";

        String sqlArchive = "UPDATE ARCHIVE SET password = (case ";
        String sqlArchiveIv = "end), iv = (case ";

        for(int i = 0; i < archiveIdList.length; i++)
        {
            sqlArchive += "when id=? then ? ";
            sqlArchiveIv += "when id=? then ? ";
        }
        sqlArchive += sqlArchiveIv + "end);";

        return new String[]{sqlRecords,sqlArchive};
    }

    /**
     * Make the update of the authentication key and all the data after re-encrypt, all in a transaction.
     * @param hashed - the new hashed authentication key
     * @param kSalt - the salt that was used to hash the new authentication key
     * @param recordIdList - a list of id's of the records in the database that their index correspond to the other records lists
     * @param encryptedRecordList - a list of the re-encrypted passwords of each record
     * @param ivRecordList - a list of the initial vectors that was used to re-encrypted passwords of each record
     * @param archiveIdList - a list of id's of the archives in the database that their index correspond to the other archives lists
     * @param encryptedArchiveList - a list of the re-encrypted passwords of each archives
     * @param ivArchiveList - a list of the initial vectors that was used to re-encrypted passwords of each archives
     * @return true if the update transaction was success, false otherwise
     */
    public boolean updatePasswordsInDB(byte[] hashed, byte[] kSalt, int[] recordIdList, byte[][] encryptedRecordList, byte[][] ivRecordList, int[] archiveIdList, byte[][] encryptedArchiveList, byte[][] ivArchiveList)
    {
        if(hashed == null || kSalt == null || recordIdList == null || encryptedRecordList == null || ivRecordList == null || archiveIdList == null || encryptedArchiveList == null || ivArchiveList == null)
            return false;

        if(recordIdList.length != encryptedRecordList.length || recordIdList.length != ivRecordList.length)
            return false;

        if(archiveIdList.length != encryptedArchiveList.length || archiveIdList.length != ivArchiveList.length)
            return false;

        SQLiteDatabase database = getWritableDatabase();

        database.beginTransaction();
        try
        {
            // update key
            String sql = "INSERT INTO PASSKEYS VALUES(NULL,?,?)";
            SQLiteStatement statement = database.compileStatement(sql);
            statement.clearBindings();
            statement.bindBlob(1,kSalt);
            statement.bindBlob(2,hashed);
            statement.execute();
            statement.close();

            String[] updateSQLs = createSqlFromLists(recordIdList,archiveIdList);

            statement = database.compileStatement(updateSQLs[C.RECORDS]);
            statement.clearBindings();

            for(int i = 0; i < recordIdList.length; i++)
            {
                statement.bindDouble(2 * i + 1, recordIdList[i]);
                statement.bindBlob(2 * i + 2, encryptedRecordList[i]);

                statement.bindDouble(2 * i + 1 + (2 * recordIdList.length), recordIdList[i]);
                statement.bindBlob(2 * i + 2 + (2 * recordIdList.length), ivRecordList[i]);
            }
            statement.execute();
            statement.close();

            statement = database.compileStatement(updateSQLs[C.ARCHIVE]);
            statement.clearBindings();

            for(int i = 0; i < archiveIdList.length; i++)
            {
                statement.bindDouble(2 * i + 1, archiveIdList[i]);
                statement.bindBlob(2 * i + 2, encryptedArchiveList[i]);

                statement.bindDouble(2 * i + 1 + (2 * archiveIdList.length), archiveIdList[i]);
                statement.bindBlob(2 * i + 2 + (2 * archiveIdList.length), ivArchiveList[i]);
            }
            statement.execute();
            statement.close();

            database.setTransactionSuccessful();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            database.endTransaction();
            database.close();
            return false;
        }
        database.endTransaction();
        database.close();

        return true;
    }
}
