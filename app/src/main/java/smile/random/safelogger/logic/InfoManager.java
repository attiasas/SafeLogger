package smile.random.safelogger.logic;

import android.content.Context;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import javax.crypto.SecretKey;

import smile.random.safelogger.logic.models.PreviewRecord;
import smile.random.safelogger.logic.models.RecordArchive;

/**
 * Author : Assaf Attias
 * Manage All Application Logic and provide interface for the GUI
 */
public class InfoManager {

    private Context context;
    private SQLHandler db;
    private SecretKey secret;

    private static InfoManager instance = null;

    private List<PreviewRecord> records;
    private boolean listChanged = true;

    /**
     * Constructor of the Singleton
     * @param context - context of the initial activity
     */
    private InfoManager(Context context)
    {
        this.context = context;
        this.db = new SQLHandler(context);
        this.records = new ArrayList<>();
    }

    /**
     * Initialize the InfoManager Instance, this method must be called when app loads
     * @param c - context of the initial activity
     */
    public static void initialize(Context c)
    {
        instance = new InfoManager(c);
    }

    /**
     * Get The InfoManager Singleton instance
     * @return the InfoManager instance
     */
    public static InfoManager get()
    {
        return instance;
    }

    /**
     * Generate random password base on a given parameters
     * @param passLen - the length of the password
     * @param withDigit - is the generated password should include digits
     * @param withUpper - is the generated password should include upper case chars
     * @param ratio
     * @return a randomly generated plain-text password
     */
    public static String generatePassword(int passLen, boolean withDigit, boolean withUpper, double ratio)
    {
        // init and calculate char type counts
        String password = "";
        Random rand = new Random();

        int digitCharsToAdd = withDigit ? 1 : 0;
        int upperCharsToAdd = withUpper ? 1 : 0;
        int normalLen = (int) Math.floor(ratio * passLen);

        if (normalLen + (digitCharsToAdd + upperCharsToAdd) > passLen)
        {
            normalLen = passLen - digitCharsToAdd - upperCharsToAdd;
        }

        while (normalLen + (digitCharsToAdd + upperCharsToAdd) < passLen)
        {
            double random = rand.nextDouble();
            if(withDigit && withUpper)
            {
                if(random <= 0.5) digitCharsToAdd++;
                else upperCharsToAdd++;
            }
            else if(withDigit) digitCharsToAdd++;
            else if(withUpper) upperCharsToAdd++;
            else normalLen++;
        }

        ArrayList<Integer> typeOptions = new ArrayList<>();

        // generate
        for(int count = 0; count < passLen; count++)
        {
            if(digitCharsToAdd > 0) typeOptions.add(C.DIGIT);
            if(upperCharsToAdd > 0) typeOptions.add(C.UPPER);
            if(count + (digitCharsToAdd + upperCharsToAdd) < passLen) typeOptions.add(C.NORMAL);

            int typeToAdd = typeOptions.get(rand.nextInt(typeOptions.size()));;
            char randomChar;

            switch (typeToAdd)
            {
                case C.DIGIT:
                    randomChar = (char) (rand.nextInt('9'-'0') + '0');
                    digitCharsToAdd--;
                    break;
                case C.UPPER:
                    randomChar = (char) (rand.nextInt('Z'-'A') + 'A');
                    upperCharsToAdd--;
                    break;
                default: // Normal
                    randomChar = (char) (rand.nextInt('z'-'a') + 'a');
                    break;
            }

            typeOptions.clear();
            password += randomChar;
        }

        return password;
    }

    /**
     * Check if this is the first time a user logs into the application
     * @return true if an authentication key is not exists, false otherwise
     */
    public boolean firstTime()
    {
        return !this.db.hasKey();
    }

    /**
     * Check if the user is authenticate
     * @return true if the user authenticated, false otherwise
     */
    public boolean isAuthenticate()
    {
        return this.secret != null;
    }

    /**
     * Validation Action Results
     */
    public enum ValidationResult
    {
        Legal, BadShort, BadChars, BadShortName, BadShortUserName, BadShortPassword, BadExist, Bad, WarnExist
    }

    /**
     * Validation a given plain txt password
     * #Chars at least C.MIN_KEY_LEN and at least one digit, lower and upper chars
     * @param key - a given password key to validate
     * @return ValidationResult, the result of the validation
     */
    public ValidationResult validateKey(String key)
    {
        if (key == null)
            return ValidationResult.Bad;
        if (key.length() < C.MIN_KEY_LEN)
            return ValidationResult.BadShort;

        boolean hasNumber = false;
        boolean hasLowChar = false;
        boolean hasUpperChar = false;

        for (int i = 0; i < key.length(); i++)
        {
            char c = key.charAt(i);
            if(!hasNumber && c >= '0' && c <= '9')
                hasNumber = true;
            if(!hasLowChar && c >= 'a' && c <= 'z')
                hasLowChar = true;
            if(!hasUpperChar && c >= 'A' && c <= 'Z')
                hasUpperChar = true;
        }

        return hasNumber && hasLowChar && hasUpperChar ? ValidationResult.Legal : ValidationResult.BadChars;
    }

