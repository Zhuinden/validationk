/*
 * Copyright 2018 Gabor Varadi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.zhuinden.validationk

import junit.framework.Assert
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

/**
 * Created by Zhuinden on 2018.05.29.
 */

class ValidationTest {
    @Test
    fun testValidationFailure() {
        val something = "blah"
        val another = "another"
        val x = true

        fun <E> doAsync(callback: ValidationCallback<E>) {
            Thread(Runnable {
                Thread.sleep(1250)
                callback.success()
            }).start()
        }

        var didExecuteLast = false
        val validation = Validation.buildChain<String>()
            .validate { check("failure") { "blah" == something } }
            .validate {
                when {
                    x -> success()
                    else -> failure("blah blah")
                }
            }
            .validate { doAsync(this) }
            .validate { check("invalid blah") { "another" != another } }
            .validate { didExecuteLast = true; success() }
            .evaluate(
                onInvalid = { failure: String ->
                    assertThat(failure).isEqualTo("invalid blah")
                    assertThat(didExecuteLast).isFalse()
                },
                onValid = {
                    Assert.fail()
                }
            )

        Thread.sleep(1500)
        assertThat(validation.isEvaluated).isTrue()
    }

    @Test
    fun testValidationSuccess() {
        val something = "blah"
        val another = "another"
        val x = true

        fun <E> doAsync(callback: ValidationCallback<E>) {
            Thread(Runnable {
                Thread.sleep(1250)
                callback.success()
            }).start()
        }

        var didExecuteLast = false
        val validation = Validation.buildChain<String>()
            .validate { check("failure") { "blah" == something } }
            .validate {
                when {
                    x -> success()
                    else -> failure("blah blah")
                }
            }
            .validate { doAsync(this) }
            .validate { check("invalid blah") { "another" == another } }
            .validate { didExecuteLast = true; success() }
            .evaluate(
                onInvalid = { failure: String ->
                    Assert.fail()
                },
                onValid = {
                    assertThat(didExecuteLast).isTrue()
                }
            )

        Thread.sleep(1500)
        assertThat(validation.isEvaluated).isTrue()
    }

    @Test
    fun evaluateOnlyOnce() {
        val onInvalid = { failure: Nothing ->
        }
        val onValid = {
        }

        try {
            Validation.buildChain<Nothing>().evaluate(onInvalid, onValid).evaluate(onInvalid, onValid)
            Assert.fail()
        } catch (e: IllegalStateException) {
            // OK!
        }
    }

    @Test
    fun validatorSuccessOnlyOnce() {
        val onInvalid = { failure: Nothing ->
        }
        val onValid = {
        }

        try {
            Validation.buildChain<Nothing>().validate { success(); success() }.evaluate(onInvalid, onValid)
            Assert.fail()
        } catch (e: IllegalStateException) {
            // OK!
        }
    }

    @Test
    fun validatorFailureOnlyOnce() {
        val onInvalid = { failure: Any? ->
        }
        val onValid = {
        }

        try {
            Validation.buildChain<Any?>().validate { failure(null); failure(null) }.evaluate(onInvalid, onValid)
            Assert.fail()
        } catch (e: IllegalStateException) {
            // OK!
        }
    }

    @Test
    fun isEvaluated() {
        val onInvalid = { failure: Any? ->
        }
        val onValid = {
        }

        val chain = Validation.buildChain<Any?>()
        assertThat(chain.isEvaluated).isFalse()
        chain.evaluate(onInvalid, onValid)
        assertThat(chain.isEvaluated).isTrue()
    }
}