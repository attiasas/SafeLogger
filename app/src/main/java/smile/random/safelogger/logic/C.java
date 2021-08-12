package smile.random.safelogger.logic;

/**
 * Author : Assaf Attias
 * Class holding all the logic constants of the applications
 */
public class C {


    public final static String HASH_ALGORITHM = "SHA-512";
    public final static String ENC_KEY_ALGORITHM = "PBKDF2WithHmacSHA256";
    public final static String KEY_ALGORITHM = "AES";
    public final static String ENC_ALGORITHM = "AES/CBC/PKCS5Padding";

    public final static int BLOCK_SIZE = 16;
    public final static int KEY_LEN = 256;
    public final static int STR_PARAM = 65536; // num of itr of enc

    public static final int MIN_KEY_LEN = 6;
    public static final int SALT = 1;
    public static final int HASHED = 0;

    public static final double D_W = 0.95;
    public static final double D_H = 0.7;

    public static final int DAY = 0;
    public static final int MONTH = 1;
    public static final int YEAR = 2;

    public static final int RECORDS = 0;
    public static final int ARCHIVE = 1;

    public static final String PASSWORD_HIDDEN = "********************";
    public static final String TAP_ACTION_TITLE = "Choose an action";
    public static final CharSequence[] ROW_OPTIONS = {"Show Password","Update","Delete","More Info"};
    public static final String HIDE_OPTION = "Hide Password";
    public static final int TOGGLE_PASSWORD_SHOW = 0;
    public static final int UPDATE_ROW_DATA = 1;
    public static final int DELETE_ROW = 2;
    public static final int SHOW_MORE_INFO = 3;

    public static final int SHORT_THRESHOLD = 150;
    public static final int MEDIUM_THRESHOLD = 240;

    public static final int MAX_TEXT_LEN = 20;

    public static final int NORMAL = 0;
    public static final int DIGIT = 1;
    public static final int UPPER = 2;
}
