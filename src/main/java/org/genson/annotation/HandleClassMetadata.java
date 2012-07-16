package org.genson.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotated Serializer/Deserializer/Converter with @HandleClassMetadata indicate that they will
 * handle @class metadata during serialization and deserialization. By default it is handled by the
 * library in {@link org.genson.convert.ClassMetadataConverter ClassMetadataConverter}. Default
 * converters from {@link org.genson.convert.DefaultConverters DefaultConverters} annotated with @HandleClassMetadata
 * do not serialize type information nor do they use it during deserialization. For security reasons
 * class metadata is disabled by default. To enable it
 * {@link org.genson.Genson.Builder#setWithClassMetadata(boolean)
 * Genson.Builder.setWithClassMetadata(true)}
 * 
 * @see org.genson.convert.ClassMetadataConverter ClassMetadataConverter
 * @see org.genson.Genson.Builder#setWithClassMetadata(boolean)
 *      Genson.Builder.setWithClassMetadata(true)
 * 
 * @author eugen
 * 
 */
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface HandleClassMetadata {

}
