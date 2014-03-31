package com.owlike.genson.ext.guava;

import com.google.common.base.Optional;
import com.owlike.genson.*;
import com.owlike.genson.annotation.HandleNull;
import com.owlike.genson.reflect.TypeUtil;
import com.owlike.genson.stream.ObjectReader;
import com.owlike.genson.stream.ObjectWriter;
import com.owlike.genson.stream.ValueType;

import java.io.IOException;
import java.lang.reflect.Type;

@HandleNull
public class OptionalConverter<T> implements Converter<Optional<T>> {
    static class OptionalConverterFactory implements Factory<Converter<Optional<Object>>> {

        @Override
        public Converter<Optional<Object>> create(Type type, Genson genson) {
            Type typeOfValue = TypeUtil.typeOf(0, type);

            return new OptionalConverter<Object>(genson.provideConverter(typeOfValue));
        }
    }

    private final Converter<T> valueConverter;

    public OptionalConverter(Converter<T> valueConverter) {
        this.valueConverter = valueConverter;
    }

    @Override
    public void serialize(Optional<T> object, ObjectWriter writer, Context ctx) throws Exception {
        if (object == null || object.isPresent()) {
            valueConverter.serialize(object.get(), writer, ctx);
        } else writer.writeNull();
    }

    @Override
    public Optional<T> deserialize(ObjectReader reader, Context ctx) throws Exception {
        if (ValueType.NULL.equals(reader.getValueType())) {
            return Optional.absent();
        }
        return Optional.of(valueConverter.deserialize(reader, ctx));
    }
}
