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
import spock.lang.Unroll

import org.ajoberstar.grgit.Grgit
import org.ajoberstar.grgit.Person
import org.ajoberstar.grgit.Repository
import org.ajoberstar.grgit.Status
import org.ajoberstar.grgit.exception.GrgitException
import org.ajoberstar.grgit.service.RepositoryService
import org.ajoberstar.grgit.util.JGitUtil

import org.eclipse.jgit.api.Git
import org.eclipse.jgit.api.ListBranchCommand.ListMode

import org.junit.Rule
import org.junit.rules.TemporaryFolder

class CheckoutOpSpec extends Specification {
	@Rule TemporaryFolder tempDir = new TemporaryFolder()

	RepositoryService grgit

	List commits = []

	def setup() {
		File repoDir = tempDir.newFolder('repo')
		Git.init().setDirectory(repoDir).call()
		grgit = Grgit.open(repoDir)

		repoFile('1.txt') << '1'
		commits << grgit.commit(message: 'do', all: true)

		repoFile('1.txt') << '2'
		commits << grgit.commit(message: 'do', all: true)

		grgit.repository.git.branchCreate().with {
			name = 'my-branch'
			delegate.call()
		}

		repoFile('1.txt') << '3'
		commits << grgit.commit(message: 'do', all: true)
	}

	def 'checkout with existing branch and createBranch false works'() {
		when:
		grgit.checkout(branch: 'my-branch')
		then:
		grgit.head() == grgit.resolveCommit('my-branch')
	}

	def 'checkout with existing branch, createBranch true fails'() {
		when:
		grgit.checkout(branch: 'my-branch', createBranch: true)
		then:
		thrown(GrgitException)
	}

	def 'checkout with non-existent branch and createBranch false fails'() {
		when:
		grgit.checkout(branch: 'fake')
		then:
		thrown(GrgitException)
	}

	def 'checkout with non-existent branch and createBranch true works'() {
		when:
		grgit.checkout(branch: 'new-branch', createBranch: true)
		then:
		grgit.repository.git.repo.branch == 'new-branch'
		grgit.head() == grgit.resolveCommit('master')
	}

	def 'checkout with non-existent branch, createBranch true, and startPoint works'() {
		when:
		grgit.checkout(branch: 'new-branch', createBranch: true, startPoint: 'my-branch')
		then:
		grgit.repository.git.repo.branch == 'new-branch'
		grgit.head() == grgit.resolveCommit('my-branch')
	}

	def 'checkout with no branch name and createBranch true fails'() {
		when:
		grgit.checkout(createBranch: true)
		then:
		thrown(IllegalArgumentException)
	}

	private File repoFile(String path, boolean makeDirs = true) {
		def file = new File(grgit.repository.rootDir, path)
		if (makeDirs) file.parentFile.mkdirs()
		return file
	}
}
