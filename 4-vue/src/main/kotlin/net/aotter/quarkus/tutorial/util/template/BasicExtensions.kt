package net.aotter.quarkus.tutorial.util.template

import io.quarkus.qute.TemplateExtension
import kotlin.math.max
import kotlin.math.min

@TemplateExtension
class BasicExtensions {
    companion object{
        @JvmStatic
        fun inc(number: Number, amount: Number): Long = number.toLong() + amount.toLong()

        @JvmStatic
        fun paginationList(currentPage: Number, totalPages: Number, elements: Int): List<Long>{
            val half: Int = Math.floorDiv(elements - 1 , 2)
            val leftMinus: Int = if(elements % 2 == 0) 1 else 0
            var left = (currentPage.toInt() - half - leftMinus).toLong()
            var right = (currentPage.toInt() + half).toLong()
            if(left < 1){
                right = min(totalPages.toLong(), right + (1L - left))
                left = 1
            }else if(right > totalPages.toLong()){
                left = max(1, left - (right - totalPages.toLong()))
                right = totalPages.toLong()
            }
            return listOf(left..right).flatten()
        }
    }
}