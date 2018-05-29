# ValidationK

Chainable validation, inspired by the Validated monad, but a bit less complex, and a bit less smart. 

I must admit that I wrote this when I was very tired and it's for a friend, but I don't even know if this is what he needs.

I added the `K` at the end because, well, most Kotlin libraries do that. The name `Validation` is just boring without it.

## What's it like?

``` kotlin
    Validation.buildChain<String>()
            .validate { check("failure") { some boolean expression} }
            .validate {
                when {
                    x -> success()
                    else -> failure("blah blah")
                }
            }
            .evaluate(
                onInvalid = { failure: String ->
                    // something failed
                },
                onValid = {
                    // everything passed
                }
            )
```

The chain is evaluated only when `evaluate` is called.

## Using ValidationK

No release yet, but the whole project is one file, so if you need it, you can add it into your project, or at least try it, figure out if it even makes sense, etc.

## License

    Copyright 2018 Gabor Varadi

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
