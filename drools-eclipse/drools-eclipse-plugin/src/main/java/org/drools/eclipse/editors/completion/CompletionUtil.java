package org.drools.eclipse.editors.completion;

import java.util.regex.Pattern;

import org.eclipse.jdt.core.Signature;

public class CompletionUtil {

	protected static final Pattern INCOMPLETED_MVEL_EXPRESSION = Pattern
			.compile("[\\.\\(\\{\\[]\\Z", Pattern.DOTALL);

	protected static final Pattern COMPLETED_MVEL_EXPRESSION = Pattern.compile(
			"]\\)\\}\\]\\Z", Pattern.DOTALL);

	private CompletionUtil() {
	}

	/**
	 * Looks behind, gets stuff after the white space. Basically ripping out the
	 * last word.
	 */
	public static String stripLastWord(String prefix) {
		if ("".equals(prefix)) {
			return prefix;
		}
		if (prefix.charAt(prefix.length() - 1) == ' ') {
			return "";
		} else {
			char[] c = prefix.toCharArray();
			int start = 0;
			for (int i = c.length - 1; i >= 0; i--) {
				if (Character.isWhitespace(c[i]) || c[i] == '(' || c[i] == ':'
						|| c[i] == ';' || c[i] == '=' || c[i] == '<'
						|| c[i] == '>' || c[i] == '.' || c[i] == '{'
						|| c[i] == '}') {
					start = i + 1;
					break;
				}
			}
			prefix = prefix.substring(start, prefix.length());
			return prefix;
		}
	}

	public static String stripWhiteSpace(String prefix) {
		if ("".equals(prefix)) {
			return prefix;
		}
		if (prefix.charAt(prefix.length() - 1) == ' ') {
			return "";
		} else {
			char[] c = prefix.toCharArray();
			int start = 0;
			for (int i = c.length - 1; i >= 0; i--) {
				if (Character.isWhitespace(c[i])) {
					start = i + 1;
					break;
				}
			}
			prefix = prefix.substring(start, prefix.length());
			return prefix;
		}
	}

	/**
	 *
	 * @param backText
	 * @return a substring of the back text, that should be compilable without
	 *         syntax errors by the mvel compiler TODO: add tests and more use
	 *         cases
	 */
	public static String getCompilableText(String backText) {
		if (backText.trim().endsWith(";")) {
			// RHS expression should compile if it ends with ;. but to hget the last object, we do no want it
			return backText.substring(0, backText.length() - 1);
		}
		else if (backText.endsWith(".")) {
			// RHS expression should compile if it ends with ;
			return backText.substring(0, backText.length() - 1) ;
		} else if (CompletionUtil.COMPLETED_MVEL_EXPRESSION.matcher(backText)
				.matches()) {
			// RHS expression should compile if closed. just need to close the
			// statement
			return backText + ";";
		} else if (INCOMPLETED_MVEL_EXPRESSION.matcher(backText).matches()) {
			// remove the last char and close the statement
			return backText.substring(0, backText.length() - 1) ;
		} else {
			return backText;
		}
	}

	/*
	 * propertyname extraction and bean convention methods names checks
	 */

	public static boolean isGetter(String methodName, int argCount,
			String returnedType) {
		return isAccessor(methodName, argCount, 0, "get", returnedType,
				Signature.SIG_VOID, false);
	}

	public static boolean isSetter(String methodName, int argCount,
			String returnedType) {
		return isAccessor(methodName, argCount, 1, "set", returnedType,
				Signature.SIG_VOID, true);
	}

	public static boolean isIsGetter(String methodName, int argCount,
			String returnedType) {
		return isAccessor(methodName, argCount, 0, "is", returnedType,
				Signature.SIG_BOOLEAN, true);
	}

	public static String getPropertyName(String methodName, int parameterCount,
			String returnType) {
		if (methodName == null) {
			return null;
		}
		String simpleName = methodName.replaceAll("\\(\\)", "");
		int prefixLength = 0;
		if (isIsGetter(simpleName, parameterCount, returnType)) {

			prefixLength = 2;

		} else if (isGetter(simpleName, parameterCount, returnType) //
				|| isSetter(methodName, parameterCount, returnType)) {

			prefixLength = 3;
		} else {
			return methodName;
		}

		char firstChar = Character.toLowerCase(simpleName.charAt(prefixLength));
		String propertyName = firstChar
				+ simpleName.substring(prefixLength + 1);
		return propertyName;
	}

	public static String getPropertyName(String methodName, char[] signature) {
		if (signature == null || methodName == null) {
			return methodName;
		}

		int parameterCount = Signature.getParameterCount(signature);
		String returnType = new String(Signature.getReturnType(signature));

		return getPropertyName(methodName, parameterCount, returnType);
	}

	private static boolean isAccessor(String methodName,
			int actualParameterCount, int requiredParameterCount,
			String prefix, String returnType, String requiredReturnType,
			boolean includeType) {

		if (methodName.length() < prefix.length() + 1) {
			return false;
		}

		if (!methodName.startsWith(prefix)) {
			return false;
		}

		if (actualParameterCount != requiredParameterCount) {
			return false;
		}

		if (includeType) {
			if (!requiredReturnType.equals(returnType)) {
				return false;
			}
		} else {
			if (requiredReturnType.equals(returnType)) {
				return false;
			}
		}
		return true;
	}

	public static boolean isStartOfNewStatement(String text, String prefix) {
		String javaTextWithoutPrefix = text.substring(0, text.length()
				- prefix.length());

		if ("".equals(javaTextWithoutPrefix.trim())
				|| DefaultCompletionProcessor.START_OF_NEW_JAVA_STATEMENT
						.matcher(javaTextWithoutPrefix).matches()) {
			return true;
		}
		return false;
	}
}
