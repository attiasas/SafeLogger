package smile.random.safelogger.logic.models;

import android.util.Log;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;

import javax.crypto.SecretKey;

import smile.random.safelogger.logic.SecurityHandler;

/**
 * Author : Assaf Attias
 * A representation of a log Information preview and holds the data that was fetched from the DB.
 */
public class PreviewRecord implements Serializable
{

    private int id;
    private String logName;
    private String userName;

    private byte[] encrypted;
    private byte[] sIv;

    private String password = null;

    private int dayCount;
    private int day;
    private int month;
    private int year;

    /**
     * Constructor
     * @param id - the id of the log
     * @param logName - the name of the log
     * @param userName - the user-name of the log
     * @param encrypted - the encrypted password of the log
     * @param sIv - the initial vector that was used to encrypt the log
     * @param day - the day the log was created
     * @param month - the month the log was created
     * @param year - the year the log was created
     */
    public PreviewRecord(int id, String logName, String userName, byte[] encrypted, byte[] sIv, int day, int month, int year)
    {
        this.id = id;
        this.logName = logName;
        this.userName = userName;
        this.encrypted = encrypted;
        this.sIv = sIv;

        this.day = day;
        this.month = month;
        this.year = year;

        this.dayCount = calculatePasswordDayCount(day,month,year);
    }

    /**
     * * Decrypt the Password using a given secret and saves (update) the plain text in this object
     * @param secretKey - a given key to use when decrypting
     * @throws Exception - when decrypting
     */
    public void setPassword(SecretKey secretKey) throws Exception
    {
        if(this.password == null)
            this.password = SecurityHandler.decrypt(this.encrypted,secretKey,this.sIv);
    }

    /**
     * Get the number of days the current password is used
     * @return - the number of days that passed since the password changed
     */
    public int getDayCount() {
        return dayCount;
    }

    /**
     * Get the id of the log record
     * @return - the log id
     */
    public int getId() {
        return id;
    }

    /**
     * Get the day (in a month) that the current password of the log record was created
     * @return a given day of a date
     */
    public int getDay() {
        return day;
    }

    /**
     * Get the month (in a year) that the current password of the log record was created
     * @return a given month of a date
     */
    public int getMonth() {
        return month;
    }

    /**
     * Get the encrypted byte representation of the password
     * @return - byte array, encrypted password
     */
    public byte[] getEncrypted() {
        return encrypted;
    }

    /**
     * Get the initial byte vector that was used in the encryption process
     * @return - byte array, initial vector
     */
    public byte[] getsIv() {
        return sIv;
    }

    /**
     * Get the yearthat the current password of the log record was created
     * @return a given year of a date
     */
    public int getYear() {
        return year;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof PreviewRecord)
        {
            PreviewRecord other = (PreviewRecord)obj;
            return  other.id == this.id &&
                    this.logName.equals(other.logName) &&
                    this.userName.equals(other.userName) &&
                    Arrays.equals(this.encrypted,other.encrypted);
        }

        return super.equals(obj);
    }

    /**
     * Get the name of the log
     * @return - the name of the log
     */
    public String getLogName() {
        return logName;
    }

    /**
     * Get the user-name of the log
     * @return - the user-name of the log
     */
    public String getUserName() {
        return userName;
    }

    /**
     * Get a String representation of the starting date the password have been used
     * @return string representation of the date
     */
    public String getStartDate()
    {
        return this.day + "/" + this.month+ "/" + this.year;
    }

    /**
     * Get the plain-text password, can only be called after calling the method setPassword
     * @return plain-text decrypted password, null if 'setPassword' was not called
     */
    public String getPassword() {
        return password;
    }

    /**
     * Calculates the number of days that passed since the password created (a given parameters)
     * until the current date.
     * @param day - the day the password was created
     * @param month - the month the password was created
     * @param year - the year the password was created
     * @return number of days that passed since the given parameters until today
     */
    private int calculatePasswordDayCount(int day, int month, int year)
    {
        LocalDateTime recordDate = LocalDateTime.of(year,month,day,0,0);
        LocalDateTime currentDate = LocalDateTime.now();

        return (int) ChronoUnit.DAYS.between(recordDate,currentDate);
    }

}
