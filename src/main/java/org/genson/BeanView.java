package org.genson;

/**
 * Interface to be implemented by classes who want to act as a kind of view on object of type T
 * during serialization and deserializaiton.
 * 
 * To understand what a BeanView is we must first understand one of the problems it is intended to
 * solve. Imagine you store some business objects in a cache and you have internal and external
 * webservices that all return a different json representation of those objects (filtered properties
 * and even transformed properties). The external webservices can't return the same representation
 * as the internal ones as the object may contain some confidential data.
 * <ul>
 * Usually you have two choices :
 * <li>Use a different instance of the json library in each webservice and configure them with
 * custom Serializers/Deserializers.</li>
 * <li>Or use DTOs that will act as a "View of your Model". You will have to copy the data from the
 * cached objects to the DTOs and serialize them. As result your cache has lost some of its
 * interest.</li>
 * </ul>
 * <p>
 * The BeanView tries to solve this kind of problem by taking the second approach. Indeed
 * implementations of BeanView will act as a stateless bean that will extract data (and could apply
 * transformations) during serialization and as a factory and data aggregator during
 * deserialization. The parameterized type T will correspond to the type of the objects on which
 * this view can be applied. All the methods from the view respecting the conventional JavaBean
 * structure will be used (getters to extract data, setters to aggregate and static methods
 * annotated with {@link org.genson.annotation.Creator Creator} as factory methods). Except that the
 * getters will take an argument of type T (from which to extract the data), and the setter two
 * arguments, the value (can be a complex object, in that case Genson will try to deserialize the
 * current value into that type) and T object in which to set the data. Lets have a look at this
 * example to better understand how it works.
 * 
 * <pre>
 * public static class Person {
 * 	private String lastName;
 * 	String name;
 * 	int birthYear;
 * 	String thisFieldWontBeSerialized;
 * 
 * 	Person(String lastName) {
 * 		this.lastName = lastName;
 * 	}
 * 
 * public String getLastName() {
 * 	return lastName;
 * }
 * 
 * // instead of serializing and deserializing Person based on the fields and methods it contains those
 * // and only those from the BeanView will be used
 * public static class ViewOfPerson implements BeanView&lt;Person&gt; {
 * 	public ViewOfPerson() {
 * 	}
 * 
 * 	// This method will be called to create an instance of Person instead of using the constructor
 * 	// or annotated @Creator method from Person
 * 	&#064;Creator
 * 	public static Person createNewPerson(String lastName) {
 * 		return new Person(lastName);
 * 	}
 * 
 * 	public String getLastName(Person p) {
 * 		return p.getLastName();
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
 * Implementations of BeanView must be stateless, thread safe and have a default no arg constructor.
 * BeanViews will be applied at <u>runtime before the standard Converter</u>. If a view for the
 * current type is present in the context it will be used instead of the corresponding Converter. If
 * you want to understand how it works behind the scene you can have a look at
 * {@link org.genson.convert.BeanViewConverter BeanViewConverter} and
 * {@link org.genson.reflect.BeanViewDescriptorProvider BeanViewDescriptorProvider}.
 * 
 * @see org.genson.convert.BeanViewConverter BeanViewConverter
 * @see org.genson.reflect.BeanViewDescriptorProvider BeanViewDescriptorProvider
 * @see org.genson.annotation.Creator Creator
 * @see org.genson.annotation.JsonProperty JsonProperty
 * 
 * @param <T> the type of objects on which this view will be applied.
 */
public interface BeanView<T> {

}