/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.jboss.shrinkwrap.resolver.example;

/**
 * A component for creating personal greetings.
 */
public class Greeter {

	public String createGreeting(String name) {
		return "Hello, " + name + "!";
	}
}
