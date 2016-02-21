/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.proxy.pojo.bytebuddy;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Set;

import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.internal.util.ReflectHelper;
import org.hibernate.proxy.HibernateProxy;
import org.hibernate.proxy.ProxyFactory;
import org.hibernate.type.CompositeType;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.NamingStrategy;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;
import net.bytebuddy.dynamic.scaffold.subclass.ConstructorStrategy;
import net.bytebuddy.implementation.FixedValue;
import net.bytebuddy.implementation.InvocationHandlerAdapter;
import net.bytebuddy.implementation.MethodCall;
import net.bytebuddy.matcher.ElementMatchers;
import net.bytebuddy.utility.RandomString;


/**
 * @author Gunnar Morling
 *
 */
public class ByteBuddyProxyFactory implements ProxyFactory {

	private Class<?> persistentClass;
	private String entityName;
	private Set<Class> interfaces;
	private Method getIdentifierMethod;
	private Method setIdentifierMethod;
	private CompositeType componentIdType;
	private boolean overridesEquals;

	@Override
	public void postInstantiate(String entityName, Class persistentClass, Set<Class> interfaces, Method getIdentifierMethod, Method setIdentifierMethod,
			CompositeType componentIdType) throws HibernateException {
		this.entityName = entityName;
		this.persistentClass = persistentClass;
		this.interfaces = interfaces;
		this.getIdentifierMethod = getIdentifierMethod;
		this.setIdentifierMethod = setIdentifierMethod;
		this.componentIdType = componentIdType;
		this.overridesEquals = ReflectHelper.overridesEquals( persistentClass );
	}

	@Override
	public HibernateProxy getProxy(Serializable id, SessionImplementor session) throws HibernateException {
		ByteBuddyLazyInitializer lazyInitializer = new ByteBuddyLazyInitializer(
				entityName, persistentClass, interfaces.toArray( new Class<?>[0]), id, getIdentifierMethod, setIdentifierMethod, componentIdType, session, overridesEquals
		);

		Constructor<?> superConstructor = null;
		Class<?> superClass = ( persistentClass.isInterface() || interfaces.size() > 1 ) ? Object.class : persistentClass;

		try {
			superConstructor = superClass.getDeclaredConstructor();
		}
		catch (Exception e) {
			throw new HibernateException( e );
		}


		String pakkage = null;

		if ( superClass != Object.class ) {
			pakkage = superClass.getPackage().getName();
		}
		else if ( interfaces.size() > 1 ) {
			for ( Class<?> clazz : interfaces ) {
				if ( clazz != HibernateProxy.class ) {
					pakkage = clazz.getPackage().getName();
					break;
				}
			}
		}
		else {
			pakkage = "org.hibernate.proxy";
		}

		final String pakkage1 = pakkage;

		Class<?> proxy = new ByteBuddy()
					.with( new NamingStrategy.AbstractBase() {

						@Override
						protected String name(TypeDescription superClass) {
							RandomString rs = new RandomString();

							return pakkage1 + "." + superClass.getSimpleName() + "$HibernateProxy$" + rs.nextString();
						}
					} )
					.subclass( superClass, ConstructorStrategy.Default.NO_CONSTRUCTORS )
					. define( superConstructor )
					.intercept(
							MethodCall.invoke( superConstructor ).onSuper()
					)
					.implement( new ArrayList<Type>( interfaces ) )
					.method( ElementMatchers.any() )
						.intercept( InvocationHandlerAdapter.of( lazyInitializer ) )
					.method( ElementMatchers.named( "getHibernateLazyInitializer" ) )
						.intercept( FixedValue.value( lazyInitializer ) )
					.make()
					.load( persistentClass.getClassLoader(), ClassLoadingStrategy.Default.INJECTION )
					.getLoaded();

		Constructor<?> declaredConstructor;
		try {
			declaredConstructor = proxy.getDeclaredConstructor();
			declaredConstructor.setAccessible( true );
		}
		catch (Exception e) {
			throw new HibernateException( e );
		}

		try {
			@SuppressWarnings("unchecked")
			HibernateProxy instance = (HibernateProxy) declaredConstructor.newInstance();
			return instance;
		}
		catch (Exception e) {
			throw new HibernateException( e );
		}
	}
}
