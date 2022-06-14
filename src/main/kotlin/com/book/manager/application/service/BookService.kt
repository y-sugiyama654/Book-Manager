package com.book.manager.application.service

import com.book.manager.domain.model.BookWithRental
import com.book.manager.domain.repository.BookRepository

class BookService(
    private val bookRepository: BookRepository
) {

    fun getList(): List<BookWithRental> {
        return bookRepository.findAllWithRental()
    }
}