package io.amplicode.pja.rasupport;

import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.ErrorResponse;
import org.springframework.web.ErrorResponseException;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.*;
import java.util.function.Function;

/**
 * Similar to {@link MethodArgumentNotValidException} and {@link BindException}.
 * But extends from RuntimeException, as it's supposed to be thrown inside of controller handler body.
 */
public class PatchValidationException extends ErrorResponseException {

	private final BindingResult bindingResult;

	public PatchValidationException(BindingResult bindingResult) {
		super(HttpStatus.BAD_REQUEST, ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Invalid patch request content."), null);
		Assert.notNull(bindingResult, "BindingResult must not be null");
		this.bindingResult = bindingResult;
	}

	@Override
	public String getMessage() {
		StringBuilder sb = new StringBuilder("Validation failed ");
		if (bindingResult.getErrorCount() > 1) {
			sb.append(" with ").append(bindingResult.getErrorCount()).append(" errors");
		}
		sb.append(": ");
		for (ObjectError error : bindingResult.getAllErrors()) {
			sb.append('[').append(error).append("] ");
		}
		return sb.toString();
	}

	@Override
	public Object[] getDetailMessageArguments() {
		return new Object[] {errorsToStringList(bindingResult.getGlobalErrors()), errorsToStringList(bindingResult.getFieldErrors())};
	}

	@Override
	public Object[] getDetailMessageArguments(MessageSource messageSource, Locale locale) {
		return new Object[] {
			errorsToStringList(bindingResult.getGlobalErrors(), messageSource, locale),
			errorsToStringList(bindingResult.getFieldErrors(), messageSource, locale)
		};
	}

	/**
	 * Resolve global and field errors to messages with the given
	 * {@link MessageSource} and {@link Locale}.
	 * @return a Map with errors as key and resolved messages as value
	 * @since 6.0.3
	 */
	public Map<ObjectError, String> resolveErrorMessages(MessageSource messageSource, Locale locale) {
		Map<ObjectError, String> map = new LinkedHashMap<>();
		addMessages(map, bindingResult.getGlobalErrors(), messageSource, locale);
		addMessages(map, bindingResult.getFieldErrors(), messageSource, locale);
		return map;
	}

	private static void addMessages(
		Map<ObjectError, String> map, List<? extends ObjectError> errors,
		MessageSource messageSource, Locale locale) {

		List<String> messages = errorsToStringList(errors, messageSource, locale);
		for (int i = 0; i < errors.size(); i++) {
			map.put(errors.get(i), messages.get(i));
		}
	}

	/**
	 * Convert each given {@link ObjectError} to a String in single quotes, taking
	 * either the error's default message, or its error code.
	 * @since 6.0
	 */
	public static List<String> errorsToStringList(List<? extends ObjectError> errors) {
		return errorsToStringList(errors, error ->
			error.getDefaultMessage() != null ? error.getDefaultMessage() : error.getCode());
	}

	/**
	 * Variant of {@link #errorsToStringList(List)} that uses a
	 * {@link MessageSource} to resolve the message code of the error, or fall
	 * back on the error's default message.
	 * @since 6.0
	 */
	public static List<String> errorsToStringList(
		List<? extends ObjectError> errors, @Nullable MessageSource source, Locale locale) {

		return (source != null ?
			errorsToStringList(errors, error -> source.getMessage(error, locale)) :
			errorsToStringList(errors));
	}

	private static List<String> errorsToStringList(
		List<? extends ObjectError> errors, Function<ObjectError, String> formatter) {

		List<String> result = new ArrayList<>(errors.size());
		for (ObjectError error : errors) {
			String value = formatter.apply(error);
			if (StringUtils.hasText(value)) {
				result.add(error instanceof FieldError fieldError ?
					fieldError.getField() + ": '" + value + "'" : "'" + value + "'");
			}
		}
		return result;
	}
}
