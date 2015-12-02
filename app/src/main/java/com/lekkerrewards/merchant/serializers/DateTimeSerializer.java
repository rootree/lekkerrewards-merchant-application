package com.lekkerrewards.merchant.serializers;

import com.activeandroid.serializer.TypeSerializer;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public final class DateTimeSerializer extends TypeSerializer {

    public final static String DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

    public Class<?> getDeserializedType() {
        return DateTime.class;
    }

    public Class<?> getSerializedType() {
        return String.class;
    }

    public String serialize(Object data) {
        if (data == null) {
            return null;
        }
        DateTimeFormatter df = DateTimeFormat.forPattern(DATETIME_FORMAT);
        String dt = df.print((DateTime) data);
        return dt;
    }

    public DateTime deserialize(Object data) {
        if (data == null) {
            return null;
        }
        DateTime dt = DateTime.parse((String) data, DateTimeFormat.forPattern(DATETIME_FORMAT));
        return dt;
    }
}
