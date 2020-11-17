![FP Foundation Logo](logo/Foundation.png)<br>

## Synopsis

### Module 1: Function

Scala is a language with first-class functions. This simple fact has a tremendous impact on how we program. 
In this module, we analyse the various syntax and concepts around functions in Scala. 
We also look at what is the functional programming subset of Scala and what are the benefits and 
restrictions of using it.

**Objectives**:
* Getting familiar with the various function encodings in Scala (val/def, curried/uncurried, lazy/strict parameters).
* Understand rules of type inference.
* Learn to detect if a function satisfies the functional programming subset.

### Module 2: Side Effect

Functional programming is considerably restrictive. For example, we cannot do any actions like sending 
an email to our users or update a record in the database. Yet, that's something we need to do in our daily
job. In this section, we will discuss the workarounds functional programmers found to interact with the
outside world while respecting the limitation of functional programming. In particular, we examine Thunk,
IO and some initiation to concurrency.

**Objectives**:
* Being able to write an entire program following functional programming principles.
* Learn how to test IO and laziness.
* Getting familiar with abstract API based on type parameters.

### Module 3: Error Handling

There are many ways to deal with runtime errors. The easiest and most common method consists of throwing 
exceptions. Unfortunately, it is not compatible with the functional programming paradigm. In this module,
we look at the problems caused by exceptions and the various ways we can safely encode errors in Scala.

**Objectives**:
* Learn when to use Option, Try and Either.
* Learn how to encode errors with Algebraic Data Type (ADT) and the various trade-offs.
* Practice how to accumulate errors or fail early.

### Module 4: Type

We all have the tendency to use types that are too large for our purpose. This leads to either runtime 
exceptions or a lot of error handling logic. In this module, we look at using Algebraic Data Types (ADT)
and type parameters to constraint our programs. We will also discuss various testing strategies and the
best way to use types and tests together.

**Objectives**:
* Learn how to precisely encode business requirements with types.
* Getting more familiar with Scala's type hierarchy and type parameters.

## SBT 101

Sbt is the most common build tool for Scala projects and 
it is the one we use in this course.

Start sbt shell by running `sbt` in your terminal 

| Actions           | Command to run in sbt shell |
|-------------------|-----------------------------|
| Compile           | exercises/compile           |
| Run tests         | exercises/test              | 
| Run tests with regex | exercises/testOnly *FunctionEx*   |
| Run tests with regex v2   | exercises/testOnly *FunctionEx* -- -z "selectDigits" |
