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
package org.ajoberstar.grgit.operation

import spock.lang.Specification

import org.ajoberstar.grgit.Grgit
import org.ajoberstar.grgit.Person
import org.ajoberstar.grgit.Repository
import org.ajoberstar.grgit.Status
import org.ajoberstar.grgit.exception.GrgitException
import org.ajoberstar.grgit.service.RepositoryService

import org.eclipse.jgit.api.Git

import org.junit.Rule
import org.junit.rules.TemporaryFolder

class RevertOpSpec extends Specification {
	@Rule TemporaryFolder tempDir = new TemporaryFolder()

	RepositoryService grgit
	List commits = []

	def setup() {
		File repoDir = tempDir.newFolder('repo')
		Git git = Git.init().setDirectory(repoDir).call()
		grgit = Grgit.open(repoDir)

		5.times {
			repoFile("${it}.txt") << "1"
			grgit.add(patterns:['.'])
			commits << grgit.commit(message:'Test', all: true)
		}
	}

	def 'revert with no commits does nothing'() {
		when:
		grgit.revert()
		then:
		grgit.log().size() == 5
	}

	def 'revert with commits removes associated changes'() {
		when:
		grgit.revert(commits:[1, 3].collect { commits[it].id })
		then:
		grgit.log().size() == 7
		repoFile('.').listFiles().collect { it.name }.findAll { !it.startsWith('.') } as Set == [0, 2, 4].collect { "${it}.txt" } as Set
	}

	private File repoFile(String path, boolean makeDirs = true) {
		def file = new File(grgit.repository.rootDir, path)
		if (makeDirs) file.parentFile.mkdirs()
		return file
	}
}
