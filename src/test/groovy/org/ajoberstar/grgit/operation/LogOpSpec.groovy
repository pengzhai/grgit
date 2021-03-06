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
import org.ajoberstar.grgit.Repository
import org.ajoberstar.grgit.service.RepositoryService
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.lib.ObjectId
import org.eclipse.jgit.merge.MergeStrategy
import org.junit.Rule
import org.junit.rules.TemporaryFolder

class LogOpSpec extends Specification {
	@Rule TemporaryFolder tempDir = new TemporaryFolder()

	RepositoryService grgit
	List commits = []

	def intToCommit = { ObjectId.toString(commits[it]) }

	def setup() {
		File repoDir = tempDir.newFolder('repo')
		Git git = Git.init().setDirectory(repoDir).call()
		File testFile = new File(repoDir, '1.txt')
		File testFile2 = new File(repoDir, '2.txt')
		testFile << '1\n'
		git.add().addFilepattern(testFile.name).call()
		testFile2 << '2.1\n'
		git.add().addFilepattern(testFile2.name).call()
		commits << git.commit().setMessage('first commit\ntesting').call()
		testFile << '2\n'
		git.add().addFilepattern(testFile.name).call()
		commits << git.commit().setMessage('second commit').call()
		git.checkout().setName(ObjectId.toString(commits[0])).call()
		testFile << '3\n'
		git.add().addFilepattern(testFile.name).call()
		commits << git.commit().setMessage('third commit').call()
		git.checkout().setName('master').call()
		commits << git.merge().include(commits[2]).setStrategy(MergeStrategy.OURS).call().newHead
		testFile << '4\n'
		git.add().addFilepattern(testFile.name).call()
		commits << git.commit().setMessage('fifth commit').call()
		testFile2 << '2.2\n'
		git.add().addFilepattern(testFile2.name).call()
		commits << git.commit().setMessage('sixth commit').call()
		grgit = Grgit.open(repoDir)
	}

	def 'log with no arguments returns all commits'() {
		expect:
		grgit.log().collect { it.id } == [5, 4, 3, 1, 2, 0].collect(intToCommit)
	}

	def 'log with max commits returns that number of commits'() {
		expect:
		grgit.log(maxCommits:2).collect { it.id } == [5, 4].collect(intToCommit)
	}

	def 'log with skip commits does not return the first x commits'() {
		expect:
		grgit.log(skipCommits:2).collect { it.id } == [3, 1, 2, 0].collect(intToCommit)
	}

	def 'log with range returns only the commits in that range'() {
		expect:
		grgit.log {
			range intToCommit(2), intToCommit(4)
		}.collect { it.id } == [4, 3, 1].collect(intToCommit)
	}

	def 'log with path includes only commits with changes for that path'() {
		expect:
		grgit.log(paths:['2.txt']).collect { it.id } == [5, 0].collect(intToCommit)
	}
}
