package org.genson;

/**
 * To understand what a BeanView is we must first understand one of the problems it is intended to
 * solve. Imagine you store some business objects in a cache and you have internal and external
 * webservices that all return a different json representation of those objects (filtered properties
 * and even transformed properties). The external webservices can't return the same representation
 * as the internal ones as the object may contain some confidential data. The problem is that
 * actually in the existing frameworks there is no distinction between model objects and view
 * objects, so it is up to you to do that.
 * <ul>
 * Usually you have two choices :
 * <li>Use a different instance of the json library in each webservice and configure them with
 * custom Serializers/Deserializers.</li>
 * <li>Or (usually I have seen this one) create other objects that are a "View of your Model" copy
 * the data in it and serialize them (the cache has lost all its interest and you will end up with
 * tons of dummy Beans!!!)</li>
 * </ul>
 * <p>
 * The BeanView tries to solve this kind of problem by taking the second approach. Indeed
 * implementations of BeanView will act as a stateless bean that will extract data (and could apply
 * transformations) during serialization and as a factory and data aggregator during
 * deserialization. The parameterized type T will correspond to the type of the objects on which
 * this view can be applied. All the methods from the view respecting the conventional JavaBean
 * structure will be used (getters to extract data, setters to aggregate and static methods
 * annotated with {@link org.genson.annotation.Creator Creator} as factory methods). Except that the getters will take an argument
 * of type T (from which to extract the data), and the setter two arguments, the value (can be a
 * complex object, in that case Genson will try to deserialize the current value into that type) and
 * T object in which to set the data. Lets have a look at this example to better understand how it
 * works.
 * 
 * <pre>
 * public static class Person {
 *  // this field value will not be set directly, instead it will be passed to the constructor 
 *  // called in the creator method from the BeanView
 * 	private String civility;
 * 	String name;
 * 	int birthYear;
 * 	String thisFieldWontBeSerialized;
 * 
 * 	Person(String civility) {
 * 		this.civility = civility;
 * 	}
 * 
 * public String getCivility() {
 * 	return civility;
 * }
 * 
 * // instead of serializing and deserializing Person based on the fields and methods it contains those
 * // and only those from the BeanView will be used
 * public static class ViewOfPerson implements BeanView&lt;Person&gt; {
 * 	public ViewOfPerson() {
 * 	}
 * 
 * 	// This method will be called to create an instance of Person instead of using the creators
 * 	// (constructor or method)
 * 	// from Person.
 * 	&#064;Creator
 * 	public static Person createNewPerson(String gender) {
 * 		String civility = &quot;M&quot;.equalsIgnoreCase(gender) ? &quot;Mr&quot;
 * 				: &quot;F&quot;.equalsIgnoreCase(gender) ? &quot;UNKNOWN&quot; : &quot;&quot;;
 * 		return new Person(civility);
 * 	}
 * 
 * 	public String getGender(Person p) {
 * 		return &quot;Mr&quot;.equalsIgnoreCase(p.getCivility()) ? &quot;M&quot; : &quot;Mrs&quot;.equalsIgnoreCase(p.getCivility()) ? &quot;F&quot;
 * 				: &quot;UNKNOWN&quot;;
 * 	}
 * 
 * 	public @JsonProperty(&quot;name&quot;)
 * 	String getNameOf(Person p) {
 * 		return p.name;
 * 	}
 * 
 * 	// here we will transform the birth year of the person into its age and change the serialized
 * 	// name from &quot;birthYear&quot; to &quot;age&quot;
 * 	public int getAge(Person p) {
 * 		return GregorianCalendar.getInstance().get(Calendar.YEAR) - p.birthYear;
 * 	}
 * 
 * 	public void setName(String name, Person p) {
 * 		p.name = name;
 * 	}
 * 
 * 	// here it will match the property named &quot;age&quot; from the json stream and transform it into birth
 * 	// year of Person
 * 	&#064;JsonProperty(&quot;age&quot;)
 * 	public void setBirthYear(int personBirthYear, Person p) {
 * 		p.birthYear = GregorianCalendar.getInstance().get(Calendar.YEAR) - personBirthYear;
 * 	}
 * }
 * </pre>
 * 
 * <p>
 * Implementations of BeanView must be thread safe and have a default no arg constructor. BeanViews
 * will be applied at <u>runtime before the standard Bean Serializer/Deserializer</u>. If a view for
 * the current type is present in the context it will be used instead of the Bean
 * Serializer/Deserializer. However <u>BeanViews will not be applied on types having a custom
 * Serializer/Deserializer</u> (or if a Factory provides one).
 * <p>
 * If you want to understand how it works behind the scene you can have a look at
 * {@link org.genson.serialization.BeanViewSerializer BeanViewSerializer} and {@link org.genson.reflect.BeanViewDescriptorProvider BeanViewDescriptorProvider}.
 * 
 * @see org.genson.serialization.BeanViewSerializer BeanViewSerializer
 * @see org.genson.deserialization.BeanViewDeserializer BeanViewDeserializer
 * @see org.genson.reflect.BeanViewDescriptorProvider BeanViewDescriptorProvider
 * @see org.genson.annotation.Creator Creator
 * @see org.genson.annotation.JsonProperty JsonProperty
 * 
 * @param <T> the type of objects on which this view will be applied.
 */
public interface BeanView<T> {

}