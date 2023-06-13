package org.jetbrains.packagesearch.packageversionutils

import assertk.Assert
import assertk.assertAll
import assertk.assertThat
import assertk.fail
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.ArgumentsProvider
import org.junit.jupiter.params.provider.ArgumentsSource
import org.junit.jupiter.params.provider.ValueSource
import kotlin.streams.asStream

internal class PackageVersionStabilityEvaluationTest {

    @ParameterizedTest(name = "[{index}] should flag {0} as UNSTABLE (empty or blank)")
    @ValueSource(strings = ["", "  ", "\n", "\r", "\t"])
    fun `should flag as unstable any blank or empty version names`() {
        assertAll {
            assertThat("").isNotConsideredStable()
            assertThat("  ").isNotConsideredStable()
            assertThat("\n").isNotConsideredStable()
            assertThat("\r").isNotConsideredStable()
            assertThat("\t").isNotConsideredStable()
        }
    }

    @ParameterizedTest(name = "[{index}] should flag {0} as UNSTABLE (contains one of the forbidden words)")
    @ArgumentsSource(ForbiddenWordsProvider::class)
    fun `should flag as unstable any version name containing one of the forbidden words`(versionName: String) {
        assertThat(versionName).isNotConsideredStable()
    }

    @ParameterizedTest(name = "[{index}] should flag {0} as UNSTABLE (avoid false positives)")
    @ArgumentsSource(UnstableNamesProvider::class)
    fun `should flag any unstable version name as unstable`(versionName: String) {
        assertThat(versionName).isNotConsideredStable()
    }

    @ParameterizedTest(name = "[{index}] should flag {0} as UNSTABLE (simple)")
    @ArgumentsSource(SimpleUnstableProvider::class)
    fun `should flag any simple unstable version name as unstable`(versionName: String) {
        assertThat(versionName).isNotConsideredStable()
    }

    @ParameterizedTest(name = "[{index}] should flag {0} as STABLE (avoid false negatives)")
    @ArgumentsSource(StableNamesProvider::class)
    fun `should flag any stable version name as stable`(versionName: String) {
        assertThat(versionName).isConsideredStable()
    }

    object ForbiddenWordsProvider : ArgumentsProvider by arguments(
        forbiddenWords.flatMap { forbiddenWord ->
            versionTemplates.map { template ->
                String.format(
                    template,
                    forbiddenWord
                )
            }
        }
    )

    object UnstableNamesProvider : ArgumentsProvider by arguments(shouldBeUnstable)

    object SimpleUnstableProvider : ArgumentsProvider by arguments(
        simplePrefixes.flatMap { prefix ->
            simpleSeparators.flatMap { separator ->
                simpleSuffixes.map { suffix -> Triple(prefix, separator, suffix) }
            }
        }.map { (prefix, separator, suffix) -> "$prefix$separator$suffix" }
    )

    object StableNamesProvider : ArgumentsProvider by arguments(shouldBeStable)

