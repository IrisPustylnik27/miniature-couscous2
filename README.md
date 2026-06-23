# Configurable Fibonacci Heap Implementation

This project implements a configurable Fibonacci Heap over positive integers in Java.
The heap supports different execution modes by allowing lazy or non-lazy behavior for meld and decrease-key operations.

The project was developed as a team project for a Data Structures course.

## Overview

The implementation provides a heap-based priority queue with support for core Fibonacci Heap operations.
The heap can be initialized with two configuration flags:

* `lazyMelds` — controls whether meld operations are performed lazily
* `lazyDecreaseKeys` — controls whether decrease-key operations use cascading cuts or heapify-up behavior

The goal of the project was to practice advanced data structures, object-oriented programming, debugging, pointer/reference-based logic, and amortized complexity analysis.

## Features

* Configurable Fibonacci Heap implementation
* Supports lazy and non-lazy meld operations
* Supports lazy and non-lazy decrease-key behavior
* Core heap operations:

  * `insert`
  * `findMin`
  * `deleteMin`
  * `decreaseKey`
  * `delete`
  * `meld`
* Heap-specific logic:

  * Circular doubly linked root lists
  * Tree linking
  * Successive linking / consolidation
  * Cascading cuts
  * Marked nodes
  * Rank management
* Performance counters:

  * Total links
  * Total cuts
  * Total heapify costs
  * Number of trees
  * Number of marked nodes

## Technologies

* Java
* Object-Oriented Programming
* Data Structures

## Main Concepts Practiced

* Fibonacci Heap structure and operations
* Priority queue implementation
* Lazy vs non-lazy operation strategies
* Circular linked lists
* Tree consolidation
* Cascading cuts
* Amortized complexity analysis
* Debugging complex reference-based structures

## Example Usage

```java
Heap heap = new Heap(true, true);

Heap.HeapItem item1 = heap.insert(10, "A");
Heap.HeapItem item2 = heap.insert(5, "B");

Heap.HeapItem min = heap.findMin();

heap.decreaseKey(item1, 7);
heap.deleteMin();
```

## Project Structure

```text
Heap.java
```

The main file contains:

* `Heap` — the main heap implementation
* `HeapNode` — internal node representation
* `HeapItem` — item stored inside the heap, containing key and info

## Team

This project was developed by:

* Ksenia Iaremenko
* Iris Pustylnik

## Notes

This project was created for academic purposes as part of a university Data Structures course.
