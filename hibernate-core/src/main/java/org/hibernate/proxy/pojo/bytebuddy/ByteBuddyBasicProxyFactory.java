/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.proxy.pojo.bytebuddy;

import static net.bytebuddy.matcher.ElementMatchers.isGetter;
import static net.bytebuddy.matcher.ElementMatchers.isSetter;

import java.lang.reflect.Method;

import org.hibernate.HibernateException;
import org.hibernate.bytecode.spi.BasicProxyFactory;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.description.modifier.Visibility;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.dynamic.DynamicType.Builder;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;
import net.bytebuddy.implementation.FieldAccessor;
/**
 * @author Gunnar Morling
 */
public class ByteBuddyBasicProxyFactory implements BasicProxyFactory {

	private final Class<?> superType;

	public ByteBuddyBasicProxyFactory(Class<?> superType) {
		this.superType = superType;
	}

	@Override
	public Object getProxy() {
		DynamicType.Builder<?> builder = new ByteBuddy()
			.subclass( superType.isInterface() ? Object.class : superType );

		if ( superType.isInterface() ) {
			builder = builder.implement( superType );
		}

		builder = builder.method( isGetter().or( isSetter() ) )
			.intercept( FieldAccessor.ofBeanProperty() );

		builder = fields( builder );

		try {
			return builder.make()
				.load( superType.getClassLoader(), ClassLoadingStrategy.Default.INJECTION )
				.getLoaded()
				.newInstance();
		}
		catch (Exception e) {
			throw new HibernateException( e );
		}
	}

	private Builder<?> fields(DynamicType.Builder<?> builder) {
		for ( Method method : superType.getDeclaredMethods() ) {
			final boolean hasGetterSignature = method.getParameterTypes().length == 0
					&& method.getReturnType() != null;

			String name = method.getName();

			if ( name.startsWith( "get" ) && hasGetterSignature ) {
				final String propName = name.substring( 3, 4 ).toLowerCase() + name.substring( 4 );
				builder = builder.defineField( propName, method.getReturnType(), Visibility.PRIVATE );
			}
			else if ( name.startsWith( "is" ) && hasGetterSignature ) {
				final String propName = name.substring( 2, 3 ).toLowerCase() + name.substring( 3 );
				builder = builder.defineField( propName, method.getReturnType(), Visibility.PRIVATE );
			}
		}

		return builder;
	}
}
