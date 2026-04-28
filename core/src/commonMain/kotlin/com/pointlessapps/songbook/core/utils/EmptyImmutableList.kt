package com.pointlessapps.songbook.core.utils

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

fun <T> emptyImmutableList(): ImmutableList<T> = EmptyImmutableList

fun <T : Any> persistentListOfNotNull(vararg elements: T?): ImmutableList<T> =
    elements.filterNotNull().toImmutableList()

private object EmptyIterator : ListIterator<Nothing> {
    override fun hasNext(): Boolean = false
    override fun hasPrevious(): Boolean = false
    override fun nextIndex(): Int = 0
    override fun previousIndex(): Int = -1
    override fun next(): Nothing = throw NoSuchElementException()
    override fun previous(): Nothing = throw NoSuchElementException()
}

private object EmptyImmutableList : ImmutableList<Nothing> {
    override val size = 0
    override fun isEmpty() = true
    override fun contains(element: Nothing) = false
    override fun iterator() = EmptyIterator
    override fun containsAll(elements: Collection<Nothing>) = false
    override fun get(index: Int) =
        throw IndexOutOfBoundsException("Empty list doesn't contain element at index $index.")

    override fun indexOf(element: Nothing) = -1
    override fun lastIndexOf(element: Nothing) = -1
    override fun listIterator() = EmptyIterator
    override fun listIterator(index: Int) = EmptyIterator
}
