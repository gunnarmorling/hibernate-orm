/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * Copyright (c) 2014, Red Hat Inc. or third-party contributors as
 * indicated by the @author tags or express copyright attribution
 * statements applied by the authors.  All third-party contributions are
 * distributed under license by Red Hat Inc.
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU
 * Lesser General Public License, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this distribution; if not, write to:
 * Free Software Foundation, Inc.
 * 51 Franklin Street, Fifth Floor
 * Boston, MA  02110-1301  USA
 */
package org.hibernate.engine.spi;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.hibernate.LockMode;
import org.hibernate.engine.internal.DefaultEntityEntry;
import org.hibernate.engine.internal.StatefulPersistenceContext;
import org.junit.Test;

/**
 * Tests for setting and getting the enum/boolean values stored in the compressed state int.
 *
 * @author Gunnar Morling
 */
public class DefaultEntityEntryTest {

	@Test
	public void packedAttributesAreSetByConstructor() {
		DefaultEntityEntry entityEntry = createEntityEntry();

		assertEquals( LockMode.OPTIMISTIC, entityEntry.getLockMode() );
		assertEquals( Status.MANAGED, entityEntry.getStatus() );
		assertEquals( true, entityEntry.isExistsInDatabase() );
		assertEquals( true, entityEntry.isBeingReplicated() );
		assertEquals( true, entityEntry.isLoadedWithLazyPropertiesUnfetched() );
	}

	@Test
	public void testLockModeCanBeSetAndDoesNotAffectOtherPackedAttributes() {
		// Given
		DefaultEntityEntry entityEntry = createEntityEntry();

		assertEquals( LockMode.OPTIMISTIC, entityEntry.getLockMode() );
		assertEquals( Status.MANAGED, entityEntry.getStatus() );
		assertEquals( true, entityEntry.isExistsInDatabase() );
		assertEquals( true, entityEntry.isBeingReplicated() );
		assertEquals( true, entityEntry.isLoadedWithLazyPropertiesUnfetched() );

		// When
		entityEntry.setLockMode( LockMode.PESSIMISTIC_READ );

		// Then
		assertEquals( LockMode.PESSIMISTIC_READ, entityEntry.getLockMode() );
		assertEquals( Status.MANAGED, entityEntry.getStatus() );
		assertEquals( true, entityEntry.isExistsInDatabase() );
		assertEquals( true, entityEntry.isBeingReplicated() );
		assertEquals( true, entityEntry.isLoadedWithLazyPropertiesUnfetched() );
	}

	@Test
	public void testStatusCanBeSetAndDoesNotAffectOtherPackedAttributes() {
		// Given
		DefaultEntityEntry entityEntry = createEntityEntry();

		// When
		entityEntry.setStatus( Status.DELETED );

		// Then
		assertEquals( LockMode.OPTIMISTIC, entityEntry.getLockMode() );
		assertEquals( Status.DELETED, entityEntry.getStatus() );
		assertEquals( true, entityEntry.isExistsInDatabase() );
		assertEquals( true, entityEntry.isBeingReplicated() );
		assertEquals( true, entityEntry.isLoadedWithLazyPropertiesUnfetched() );
	}

	@Test
	public void testPostDeleteSetsStatusAndExistsInDatabaseWithoutAffectingOtherPackedAttributes() {
		// Given
		DefaultEntityEntry entityEntry = createEntityEntry();

		// When
		entityEntry.postDelete();

		// Then
		assertEquals( LockMode.OPTIMISTIC, entityEntry.getLockMode() );
		assertEquals( Status.GONE, entityEntry.getStatus() );
		assertEquals( false, entityEntry.isExistsInDatabase() );
		assertEquals( true, entityEntry.isBeingReplicated() );
		assertEquals( true, entityEntry.isLoadedWithLazyPropertiesUnfetched() );
	}

	@Test
	public void testSerializationAndDeserializationKeepCorrectPackedAttributes() throws Exception {
		DefaultEntityEntry entityEntry = createEntityEntry();

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream( baos );
		entityEntry.serialize(oos);
		oos.flush();

		InputStream is = new ByteArrayInputStream( baos.toByteArray() );
		EntityEntry deserializedEntry = DefaultEntityEntry.deserialize(new ObjectInputStream( is ), new StatefulPersistenceContext( null ) );

		assertEquals( LockMode.OPTIMISTIC, deserializedEntry.getLockMode() );
		assertEquals( Status.MANAGED, deserializedEntry.getStatus() );
		assertEquals( true, deserializedEntry.isExistsInDatabase() );
		assertEquals( true, deserializedEntry.isBeingReplicated() );
		assertEquals( true, deserializedEntry.isLoadedWithLazyPropertiesUnfetched() );
	}

	private DefaultEntityEntry createEntityEntry() {
		return new DefaultEntityEntry(
				Status.MANAGED,                        // status
				new Object[]{},                        // loadedState
				1L,                                    // rowId
				42L,                                   // id
				23L,                                   // version
				LockMode.OPTIMISTIC,                   // lockMode
				true,                                  // existsInDatabase
				null,                                  // persister
				true,                                  // disableVersionIncrement
				true,                                  // lazyPropertiesAreUnfetched
				new StatefulPersistenceContext( null ) // persistenceContext)
		);
	}
}
