package util;

import java.util.regex.Pattern;

public class ValidationUtil {
	private static final int EMAIL_MAX = 100;
	private static final int PWD_MAX = 100;
	private static final int NAME_MAX = 30;

	private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

	// 닉네임 허용 규칙(원하는대로 바꿔도 됨)
	private static final Pattern NAME_PATTERN = Pattern.compile("^[A-Za-z0-9가-힣_\\-\\.]+$");

	public static boolean isValidEmail(String email) {
		if (email == null)
			return false;
		email = email.trim();
		if (email.isEmpty() || email.length() > EMAIL_MAX)
			return false;
		return EMAIL_PATTERN.matcher(email).matches();
	}

	public static boolean isValidName(String name) {
		if (name == null)
			return false;
		name = name.trim();
		if (name.isEmpty() || name.length() > NAME_MAX)
			return false;
		return NAME_PATTERN.matcher(name).matches();
	}

	public static Enum validateSignup(String email, String pwd, String name, int stoneStyle) {
		// email
		if (email == null || email.trim().isEmpty())
			return Enum.EMAIL_EMPTY;
		if (email.trim().length() > EMAIL_MAX)
			return Enum.EMAIL_TOO_LONG;
		if (!EMAIL_PATTERN.matcher(email.trim()).matches())
			return Enum.EMAIL_INVALID_FORMAT;

		// pwd
		if (pwd == null || pwd.isEmpty())
			return Enum.PASSWORD_EMPTY;
		if (pwd.length() > PWD_MAX)
			return Enum.PASSWORD_TOO_LONG;
		if (pwd.contains(" "))
			return Enum.PASSWORD_INVALID_CHAR;

		// name
		if (name == null || name.trim().isEmpty())
			return Enum.NAME_EMPTY;
		if (name.trim().length() > NAME_MAX)
			return Enum.NAME_TOO_LONG;
		if (!NAME_PATTERN.matcher(name.trim()).matches())
			return Enum.NAME_INVALID_CHAR;

		// stone style
		if (stoneStyle < 1 || stoneStyle > 6)
			return Enum.STONE_STYLE_INVALID;

		return Enum.OK;
	}

	public static Enum validateLogin(String email, String pwd) {

		if (email == null || email.isEmpty())
			return Enum.EMAIL_EMPTY;

		if (pwd == null || pwd.isEmpty())
			return Enum.PASSWORD_EMPTY;

		if (email.length() > 255)
			return Enum.EMAIL_TOO_LONG;

		if (pwd.length() > 200)
			return Enum.PASSWORD_TOO_LONG;

		if (!email.contains("@") || !email.contains("."))
			return Enum.EMAIL_INVALID_FORMAT;

		if (email.contains(" ") || email.contains("'") || email.contains("\""))
			return Enum.EMAIL_INVALID_CHAR;

		if (pwd.contains(" ") || pwd.contains("'") || pwd.contains("\""))
			return Enum.PASSWORD_INVALID_CHAR;

		return Enum.OK;
	}
}
