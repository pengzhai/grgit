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

class OpSyntaxUtil {
	private OpSyntaxUtil() {
		throw new AssertionError('Cannot instantiate this class.')
	}

	static def tryOp(Class service, Map supportedOps, Object[] classArgs, String methodName, Object[] methodArgs) {
		if (methodName in supportedOps && methodArgs.size() < 2) {
			def op = supportedOps[methodName].newInstance(classArgs)
			def config = methodArgs.size() == 0 ? [:] : methodArgs[0]
			ConfigureUtil.configure(op, config)
			return op.call()
		} else {
			throw new MissingMethodException(methodName, service, methodArgs)
		}
	}
}
