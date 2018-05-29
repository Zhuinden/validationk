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

private typealias Validator<T> = ValidationCallback<T>.() -> Unit

/**
 * Used to notify the validation that a given validation is complete with a given result.
 */
class ValidationCallback<ErrorType> internal constructor(private val validation: Validation<ErrorType>) {
    private var didCallback = false

    /**
     * Checks if the predicate is true.
     *
     * If it's true, then it is successful.
     * If it's false, then the error is returned.
     */
    inline fun check(error: ErrorType, predicate: () -> Boolean) {
        val result = predicate()
        if(result) {
            success()
        } else {
            failure(error)
        }
    }

    private fun checkCallback() {
        if(didCallback) {
            throw IllegalStateException("Callback can be invoked only once!")
        }
        didCallback = true
    }

    /**
     * Signals that the validation is successful.
     */
    fun success() {
        checkCallback()
        validation.success()
    }

    /**
     * Signals that the validation failed, with the given error.
     */
    fun failure(error: ErrorType) {
        checkCallback()
        validation.failure(error)
    }
}

/**
 * The Validation class allows building a validation chain that can be evaluated.
 */
class Validation<ErrorType> private constructor() {
    private val pendingValidation = mutableListOf<Validator<ErrorType>>()

    /**
     * Adds a validation to the chain to be evaluated when evaluate() is called.
     *
     * The Validator is a callback, where `success()` or `failure()` should be called.
     * There is also `check(error, { bool })` for simple checks.
     */
    fun validate(callback: Validator<ErrorType>): Validation<ErrorType> = apply {
        pendingValidation.add(callback)
    }

    var isEvaluated: Boolean = false
        private set

    private var isSuccessful = true
    private var error: Any? = null

    private lateinit var onInvalid: (ErrorType) -> Unit
    private lateinit var onValid: () -> Unit

    internal fun success() {
        doEvaluate()
    }

    internal fun failure(error: ErrorType) {
        isSuccessful = false
        this@Validation.error = error
        doEvaluate()
    }

    private fun doEvaluate() {
        if(pendingValidation.isEmpty() || !isSuccessful) {
            isEvaluated = true
            if(isSuccessful) {
                onValid()
            } else {
                @Suppress("UNCHECKED_CAST")
                onInvalid(error as ErrorType)
            }
            return
        }

        val validator = pendingValidation.removeAt(0)
        validator.invoke(ValidationCallback(this))
    }

    /**
     * Evaluates the validation chain. When an error occurs, the evaluation stops, and the error is returned.
     *
     * Can only be called once.
     */
    fun evaluate(onInvalid: (ErrorType) -> Unit, onValid: () -> Unit): Validation<ErrorType> {
        if(isEvaluated) {
            throw IllegalStateException("This validation has already been evaluated!")
        }
        this@Validation.onInvalid = onInvalid
        this@Validation.onValid = onValid
        doEvaluate()
        return this
    }

    companion object {
        /**
         * This lets you begin building a chain of validations.
         *
         * It only gets evaluated when evaluate() is called.
         */
        @Suppress("RemoveExplicitTypeArguments")
        fun <ErrorType> buildChain(): Validation<ErrorType> = Validation<ErrorType>()
    }
}