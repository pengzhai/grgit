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
import org.ajoberstar.grgit.service.RepositoryService

import org.eclipse.jgit.api.Git

import org.junit.Rule
import org.junit.rules.TemporaryFolder

class CommitOpSpec extends Specification {
	@Rule TemporaryFolder tempDir = new TemporaryFolder()

	RepositoryService grgit

	def setup() {
		File repoDir = tempDir.newFolder('repo')
		Git git = Git.init().setDirectory(repoDir).call()
		git.repo.config.with {
			setString('user', null, 'name', 'Alfred Pennyworth')
			setString('user', null, 'email', 'alfred.pennyworth@wayneindustries.com')
			save()
		}
		grgit = Grgit.open(repoDir)

		repoFile('1.txt') << '1'
		repoFile('2.txt') << '1'
		repoFile('folderA/1.txt') << '1'
		repoFile('folderA/2.txt') << '1'
		repoFile('folderB/1.txt') << '1'
		repoFile('folderC/1.txt') << '1'
		grgit.add(patterns:['.'])
		grgit.repository.git.commit().setMessage('Test').call()
		repoFile('1.txt') << '2'
		repoFile('folderA/1.txt') << '2'
		repoFile('folderA/2.txt') << '2'
		repoFile('folderB/1.txt') << '2'
		repoFile('folderB/2.txt') << '2'
	}

	def 'commit with all false commits changes from index'() {
		given:
		grgit.add(patterns:['folderA'])
		when:
		grgit.commit(message:'Test2')
		then:
		grgit.log().size() == 2
		grgit.status() == new Status(
			[] as Set,
			[] as Set,
			[] as Set,
			['folderB/2.txt'] as Set,
			['1.txt', 'folderB/1.txt'] as Set,
			[] as Set)
	}

	def 'commit with all true commits changes in previously tracked files'() {
		when:
		grgit.commit(message:'Test2', all: true)
		then:
		grgit.log().size() == 2
		grgit.status() == new Status(
			[] as Set,
			[] as Set,
			[] as Set,
			['folderB/2.txt'] as Set,
			[] as Set,
			[] as Set)
	}

	def 'commit amend changes the previous commit'() {
		given:
		grgit.add(patterns:['folderA'])
		when:
		grgit.commit(message:'Test2', amend: true)
		then:
		grgit.log().size() == 1
		grgit.status() == new Status(
			[] as Set,
			[] as Set,
			[] as Set,
			['folderB/2.txt'] as Set,
			['1.txt', 'folderB/1.txt'] as Set,
			[] as Set)
	}

	def 'commit with paths only includes the specified paths from the index'() {
		given:
		grgit.add(patterns:['.'])
		when:
		grgit.commit(message:'Test2', paths:['folderA'])
		then:
		grgit.log().size() == 2
		grgit.status() == new Status(
			['folderB/2.txt'] as Set,
			['1.txt', 'folderB/1.txt'] as Set,
			[] as Set,
			[] as Set,
			[] as Set,
			[] as Set)
	}

	def 'commit without specific committer or author uses repo config'() {
		given:
		grgit.add(patterns:['folderA'])
		when:
		def commit = grgit.commit(message:'Test2')
		then:
		commit.committer == new Person('Alfred Pennyworth', 'alfred.pennyworth@wayneindustries.com')
		commit.author == new Person('Alfred Pennyworth', 'alfred.pennyworth@wayneindustries.com')
		grgit.log().size() == 2
		grgit.status() == new Status(
			[] as Set,
			[] as Set,
			[] as Set,
			['folderB/2.txt'] as Set,
			['1.txt', 'folderB/1.txt'] as Set,
			[] as Set)
	}

	def 'commit with specific committer and author uses those'() {
		given:
		grgit.add(patterns:['folderA'])
		def bruce = new Person('Bruce Wayne', 'bruce.wayne@wayneindustries.com')
		def lucius = new Person('Lucius Fox', 'lucius.fox@wayneindustries.com')
		when:
		def commit = grgit.commit {
			message = 'Test2'
			committer = lucius
			author = bruce
		}
		then:
		commit.committer == lucius
		commit.author == bruce
		grgit.log().size() == 2
		grgit.status() == new Status(
			[] as Set,
			[] as Set,
			[] as Set,
			['folderB/2.txt'] as Set,
			['1.txt', 'folderB/1.txt'] as Set,
			[] as Set)
	}

	private File repoFile(String path, boolean makeDirs = true) {
		def file = new File(grgit.repository.rootDir, path)
		if (makeDirs) file.parentFile.mkdirs()
		return file
	}
}
