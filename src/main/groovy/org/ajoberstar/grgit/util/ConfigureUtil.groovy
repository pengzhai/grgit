/*
 * Copyright 2012-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ajoberstar.grgit.util

/**
 *
 * @since 0.7.0
 * @author Andrew Oberstar
 */
class ConfigureUtil {
	private ConfigureUtil() {
		throw new AssertionError('Cannot instantiate this class.')
	}

	static configure(Object object, Map map) {
		map.each { key, value ->
			object[key] = value
		}
	}

	static configure(Object object, Closure closure) {
		Object originalDelegate = closure.delegate
		closure.delegate = object
		closure.call()
		closure.delegate = originalDelegate
	}
}
