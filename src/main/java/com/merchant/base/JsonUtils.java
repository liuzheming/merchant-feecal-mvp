package com.merchant.base;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer;
import com.merchant.common.exception.ServiceException;
import lombok.NonNull;
import org.apache.commons.lang3.StringUtils;

import java.io.InputStream;
import java.io.Reader;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class JsonUtils {
    private static final String LOCAL_DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    static {
        OBJECT_MAPPER.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        // 忽略空 Bean 转字符串的错误
        OBJECT_MAPPER.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        // 忽略反序列化时，对象不存在对应属性的错误
        OBJECT_MAPPER.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        OBJECT_MAPPER.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        JavaTimeModule timeModule = new JavaTimeModule();
        timeModule.addSerializer(LocalDate.class, new LocalDateSerializer(DateTimeFormatter.ISO_LOCAL_DATE));
        timeModule.addDeserializer(LocalDate.class, new LocalDateDeserializer(DateTimeFormatter.ISO_LOCAL_DATE));
        timeModule.addSerializer(LocalTime.class, new LocalTimeSerializer(DateTimeFormatter.ISO_LOCAL_TIME));
        timeModule.addDeserializer(LocalTime.class, new LocalTimeDeserializer(DateTimeFormatter.ISO_LOCAL_TIME));
        timeModule.addSerializer(LocalDateTime.class,
            new LocalDateTimeSerializer(DateTimeFormatter.ofPattern(LOCAL_DATE_TIME_PATTERN)));
        timeModule.addDeserializer(LocalDateTime.class,
            new LocalDateTimeDeserializer(DateTimeFormatter.ofPattern(LOCAL_DATE_TIME_PATTERN)));
        OBJECT_MAPPER.registerModule(timeModule);
        OBJECT_MAPPER.setDateFormat(new SimpleDateFormat(LOCAL_DATE_TIME_PATTERN));
    }

    private JsonUtils() {
    }

    public static ObjectMapper getObjectMapper() {
        return OBJECT_MAPPER;
    }

    public static <T> String obj2Str(T obj) {
        if (obj == null) {
            return null;
        }
        try {
            return obj instanceof String ? (String) obj : OBJECT_MAPPER.writeValueAsString(obj);
        } catch (Exception e) {
            throw new ServiceException(e);
        }
    }

    public static <T> String obj2PrettyStr(T obj) {
        if (obj == null) {
            return null;
        }
        try {
            return obj instanceof String ? (String) obj : OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
        } catch (Exception e) {
            throw new ServiceException(e);
        }
    }

    public static <T> T str2Obj(String str, Class<T> clazz) {
        if (StringUtils.isEmpty(str) || clazz == null) {
            return null;
        }
        try {
            return clazz.equals(String.class) ? (T) str : OBJECT_MAPPER.readValue(str, clazz);
        } catch (Exception e) {
            throw new ServiceException(e);
        }
    }

    public static <T> T str2Obj(String str, TypeReference<T> typeReference) {
        if (StringUtils.isEmpty(str) || typeReference == null) {
            return null;
        }
        try {
            return (T) (typeReference.getType().equals(String.class) ? str : OBJECT_MAPPER.readValue(str, typeReference));
        } catch (Exception e) {
            throw new ServiceException(e);
        }
    }

    public static JsonNode obj2JsonNode(Object obj) {
        String str = JsonUtils.obj2Str(obj);
        return JsonUtils.str2JsonNode(str);
    }

    public static <T> T jsonNode2Obj(JsonNode jsonNode, Class<T> clz) {
        try {
            return OBJECT_MAPPER.treeToValue(jsonNode, clz);
        } catch (Exception e) {
            throw new ServiceException(e);
        }
    }

	/**
	 * 判断是否存在中文
	 * @param str
	 * @return
	 */
	public static boolean hasChinese(String str) {
		return str.matches(".*[\\u4e00-\\u9fa5].*");
	}

	/**
	 * 判断是否全部为数字
	 * @param str
	 * @return
	 */
	public static boolean isNumeric(String str) {
		return str != null && str.matches("\\d+");
	}


    public static ObjectNode obj2ObjectNode(Object obj) {
        String str = obj2Str(obj);
        return str2ObjectNode(str);
    }

    public static ObjectNode str2ObjectNode(String str) {
        if (StringUtils.isEmpty(str)) {
            return null;
        }
        try {
            JsonNode jsonNode = OBJECT_MAPPER.readTree(str);
            if (jsonNode.isObject()) {
                return (ObjectNode) jsonNode;
            } else {
                throw new ServiceException("str:" + str + " is not a object!");
            }
        } catch (Exception e) {
            throw new ServiceException(e);
        }
    }

    public static JsonNode str2JsonNode(String str) {
        if (StringUtils.isEmpty(str)) {
            return null;
        }
        try {
            return OBJECT_MAPPER.readTree(str);
        } catch (Exception e) {
            throw new ServiceException(e);
        }
    }

    public static JsonNode io2JsonNode(InputStream in) {
        if (in == null) {
            return null;
        }
        try {
            return OBJECT_MAPPER.readTree(in);
        } catch (Exception e) {
            throw new ServiceException(e);
        }
    }

	public static <T> T io2Obj(InputStream in, Class<T> clazz) {
		if (in == null) {
			return null;
		}
		try {
			JsonNode jsonNode = OBJECT_MAPPER.readTree(in);
			return JsonUtils.str2Obj(jsonNode.toString(), clazz);
		} catch (Exception e) {
			throw new ServiceException(e);
		}
	}

    public static JsonNode read2JsonNode(Reader in) {
        if (in == null) {
            return null;
        }
        try {
            return OBJECT_MAPPER.readTree(in);
        } catch (Exception e) {
            throw new ServiceException(e);
        }
    }

    public static <T> T convertValue(@NonNull Object fromValue, @NonNull Class<T> toValueType) {
        try {
            return OBJECT_MAPPER.convertValue(fromValue, toValueType);
        } catch (Exception e) {
            throw new ServiceException(e);
        }
    }

    public static <T> T convertValue(@NonNull Object fromValue, @NonNull TypeReference<T> toValueTypeRef) {
        try {
            return OBJECT_MAPPER.convertValue(fromValue, toValueTypeRef);
        } catch (Exception e) {
            throw new ServiceException(e);
        }
    }

    /**
     * 通过jackson 的javatype 来处理多泛型的转换
     *
     * @param json
     * @param collectionClazz
     * @param elements
     * @param <T>
     * @return
     */
    public static <T> T str2Obj(String json, Class<?> collectionClazz, Class<?>... elements) {
        JavaType javaType = OBJECT_MAPPER.getTypeFactory().constructParametricType(collectionClazz, elements);

        try {
            return OBJECT_MAPPER.readValue(json, javaType);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 格式化
     *
     * @param jsonStr
     * @return
     * @author lizhgb
     * @Date 2015-10-14 下午1:17:35
     * @Modified 2017-04-28 下午8:55:35
     */
    public static String formatJson(String jsonStr) {
        try {
            if (null == jsonStr || "".equals(jsonStr))
                return "";
            StringBuilder sb = new StringBuilder();
            char last = '\0';
            char current = '\0';
            int indent = 0;
            boolean isInQuotationMarks = false;
            for (int i = 0; i < jsonStr.length(); i++) {
                last = current;
                current = jsonStr.charAt(i);
                switch (current) {
                    case '"':
                        if (last != '\\') {
                            isInQuotationMarks = !isInQuotationMarks;
                        }
                        sb.append(current);
                        break;
                    case '{':
                    case '[':
                        sb.append(current);
                        if (!isInQuotationMarks) {
                            sb.append('\n');
                            indent++;
                            addIndentBlank(sb, indent);
                        }
                        break;
                    case '}':
                    case ']':
                        if (!isInQuotationMarks) {
                            sb.append('\n');
                            indent--;
                            addIndentBlank(sb, indent);
                        }
                        sb.append(current);
                        break;
                    case ',':
                        sb.append(current);
                        if (last != '\\' && !isInQuotationMarks) {
                            sb.append('\n');
                            addIndentBlank(sb, indent);
                        }
                        break;
                    default:
                        sb.append(current);
                }
            }

            return sb.toString();
        } catch (Exception e) {
            return jsonStr;
        }
    }

    /**
     * JSON串特殊字符转义
     * Escape quotes, \, /, \r, \n, \b, \f, \t and other control characters
     */
    public static String escape(String s) {
        if (s == null) {
            return null;
        }
        StringBuffer sb = new StringBuffer();
        escape(s, sb);
        return sb.toString();
    }

    static void escape(String s, StringBuffer sb) {
        for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);
            switch (ch) {
                case '"':
                    sb.append("\\\"");
                    break;
                case '\\':
                    sb.append("\\\\");
                    break;
                case '\b':
                    sb.append("\\b");
                    break;
                case '\f':
                    sb.append("\\f");
                    break;
                case '\n':
                    sb.append("\\n");
                    break;
                case '\r':
                    sb.append("\\r");
                    break;
                case '\t':
                    sb.append("\\t");
                    break;
                case '/':
                    sb.append("\\/");
                    break;
                default:
                    // Reference: http://www.unicode.org/versions/Unicode5.1.0/
                    if ((ch >= '\u0000' && ch <= '\u001F') || (ch >= '\u007F' && ch <= '\u009F')
                        || (ch >= '\u2000' && ch <= '\u20FF')) {
                        String ss = Integer.toHexString(ch);
                        sb.append("\\u");
                        for (int k = 0; k < 4 - ss.length(); k++) {
                            sb.append('0');
                        }
                        sb.append(ss.toUpperCase());
                    } else {
                        sb.append(ch);
                    }
            }
        }// for
    }

    /**
     * 添加space
     *
     * @param sb
     * @param indent
     * @author lizhgb
     * @Date 2015-10-14 上午10:38:04
     */
    private static void addIndentBlank(StringBuilder sb, int indent) {
        for (int i = 0; i < indent; i++) {
            sb.append('\t');
        }
    }
}
