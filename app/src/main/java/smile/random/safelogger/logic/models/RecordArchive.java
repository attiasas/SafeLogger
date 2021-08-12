package smile.random.safelogger.logic.models;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import javax.crypto.SecretKey;

import smile.random.safelogger.logic.SecurityHandler;

/**
 * Author : Assaf Attias
 * A representation of a log Information password archive (used password) and holds the data that was fetched from the DB.
 */
public class RecordArchive
{
    private int id;
    private int logId;
    private int startDay;
    private int startMonth;
    private int startYear;
    private int endDay;
    private int endMonth;
    private int endYear;
    private byte[] encrypted;
    private byte[] sIv;


    private String password;
    private int dayCount;

    /**
     * Constructor
     * @param id - the id of the archive password
     * @param logId - the log id that the archive belongs to
     * @param startDay - the day of the date that the password started been used
     * @param startMonth - the month of the date that the password started been used
     * @param startYear - the year of the date that the password started been used
     * @param endDay - the day of the date that the password stopped been used
     * @param endMonth - the month of the date that the password stopped been used
     * @param endYear - the year of the date that the password stopped been used
     * @param encrypted - the encrypted password
     * @param sIv - the initial vector that was used to encrypt the password
     */
    public RecordArchive(int id, int logId, int startDay, int startMonth, int startYear, int endDay, int endMonth, int endYear, byte[] encrypted, byte[] sIv)
    {
        this.id = id;
        this.logId = logId;

        this.startDay = startDay;
        this.startMonth = startMonth;
        this.startYear = startYear;
        this.endDay = endDay;
        this.endMonth = endMonth;
        this.endYear = endYear;

        this.encrypted = encrypted;
        this.sIv = sIv;

        this.password = null;
        this.dayCount = calculatePasswordDayCount(startDay,startMonth,startYear,endDay,endMonth,endYear);
    }

    /**
     * Decrypt the Password using a given secret and saves (update) the plain text in this object
     * @param secretKey - a given key to use when decrypting
     * @throws Exception - when decrypting
     */
    public void setPassword(SecretKey secretKey) throws Exception
    {
        if(this.password == null)
            this.password = SecurityHandler.decrypt(this.encrypted,secretKey,this.sIv);
    }

    /**
     * Get the number of days the password been used
     * @return - the number of days that the password have been used
     */
    public int getDayCount() {
        return dayCount;
    }

    /**
     * Get the plain-text password, can only be called after calling the method setPassword
     * @return plain-text decrypted password, null if 'setPassword' was not called
     */
    public String getPassword() {  return password; }

    /**
     * Get a String representation of the starting date the password have been used
     * @return string representation of the date
     */
    public String getStartDate()
    {
        return startDay + "/" + startMonth + "/" + startYear;
    }

    /**
     * Get a String representation of the ending date the password stopped been used
     * @return string representation of the date
     */
    public String getEndDate()
    {
        return endDay + "/" + endMonth + "/" + endYear;
    }

    /**
     * Get the id of the archive password
     * @return - the archive id
     */
    public int getId() {
        return id;
    }

    /**
     * Calculates the number of days that the password have benn used (base on a given parameters)
     * @param sDay - the start day of the date the password have been used
     * @param sMonth - the start month of the date the password have been used
     * @param sYear - the start year of the date the password have been used
     * @param eDay - the end day of the date the password have been used
     * @param eMonth - the end month of the date the password have been used
     * @param eYear - the end year of the date the password have been used
     * @return number of days that passed.
     */
    private static int calculatePasswordDayCount(int sDay, int sMonth, int sYear, int eDay, int eMonth, int eYear)
    {
        LocalDateTime recordDate = LocalDateTime.of(sYear,sMonth,sDay,0,0);
        LocalDateTime currentDate = LocalDateTime.of(eYear,eMonth,eDay,0,0);

        return (int) ChronoUnit.DAYS.between(recordDate,currentDate);
    }
}