    companion object {

        val versionTemplates = listOf(
            "%s",
            "%s-1",
            "%s.1",
            "%s_1",
            "%s-01",
            "%s.01",
            "%s_01",
            "1.0-%s",
            "1.0-%s1",
            "1.0-%s01",
            "1.0-%s.1",
            "1.0-%s-1",
            "1.0-%s_1",
            "1.0-%s.01",
            "1.0-%s-01",
            "1.0-%s_01",
            "blah-%s",
            "blah-%s1",
            "blah-%s01",
            "blah.%s",
            "blah.%s1",
            "blah.%s01",
            "blah1%s",
            "blah1%s1",
            "blah1%s.1",
            "blah1%s-1",
            "blah1%s_1",
            "blah1%s01",
            "blah1%s.01",
            "blah1%s-01",
            "blah1%s_01"
        )

        val shouldBeUnstable = listOf(
            "1.0.9902_devpreview_1",
            "1.0.9902_rdev1",
            "10.001.292_rdev1",
            "10.001.292_RDEV1",
            "1.0.1+191-dev",
            "1.0.1+191-DEV",
            "2.0.0-relcandidate-01",
            "2.0.0-releasecandidate",
            "7.0.0.cr1",
            "3.0-b74b",
            "3.0-B74B",
            "7.24.0.t043",
            "7.24.0.T043",
            "1.0.0-RC10",
            "1.0.0.rc10",
            "1.0.0.RC9.1",
            "1.0.0-RC9.1",
            "0.58.0.21-rctest",
            "0.58.0.21-RCTEST",
            "master-30",
            "1.6.0-eap-29",
            "1.6.0-eap-33",
            "1.4.10-dev-67",
            "1.4.30-dev-75",
            "1.4.20.2-dev-62",
            "main-81"
        )

        val shouldBeStable = listOf(
            "1.0.1+191-a4e223b",
            "1.0.1+191-A4E223B",
            "1",
            "1.0",
            "1.0.0",
            "1.0-release",
            "1.0-RELEASE",
            "1.0.arcane",
            "1.0.ARCANE",
            "1-expressive",
            "1-EXPRESSIVE",
            "1-incubus",
            "1-INCUBUS",
            "1.spread",
            "1.SPREAD",
            "2.1devolution",
            "2.1DEVOLUTION",
            "20190817-preposterous.novel",
            "20190817-PREPOSTEROUS.NOVEL",
            "0.3.6-33-a9543e",
            "3.3.1+141-a4a85724",
            "asia.something",
            "ASIA.SOMETHING",
            "3.2.1.snapping1",
            "3.2.1.SNAPPING1",
            "2.0.0-candidatetosomething",
            "2.0.0-CANDIDATETOSOMETHING",
            "1.2",
            "1.2.3",
            "1.2.3.4",
            "1.2.3.4.5",
            "1.2.3.4.5-eb3ed70267e450f967a925f1744885be4bf7fe19",
            "1.2.3.4.5+eb3ed70267e450f967a925f1744885be4bf7fe19",
            "09.0",
            "1.0-final",
            "1.0-FINAL",
            "1.0-ga",
            "1.0-GA",
            "1.0-release.1",
            "1.0-RELEASE.1",
            "foo",
            "foo.bar",
            "foo.bar.baz",
            "FOO",
            "FOO.BAR",
            "FOO.BAR.BAZ",
            "@1",
            "@1.1",
            "#1",
            "#1.1",
            "$1",
            "$1.1"
        )

        val forbiddenWords = listOf(
            "alpha",
            "beta",
            "bate",
            "commit",
            "unofficial",
            "exp",
            "experiment",
            "experimental",
            "milestone",
            "deprecated",
            "release-candidate",
            "rc",
            "rctest",
            "cr",
            "draft",
            "ignored",
            "test",
            "placeholder",
            "incubating",
            "nightly",
            "weekly",
            "master",
            "main"
        )

        val simplePrefixes = listOf("", "0", "1", "0.1", "1.0", "2.0")

        val simpleSeparators = listOf("", "-", ".", "_")

        val simpleSuffixes = listOf(
            "snapshot", "alpha", "beta", "dev", "draft", "eap",
            "release-candidate", "rc", "milestone", "test", "nightly"
        )
    }
}

fun Assert<String>.isConsideredStable() {
    given { versionName ->
        if (!PackageVersionUtils.evaluateStability(versionName)) fail(
            "The version '$versionName' should have been considered stable, but wasn't"
        )
    }
}

fun Assert<String>.isNotConsideredStable() {
    given { versionName ->
        if (PackageVersionUtils.evaluateStability(versionName)) fail(
            "The version '$versionName' should have been considered unstable, but wasn't"
        )
    }
}

inline fun <reified T : Any> arguments(args: Iterable<T>): ArgumentsProvider = ArgumentsProvider {
    args.asSequence().map { Arguments.of(it) }.asStream()
}