    /**
     * Validation a given Log Information (not empty)
     * @param name - the name of the log
     * @param userName - user name of the log
     * @param password - password of the log
     * @return ValidationResult, the result of the validation
     */
    public ValidationResult validateLog(String name, String userName, String password)
    {
        if(name == null || userName == null || password == null)
            return ValidationResult.Bad;

        if(name.isEmpty())
            return ValidationResult.BadShortName;
        if(userName.isEmpty())
            return ValidationResult.BadShortUserName;
        if(password.isEmpty())
            return ValidationResult.BadShortPassword;

        return this.db.isLogNameUnique(name) ? ValidationResult.Legal : ValidationResult.BadExist;
    }

    /**
     * Validation a given Log Information update.
     * it checks if the new password already been used in this current record
     * @param record - the record to check its archive
     * @param name - the name of the log
     * @param userName - user name of the log
     * @param password - password of the log
     * @return ValidationResult, the result of the validation
     */
    public ValidationResult validateLogUpdate(PreviewRecord record, String name, String userName, String password)
    {
        ValidationResult result = validateLog(name,userName,password);

        if(result.equals(ValidationResult.BadExist) && record.getLogName().equals(name))
            result = ValidationResult.Legal;

        if(result.equals(ValidationResult.Legal) && isAuthenticate())
        {
            boolean exists = false;
            List<RecordArchive> archives = getRecordArchive(record.getId());

            for (int i = 0; !exists && i < archives.size(); i++)
                exists = password.equals(archives.get(i).getPassword());

            return exists ? ValidationResult.WarnExist : result;
        }

        return result;
    }

    /**
     * Remove the secret key that was fetched in the authentication process
     */
    public void logOff()
    {
        this.secret = null;
    }

