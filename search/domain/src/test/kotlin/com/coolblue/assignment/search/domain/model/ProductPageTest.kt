package com.coolblue.assignment.search.domain.model

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class ProductPageTest {

    @Test
    fun `given page 1 of 3, then next page is 2`() {
        val page = ProductPage(products = emptyList(), page = 1, pageCount = 3)

        assertThat(page.nextPage).isEqualTo(2)
    }

    @Test
    fun `given the last page, then there is no next page`() {
        val page = ProductPage(products = emptyList(), page = 3, pageCount = 3)

        assertThat(page.nextPage).isNull()
    }
}
