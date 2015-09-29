/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.test.hql;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.Session;
import org.hibernate.testing.junit4.BaseNonConfigCoreFunctionalTestCase;
import org.junit.Test;

/**
 * Test originally written to help verify/diagnose HHH-10125
 *
 * @author Steve Ebersole
 */
public class MapFunctionExpressionsTest extends BaseNonConfigCoreFunctionalTestCase {

	@Test
	public void testMapKeyExpressionInWhere() {
		// NOTE : JPA requires that an alias be used in the key() expression.  Hibernate allows
		//		path or alias.

		Session s = openSession();
		s.getTransaction().begin();
		// JPA form
		s.createQuery( "from Contact c join c.addresses a where key(a) = 'HOME'" ).list();
		// Hibernate additional form
		s.createQuery( "from Contact c where key(c.addresses) = 'HOME'" ).list();
		s.getTransaction().commit();
		s.close();
	}

	@Test
	public void testMapKeyExpressionInSelect() {
		// NOTE : JPA requires that an alias be used in the key() expression.  Hibernate allows
		//		path or alias.

		Session s = openSession();

		AddressType home = new AddressType();
		home.id = 1;
		home.name = "HOME";
		s.persist( home );

		Address homeAddress = new Address();
		homeAddress.id = 1;
		homeAddress.city = "Hamburg";
		homeAddress.street = "Main Street 1";
		s.persist( homeAddress );

		Contact contact = new Contact();
		contact.id = 1;
		contact.name = "My Contact";
		contact.addresses.put( home, homeAddress );
		s.persist( contact );

		s.getTransaction().begin();
		// JPA form
		List<AddressType> addressTypes = s.createQuery( "select key(a) from Contact c join c.addresses a" ).list();

		// Fails with a ClassCastException: Address is returned instead of AddressType
		AddressType addressType = addressTypes.iterator().next();

		// Hibernate additional form
		s.createQuery( "select key(c.addresses) from Contact c" ).list();
		s.getTransaction().commit();
		s.close();
	}

	@Test
	public void testMapValueExpressionInSelect() {
		Session s = openSession();
		s.getTransaction().begin();
		s.createQuery( "select value(a) from Contact c join c.addresses a" ).list();
		s.createQuery( "select value(c.addresses) from Contact c" ).list();
		s.getTransaction().commit();
		s.close();
	}

	@Override
	protected Class[] getAnnotatedClasses() {
		return new Class[] { Address.class, AddressType.class, Contact.class };
	}

	@Entity
	public static class AddressType {
		@Id
		public Integer id;
		String name;
	}

	@Entity(name = "Address")
	@Table(name = "address")
//	@Embeddable
	public static class Address {
		@Id
		public Integer id;
		String street;
		String city;
	}

	@Entity(name = "Contact")
	@Table(name = "contact")
	public static class Contact {
		@Id
		public Integer id;
		String name;

		@OneToMany
		Map<AddressType, Address> addresses = new HashMap<AddressType, Address>();
	}
}
