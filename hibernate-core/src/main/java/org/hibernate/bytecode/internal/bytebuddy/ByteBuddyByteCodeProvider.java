/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.bytecode.internal.bytebuddy;

import org.hibernate.bytecode.spi.BytecodeProvider;
import org.hibernate.bytecode.spi.ProxyFactoryFactory;
import org.hibernate.bytecode.spi.ReflectionOptimizer;

/**
 * @author Gunnar Morling
 *
 */
public class ByteBuddyByteCodeProvider implements BytecodeProvider {

	@Override
	public ProxyFactoryFactory getProxyFactoryFactory() {
		return new ByteBuddyProxyFactoryFactory();
	}

	@Override
	public ReflectionOptimizer getReflectionOptimizer(Class clazz, String[] getterNames, String[] setterNames, Class[] types) {
		return null;
	}

}
