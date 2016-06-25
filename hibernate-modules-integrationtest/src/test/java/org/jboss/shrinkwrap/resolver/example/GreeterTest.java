/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.jboss.shrinkwrap.resolver.example;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.hibernate.Session;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.descriptor.api.Descriptors;
import org.jboss.shrinkwrap.descriptor.api.persistence21.PersistenceDescriptor;
import org.jboss.shrinkwrap.descriptor.api.persistence21.PersistenceUnitTransactionType;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class GreeterTest {

	@Deployment
	public static WebArchive createDeployment() {
		return ShrinkWrap.create( WebArchive.class )
				.addClass( Kryptonite.class )
				.addAsWebInfResource( EmptyAsset.INSTANCE, "beans.xml" )
				.addAsResource( new StringAsset( persistenceXml().exportAsString() ), "META-INF/persistence.xml" );
	}

	private static PersistenceDescriptor persistenceXml() {
		return Descriptors.create( PersistenceDescriptor.class )
					.version( "2.1" )
					.createPersistenceUnit()
						.name( "primary" )
						.transactionType( PersistenceUnitTransactionType._JTA )
						.jtaDataSource( "java:jboss/datasources/ExampleDS" )
						.getOrCreateProperties()
							.createProperty().name( "jboss.as.jpa.providerModule" ).value( "org.hibernate:5.1" ).up()
							.createProperty().name( "jboss.as.jpa.adapterModule" ).value( "org.hibernate.jipijapa-hibernate5:5.1" ).up()
							.createProperty().name( "hibernate.hbm2ddl.auto" ).value( "create-drop" ).up()
					.up().up();
	}

	@PersistenceContext
	private EntityManager entityManager;

	@Test
	public void shouldUseHibernateOrm51() {
		Session session = entityManager.unwrap( Session.class );

		Kryptonite kryptonite1 = new Kryptonite();
		kryptonite1.id = 1L;
		kryptonite1.description = "Some Kryptonite";
		session.persist( kryptonite1 );

		Kryptonite kryptonite2 = new Kryptonite();
		kryptonite2.id = 2L;
		kryptonite2.description = "Some more Kryptonite";
		session.persist( kryptonite2 );

		session.flush();
		session.clear();

		// multiLoad only introduced in 5.1
		List<Kryptonite> loaded = session.byMultipleIds( Kryptonite.class )
			.multiLoad( 1L, 2L );

		assertThat( loaded.size(), equalTo( 2 ) );
	}
}