    /**
     * Preform a user authentication process with a given keyPassword
     * @param keyPassword - plain text to compare with the user saved key
     * @return true if the authentication process is successful, false otherwise
     */
    public boolean authentication(String keyPassword)
    {
        ValidationResult legal = validateKey(keyPassword);
        if (this.db.hasKey() && legal.equals(ValidationResult.Legal))
        {
            try
            {
                byte[][] sysKeyInfo = this.db.getKey();

                byte[] hashed = SecurityHandler.hash(keyPassword,sysKeyInfo[C.SALT]);

                boolean result = Arrays.equals(sysKeyInfo[C.HASHED],hashed); // authenticate
                if(result)
                    this.secret = SecurityHandler.getKeyFromPassword(keyPassword,sysKeyInfo[C.SALT]); // update secret to use for enc/dec
                else
                    logOff();

                return result;
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }

        return false;
    }

    /**
     * Add a new Log information (validation -> encrypt -> add to db) from a given input.
     * adding can only be preformed if the user is authenticated.
     * @param logName - string, the name of the log
     * @param userName - string, the user-name used in the log
     * @param password - string, the password used in the log
     * @return true if a new log was added, false otherwise
     */
    public boolean addRecord(String logName, String userName, String password)
    {
        // validate
        ValidationResult legal = validateLog(logName,userName,password);
        if (legal.equals(ValidationResult.Legal))
        {
            if(!isAuthenticate())
                return false;

            try
            {
                // encrypt
                byte[] sIv = SecurityHandler.generateRandomBytes();
                byte[] encrypted = SecurityHandler.encrypt(password,this.secret,sIv);
                // add
                boolean result = this.db.insertRecord(logName,userName,encrypted,sIv);
                if (result)
                    this.listChanged = true;

                return result;
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }

        return false;
    }

    /**
     * Returns all the logging data with preview only, after decrypting the information.
     * this method can be called only after authentication.
     * @return List with decrypted records, null if user did not authenticated
     */
    public List<PreviewRecord> getRecordsPreview()
    {
        if(!isAuthenticate())
            return null;

        if(this.listChanged)
        {
            try
            {
                List<PreviewRecord> updatedRecords = this.db.getAllRecords();

                for(int i = 0; i < updatedRecords.size(); i++)
                {
                    PreviewRecord updated = updatedRecords.get(i);
                    int oldIdx = this.records.indexOf(updated);
                    if(oldIdx != -1)
                        updatedRecords.set(i,this.records.get(oldIdx)); // for efficiency, decrypt operation only if new
                    else
                        updated.setPassword(this.secret);

                }

                this.records = updatedRecords;
                this.listChanged = false;
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }

        return this.records;
    }

    /**
     * Remove a given record from the database.
     * this operation can only be executed if the user has been authenticated
     * @param record - a given record to delete from the logging.
     * @return true if the record has been removed, false otherwise.
     */
    public boolean removeRecord(PreviewRecord record)
    {
        if(!isAuthenticate() || record == null)
            return false;

        boolean result = this.db.deleteRecord(record.getId());
        if (result)
            this.listChanged = true;

        return result;
    }

    /**
     * Update a given log-record information in the database
     * if password was update also create archive of the old one and saved the new encrypted password.
     * this operation can only be executed if the user has been authenticated
     * @param record - the log record to be updated
     * @param logName - the new log name
     * @param userName - the new user name
     * @param password - the new plain-txt password
     * @return true if the information was updated successfully, false otherwise
     */
    public boolean updateRecord(PreviewRecord record, String logName, String userName, String password)
    {
        // validate
        ValidationResult legal = validateLogUpdate(record,logName,userName,password);
        if (legal.equals(ValidationResult.Legal) || legal.equals(ValidationResult.WarnExist))
        {
            if(!isAuthenticate() || record == null || record.getPassword() == null)
                return false;

            String logNameNew = logName.equals(record.getLogName()) ? null : logName;
            String userNameNew = userName.equals(record.getUserName()) ? null : userName;

            // check if password was updated
            byte[] encrypted = null;
            byte[] sIv = null;
            if(!password.equals(record.getPassword()))
            {
                try
                {
                    sIv = SecurityHandler.generateRandomBytes();
                    encrypted = SecurityHandler.encrypt(password,this.secret,sIv);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                    return false;
                }
            }

            boolean result = this.db.updateRecord(record, logNameNew, userNameNew, encrypted, sIv);
            if (result)
                this.listChanged = true;

            return result;
        }

        return false;
    }

    /**
     * Returns all the archive records of a given log-record (by id), after decrypting the information.
     * this method can be called only after authentication.
     * @param id - the id of the log-record to fetch its archives
     * @return List of decrypted archive, null if user did not authenticated or problem occur
     */
    public List<RecordArchive> getRecordArchive(int id)
    {
        if(isAuthenticate())
        {
            try
            {
                // fetch
                List<RecordArchive> archives = this.db.getRecordArchives(id);
                // decrypt
                for(RecordArchive archive : archives)
                    archive.setPassword(this.secret);

                return archives;
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * Return all the archives of all the records that are stored in the database, after decrypting the information.
     * this method can be called only after authentication.
     * @return List of decrypted archive, null if user did not authenticated or problem occur
     */
    private List<RecordArchive> getAllRecordArchives()
    {
        if(!isAuthenticate())
            return null;
        try
        {
            List<RecordArchive> archives = this.db.getAllRecordArchives();

            for(RecordArchive archive : archives) archive.setPassword(this.secret);

            return archives;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Update the user authentication key to the given one and generating new salt for it.
     * in case a key already exists (not first time) Re-Encrypt all the Logging with the new one.
     * @param key - the new authentication key
     * @return true if the update was successful, false otherwise
     */
    public boolean updateKey(String key)
    {
        // validate
        ValidationResult legal = validateKey(key);
        if (legal.equals(ValidationResult.Legal))
        {
            boolean firstTime = firstTime();
            try
            {
                // hash the new key
                byte[] kSalt = SecurityHandler.generateRandomBytes();
                byte[] hashed = SecurityHandler.hash(key,kSalt);

                if(firstTime)
                    return this.db.updateKey(hashed,kSalt);
                else if(isAuthenticate())
                {
                    boolean stop = false;
                    SecretKey secretNew = SecurityHandler.getKeyFromPassword(key,kSalt);

                    int[] recordIdList = new int[this.records.size()];
                    byte[][] encryptedRecordList = new byte[this.records.size()][];
                    byte[][] ivRecordList = new byte[this.records.size()][];

                    for(int i = 0; !stop && i < this.records.size(); i++)
                    {
                        String password = this.records.get(i).getPassword();
                        if(password == null)
                        {
                            stop = true;
                            break;
                        }

                        byte[] iv = SecurityHandler.generateRandomBytes();
                        byte[] encrypted = SecurityHandler.encrypt(password,secretNew,iv);

                        recordIdList[i] = this.records.get(i).getId();
                        encryptedRecordList[i] = encrypted;
                        ivRecordList[i] = iv;
                    }

                    List<RecordArchive> archives = getAllRecordArchives();
                    int[] archiveIdList = new int[archives.size()];
                    byte[][] encryptedArchiveList = new byte[archives.size()][];
                    byte[][] ivArchiveList = new byte[archives.size()][];

                    for(int i = 0; !stop && i < archives.size(); i++)
                    {
                        String password = archives.get(i).getPassword();
                        if(password == null)
                        {
                            stop = true;
                            break;
                        }

                        byte[] iv = SecurityHandler.generateRandomBytes();
                        byte[] encrypted = SecurityHandler.encrypt(password,secretNew,iv);

                        archiveIdList[i] = archives.get(i).getId();
                        encryptedArchiveList[i] = encrypted;
                        ivArchiveList[i] = iv;
                    }

                    if(!stop)
                    {
                        boolean res = this.db.updatePasswordsInDB(hashed, kSalt, recordIdList, encryptedRecordList,ivRecordList,archiveIdList,encryptedArchiveList,ivArchiveList);
                        // update secret to the new one
                        if(res)
                        {
                            this.secret = secretNew;
                        }

                        return res;
                    }
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }

        return false;
    }
}
